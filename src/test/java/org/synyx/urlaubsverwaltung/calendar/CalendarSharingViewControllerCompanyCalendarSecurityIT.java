package org.synyx.urlaubsverwaltung.calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CalendarSharingViewControllerCompanyCalendarSecurityIT {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonService personService;
    @MockBean
    private PersonCalendarService personCalendarService;
    @MockBean
    private CompanyCalendarService companyCalendarService;
    @MockBean
    private DepartmentService departmentService;
    @MockBean
    private CalendarAccessibleService calendarAccessibleService;

    // =========================================================================================================
    // company calendar => link

    @Test
    @WithMockUser(username = "user")
    public void linkCompanyCalendarForUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/company"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    public void linkCompanyCalendarAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/company"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void linkCompanyCalendarAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/company"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void linkCompanyCalendarAsAdminIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/company"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void linkCompanyCalendarAsInactiveIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/company"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void linkCompanyCalendarAsDepartmentHeadIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/company"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void linkCompanyCalendarAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/company"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "otheruser")
    public void linkCompanyCalendarForOtherUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/company"))
            .andExpect(status().isForbidden());
    }

    // =========================================================================================================
    // company calendar => unlink

    @Test
    @WithMockUser(username = "user")
    public void unlinkCompanyCalendarForUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/company").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    public void unlinkCompanyCalendarAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/company").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void unlinkCompanyCalendarAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/company").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void unlinkCompanyCalendarAsAdminIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/company").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void unlinkCompanyCalendarAsInactiveIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/company").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void unlinkCompanyCalendarAsDepartmentHeadIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/company").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void unlinkCompanyCalendarAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/company").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "otheruser")
    public void unlinkCompanyCalendarForOtherUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/company").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    // =========================================================================================================
    // COMPANY CALENDAR ACCESSIBLE FEATURE

    @Test
    @WithMockUser(authorities = "BOSS")
    public void enableCompanyCalendarFeatureAsBossIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/company/accessible"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void enableCompanyCalendarFeatureAsOfficeIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/company/accessible"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void enableCompanyCalendarFeatureAsAdminIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/company/accessible"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void enableCompanyCalendarFeatureAsInactiveIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/company/accessible"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void enableCompanyCalendarFeatureAsDepartmentHeadIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/company/accessible"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void enableCompanyCalendarFeatureAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/company/accessible"))
            .andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
