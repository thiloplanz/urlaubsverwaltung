package org.synyx.urlaubsverwaltung.holiday.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.settings.FederalState.BAYERN;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PublicHolidayApiControllerSecurityIT {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonService personService;
    @MockBean
    private WorkingTimeService workingTimeService;

    @Test
    public void getHolidaysWithoutAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/holidays"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void getHolidaysAsAuthenticatedUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/holidays")
            .param("year", "2016")
            .param("month", "11")
            .param("person", "1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void getHolidaysAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/holidays")
            .param("year", "2016")
            .param("month", "11")
            .param("person", "1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void getHolidaysAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/holidays")
            .param("year", "2016")
            .param("month", "11")
            .param("person", "1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    public void getHolidaysAsBossUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/holidays")
            .param("year", "2016")
            .param("month", "11")
            .param("person", "1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void getHolidaysAsAdminUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/holidays")
            .param("year", "2016")
            .param("month", "11")
            .param("person", "1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void getHolidaysAsInactiveUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/holidays")
            .param("year", "2016")
            .param("month", "11")
            .param("person", "1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void getHolidaysWithOfficeRoleIsOk() throws Exception {

        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person()));
        when(workingTimeService.getFederalStateForPerson(any(Person.class), any(LocalDate.class))).thenReturn(BAYERN);

        final ResultActions resultActions = perform(get("/api/holidays")
            .param("year", "2016")
            .param("month", "11")
            .param("person", "1"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user")
    public void getHolidaysWithSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(workingTimeService.getFederalStateForPerson(any(Person.class), any(LocalDate.class))).thenReturn(BAYERN);

        final ResultActions resultActions = perform(get("/api/holidays")
            .param("year", "2016")
            .param("month", "11")
            .param("person", "1"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "differentUser")
    public void getHolidaysWithDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final ResultActions resultActions = perform(get("/api/holidays")
            .param("year", "2016")
            .param("month", "11")
            .param("person", "1"));
        resultActions.andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
