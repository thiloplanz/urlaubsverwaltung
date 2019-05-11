package org.synyx.urlaubsverwaltung.holiday.api;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.api.ApiExceptionHandlerControllerAdvice;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class PublicHolidayControllerTest {

    private MockMvc mockMvc;

    private PublicHolidaysService publicHolidayServiceMock;
    private PersonService personServiceMock;
    private WorkingTimeService workingTimeServiceMock;
    private SettingsService settingsServiceMock;

    @Before
    public void setUp() {

        personServiceMock = mock(PersonService.class);
        publicHolidayServiceMock = mock(PublicHolidaysService.class);
        workingTimeServiceMock = mock(WorkingTimeService.class);
        settingsServiceMock = mock(SettingsService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new PublicHolidayController(publicHolidayServiceMock,
                        personServiceMock, workingTimeServiceMock, settingsServiceMock))
                .setControllerAdvice(new ApiExceptionHandlerControllerAdvice())
                .build();

        Settings settings = new Settings();
        settings.getWorkingTimeSettings().setFederalState(FederalState.BADEN_WUERTTEMBERG);
        when(settingsServiceMock.getSettings()).thenReturn(settings);
    }


    @Test
    public void ensureReturnsCorrectPublicHolidaysForYear() throws Exception {

        mockMvc.perform(get("/api/holidays").param("year", "2016")).andExpect(status().isOk());

        verify(publicHolidayServiceMock).getHolidays(2016, FederalState.BADEN_WUERTTEMBERG);
    }


    @Test
    public void ensureReturnsCorrectPublicHolidaysForYearAndMonth() throws Exception {

        mockMvc.perform(get("/api/holidays").param("year", "2016").param("month", "4")).andExpect(status().isOk());

        verify(publicHolidayServiceMock).getHolidays(2016, 4, FederalState.BADEN_WUERTTEMBERG);
    }


    @Test
    public void ensureReturnsCorrectPublicHolidaysForYearAndPersonWithOverriddenFederalState() throws Exception {

        Person person = TestDataCreator.createPerson();
        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));
        when(workingTimeServiceMock.getFederalStateForPerson(any(Person.class),
                    any(LocalDate.class)))
            .thenReturn(FederalState.BAYERN);

        mockMvc.perform(get("/api/holidays").param("year", "2016").param("person", "23")).andExpect(status().isOk());

        verify(publicHolidayServiceMock).getHolidays(2016, FederalState.BAYERN);
        verify(workingTimeServiceMock).getFederalStateForPerson(person, LocalDate.of(2016, 1, 1));
    }


    @Test
    public void ensureReturnsCorrectPublicHolidaysForYearAndMonthAndPersonWithOverriddenFederalState()
        throws Exception {

        Person person = TestDataCreator.createPerson();
        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));
        when(workingTimeServiceMock.getFederalStateForPerson(any(Person.class),
                    any(LocalDate.class)))
            .thenReturn(FederalState.BAYERN);

        mockMvc.perform(get("/api/holidays").param("year", "2016").param("month", "4").param("person", "23"))
            .andExpect(status().isOk());

        verify(publicHolidayServiceMock).getHolidays(2016, 4, FederalState.BAYERN);
        verify(workingTimeServiceMock).getFederalStateForPerson(person, LocalDate.of(2016, 4, 1));
    }


    @Test
    public void ensureBadRequestForMissingYearParameter() throws Exception {

        mockMvc.perform(get("/api/holidays")).andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidYearParameter() throws Exception {

        mockMvc.perform(get("/api/holidays").param("year", "foo")).andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidMonthParameter() throws Exception {

        mockMvc.perform(get("/api/holidays").param("year", "2016").param("month", "foo"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidPersonParameter() throws Exception {

        mockMvc.perform(get("/api/holidays").param("year", "2016").param("person", "foo"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestIfThereIsNoPersonForGivenID() throws Exception {

        when(personServiceMock.getPersonByID(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/holidays").param("year", "2016").param("person", "23"))
            .andExpect(status().isBadRequest());
    }
}
