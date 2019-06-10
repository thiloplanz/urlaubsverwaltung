package org.synyx.urlaubsverwaltung.sicknote.statistics.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatistics;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatisticsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(MockitoJUnitRunner.class)
public class SickNoteStatisticsControllerTest {

    private SickNoteStatisticsController sut;

    @Mock
    private SickNoteStatisticsService statisticsService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private WorkDaysService calendarService;

    @Before
    public void setUp() {
        sut = new SickNoteStatisticsController(statisticsService);
    }

    @Test
    public void sickNoteStatistics() throws Exception {

        final int year = 2017;
        final SickNoteStatistics sickNoteStatistics = new SickNoteStatistics(year, sickNoteService, calendarService);
        when(statisticsService.createStatistics(year)).thenReturn(sickNoteStatistics);

        final ResultActions resultActions = perform(get("/web/sicknote/statistics")
            .param("year", "2017"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("statistics", sickNoteStatistics));
        resultActions.andExpect(view().name("sicknote/sick_notes_statistics"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
