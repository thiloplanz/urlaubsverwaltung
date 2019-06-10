package org.synyx.urlaubsverwaltung.statistics.vacationoverview.web;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationForLeaveVacationOverviewControllerTest {

    private ApplicationForLeaveVacationOverviewController sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;

    @Before
    public void setUp() {
        sut = new ApplicationForLeaveVacationOverviewController(personService, departmentService);
    }

    @Test
    public void applicationForLeaveVacationOverviewPostRedirect() throws Exception {

        final ResultActions resultActions = perform(post("/web/application/vacationoverview"));
        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andExpect(view().name("redirect:/web/application/vacationoverview"));
    }

    @Test
    public void applicationForLeaveVacationOverviewNoPermissions() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final Department department = new Department();
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(singletonList(department));

        final ResultActions resultActions = perform(get("/web/application/vacationoverview"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("departments", hasItem(department)));
        resultActions.andExpect(view().name("application/vacation_overview"));
    }

    @Test
    public void applicationForLeaveVacationOverviewBOSS() throws Exception {

        final Person boss = new Person();
        boss.setPermissions(singletonList(BOSS));
        when(personService.getSignedInUser()).thenReturn(boss);

        final Department department = new Department();
        when(departmentService.getAllDepartments()).thenReturn(singletonList(department));

        final ResultActions resultActions = perform(get("/web/application/vacationoverview"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("departments", hasItem(department)));
        resultActions.andExpect(view().name("application/vacation_overview"));
    }

    @Test
    public void applicationForLeaveVacationOverviewOFFICE() throws Exception {

        final Person office = new Person();
        office.setPermissions(singletonList(OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);

        final Department department = new Department();
        when(departmentService.getAllDepartments()).thenReturn(singletonList(department));

        final ResultActions resultActions = perform(get("/web/application/vacationoverview"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("departments", hasItem(department)));
        resultActions.andExpect(view().name("application/vacation_overview"));
    }

    @Test
    public void applicationForLeaveVacationOverviewSECONDSTAGE() throws Exception {

        final Person ssa = new Person();
        ssa.setPermissions(singletonList(SECOND_STAGE_AUTHORITY));
        when(personService.getSignedInUser()).thenReturn(ssa);

        final Department department = new Department();
        when(departmentService.getManagedDepartmentsOfSecondStageAuthority(ssa)).thenReturn(singletonList(department));

        final ResultActions resultActions = perform(get("/web/application/vacationoverview"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("departments", hasItem(department)));
        resultActions.andExpect(view().name("application/vacation_overview"));
    }

    @Test
    public void applicationForLeaveVacationOverviewDEPARTMENTHEAD() throws Exception {

        final Person departmentHead = new Person();
        departmentHead.setPermissions(singletonList(DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(departmentHead);

        final Department department = new Department();
        when(departmentService.getManagedDepartmentsOfDepartmentHead(departmentHead)).thenReturn(singletonList(department));

        final ResultActions resultActions = perform(get("/web/application/vacationoverview"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("departments", hasItem(department)));
        resultActions.andExpect(view().name("application/vacation_overview"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
