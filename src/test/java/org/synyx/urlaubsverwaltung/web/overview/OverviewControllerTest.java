package org.synyx.urlaubsverwaltung.web.overview;


import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.security.SessionService;

import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus.REVOKED;
import static org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.core.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.core.person.Role.DEPARTMENT_HEAD;

@RunWith(MockitoJUnitRunner.class)
public class OverviewControllerTest {

    private OverviewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private AccountService accountService;
    @Mock
    private VacationDaysService vacationDaysService;
    @Mock
    private SessionService sessionService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private WorkDaysService calendarService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private OvertimeService overtimeService;
    @Mock
    private SettingsService settingsService;

    private Person person;

    @Before
    public void setUp() {
        sut = new OverviewController(personService, departmentService, accountService, vacationDaysService, sessionService,
            applicationService, calendarService, sickNoteService, overtimeService, settingsService);

        person = new Person();
        person.setId(1);
        person.setPermissions(singletonList(DEPARTMENT_HEAD));
        when(sessionService.getSignedInUser()).thenReturn(person);
    }

    @Test
    public void showOverview() throws Exception {
        MockHttpServletRequestBuilder builder = get("/web/overview?year=2017");

        final ResultActions resultActions = perform(builder);
        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/staff/1/overview?year=2017"));
    }

    @Test
    public void showOverviewWithoutYear() throws Exception {
        MockHttpServletRequestBuilder builder = get("/web/overview");

        final ResultActions resultActions = perform(builder);
        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/staff/1/overview"));
    }

    @Test
    public void showPersonalOverview() throws Exception {
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(sessionService.isSignedInUserAllowedToAccessPersonData(person, person)).thenReturn(true);
        when(calendarService.getWorkDays(any(), any(), any(), eq(person))).thenReturn(ONE);

        final Application revokedApplication = new Application();
        revokedApplication.setStatus(REVOKED);

        final VacationType vacationType = new VacationType();
        vacationType.setCategory(HOLIDAY);

        final Application waitingApplication = new Application();
        waitingApplication.setVacationType(vacationType);
        waitingApplication.setPerson(person);
        waitingApplication.setStatus(WAITING);
        waitingApplication.setStartDate(DateMidnight.now().minus(1L));
        waitingApplication.setEndDate(DateMidnight.now().plus(1L));

        final Application allowedApplication = new Application();
        allowedApplication.setVacationType(vacationType);
        allowedApplication.setPerson(person);
        allowedApplication.setStatus(ALLOWED);
        allowedApplication.setStartDate(DateMidnight.now().minus(10L));
        allowedApplication.setEndDate(DateMidnight.now().plus(10L));

        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(), any(), eq(person)))
            .thenReturn(asList(waitingApplication, revokedApplication, allowedApplication));

        final SickNote sickNote = new SickNote();
        sickNote.setStartDate(DateMidnight.now().minus(1L));
        sickNote.setEndDate(DateMidnight.now().plus(1L));
        final SickNote sickNote2 = new SickNote();
        sickNote2.setStartDate(DateMidnight.now().minus(10L));
        sickNote2.setEndDate(DateMidnight.now().plus(10L));
        when(sickNoteService.getByPersonAndPeriod(eq(person), any(), any())).thenReturn(asList(sickNote, sickNote2));

        MockHttpServletRequestBuilder builder = get("/web/staff/1/overview");
        final ResultActions resultActions = perform(builder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(view().name("person/overview"));
        resultActions.andExpect(model().attribute("applications", hasSize(2)));
        resultActions.andExpect(model().attribute("sickNotes", hasSize(2)));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
