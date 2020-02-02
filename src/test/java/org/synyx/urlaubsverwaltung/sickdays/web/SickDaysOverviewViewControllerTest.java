package org.synyx.urlaubsverwaltung.sickdays.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.time.LocalDate.parse;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasValue;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory.SICK_NOTE;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory.SICK_NOTE_CHILD;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteStatus.ACTIVE;

@RunWith(MockitoJUnitRunner.class)
public class SickDaysOverviewViewControllerTest {

    private SickDaysOverviewViewController sut;

    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private PersonService personService;
    @Mock
    private WorkDaysService calendarService;

    @Before
    public void setUp() {
        sut = new SickDaysOverviewViewController(sickNoteService, personService, calendarService);
    }

    @Test
    public void filterSickNotes() throws Exception {

        LocalDate from = LocalDate.of(2019, 1, 1);
        LocalDate to = LocalDate.of(2019, 12, 31);
        final FilterPeriod filterPeriod = new FilterPeriod(from, to);
        final String requestBody = new ObjectMapper().writeValueAsString(filterPeriod);

        final MockHttpServletRequestBuilder builder = post("/web/sicknote/filter");
        builder.accept(APPLICATION_JSON);
        builder.contentType(APPLICATION_JSON);
        builder.content(requestBody);

        final ResultActions resultActions = perform(builder);
        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/sicknote?from=01.01.2019&to=31.12.2019"));
    }

    @Test
    public void periodsSickNotesWithDateRange() throws Exception {

        final Person person = new Person();
        final List<Person> persons = singletonList(person);
        when(personService.getActivePersons()).thenReturn(persons);


        final SickNoteType childSickType = new SickNoteType();
        childSickType.setCategory(SICK_NOTE_CHILD);
        final SickNote childSickNote = new SickNote();
        childSickNote.setStartDate(parse("01.02.2019", ofPattern("dd.MM.yyyy")));
        childSickNote.setEndDate(parse("01.03.2019", ofPattern("dd.MM.yyyy")));
        childSickNote.setDayLength(FULL);
        childSickNote.setStatus(ACTIVE);
        childSickNote.setSickNoteType(childSickType);
        childSickNote.setPerson(person);
        childSickNote.setAubStartDate(parse("10.02.2019", ofPattern("dd.MM.yyyy")));
        childSickNote.setAubEndDate(parse("15.02.2019", ofPattern("dd.MM.yyyy")));
        when(calendarService.getWorkDays(childSickNote.getDayLength(), childSickNote.getStartDate(), childSickNote.getEndDate(), person))
            .thenReturn(ONE);
        when(calendarService.getWorkDays(childSickNote.getDayLength(), childSickNote.getAubStartDate(), childSickNote.getAubEndDate(), person))
            .thenReturn(BigDecimal.valueOf(5L));

        final SickNoteType sickType = new SickNoteType();
        sickType.setCategory(SICK_NOTE);
        final SickNote sickNote = new SickNote();
        sickNote.setStartDate(parse("01.04.2019", ofPattern("dd.MM.yyyy")));
        sickNote.setEndDate(parse("01.05.2019", ofPattern("dd.MM.yyyy")));
        sickNote.setDayLength(FULL);
        sickNote.setStatus(ACTIVE);
        sickNote.setSickNoteType(sickType);
        sickNote.setPerson(person);
        sickNote.setAubStartDate(parse("10.04.2019", ofPattern("dd.MM.yyyy")));
        sickNote.setAubEndDate(parse("20.04.2019", ofPattern("dd.MM.yyyy")));
        when(calendarService.getWorkDays(sickNote.getDayLength(), sickNote.getStartDate(), sickNote.getEndDate(), person))
            .thenReturn(TEN);
        when(calendarService.getWorkDays(sickNote.getDayLength(), sickNote.getAubStartDate(), sickNote.getAubEndDate(), person))
            .thenReturn(BigDecimal.valueOf(15L));

        final String requestStartDateString = "05.01.2019";
        final String requestEndDateString = "28.12.2019";
        final LocalDate requestStartDate = parse(requestStartDateString, ofPattern("dd.MM.yyyy"));
        final LocalDate requestEndDate = parse(requestEndDateString, ofPattern("dd.MM.yyyy"));
        when(sickNoteService.getByPeriod(requestStartDate, requestEndDate)).thenReturn(asList(sickNote, childSickNote));

        final ResultActions resultActions = perform(get("/web/sicknote")
            .param("from", requestStartDateString)
            .param("to", requestEndDateString));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("sickDays", hasValue(hasProperty("days", hasEntry("TOTAL", TEN)))));
        resultActions.andExpect(model().attribute("sickDays", hasValue(hasProperty("days", hasEntry("WITH_AUB", BigDecimal.valueOf(15L))))));
        resultActions.andExpect(model().attribute("childSickDays", hasValue(hasProperty("days", hasEntry("TOTAL", ONE)))));
        resultActions.andExpect(model().attribute("childSickDays", hasValue(hasProperty("days", hasEntry("WITH_AUB", BigDecimal.valueOf(5L))))));
        resultActions.andExpect(model().attribute("persons", persons));
        resultActions.andExpect(model().attribute("from", requestStartDate));
        resultActions.andExpect(model().attribute("to", requestEndDate));
        resultActions.andExpect(model().attribute("period", hasProperty("startDate", is(requestStartDate))));
        resultActions.andExpect(model().attribute("period", hasProperty("endDate", is(requestEndDate))));
        resultActions.andExpect(view().name("sicknote/sick_notes"));
    }

    @Test
    public void periodsSickNotesWithDateWithoutRange() throws Exception {

        final int year = ZonedDateTime.now(UTC).getYear();
        final LocalDate startDate = ZonedDateTime.now(UTC).withYear(year).with(firstDayOfYear()).toLocalDate();
        final LocalDate endDate = ZonedDateTime.now(UTC).withYear(year).with(lastDayOfYear()).toLocalDate();

        final ResultActions resultActions = perform(get("/web/sicknote"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("from", startDate));
        resultActions.andExpect(model().attribute("to", endDate));
        resultActions.andExpect(model().attribute("period", hasProperty("startDate", is(startDate))));
        resultActions.andExpect(model().attribute("period", hasProperty("endDate", is(endDate))));
        resultActions.andExpect(view().name("sicknote/sick_notes"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
