package org.synyx.urlaubsverwaltung.core.statistics;

import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.statistics.ApplicationForLeaveStatistics;
import org.synyx.urlaubsverwaltung.web.statistics.ApplicationForLeaveStatisticsBuilder;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationForLeaveStatisticsServiceImplTest {

    private SessionService sessionService;
    private PersonService personService;
    private DepartmentService departmentService;
    private ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;
    private ApplicationForLeaveStatisticsServiceImpl sut;

    @Before
    public void setUp() {

        sessionService = mock(SessionService.class);
        personService = mock(PersonService.class);
        departmentService = mock(DepartmentService.class);
        applicationForLeaveStatisticsBuilder = mock(ApplicationForLeaveStatisticsBuilder.class);

        sut = new ApplicationForLeaveStatisticsServiceImpl(sessionService, personService, departmentService, applicationForLeaveStatisticsBuilder);
    }

    @Test
    public void getStatisticsForDepartmentHead() {

        FilterPeriod filterPeriod = new FilterPeriod(java.util.Optional.ofNullable("01.01.2018"), java.util.Optional.ofNullable("31.12.2018"));

        Person person = new Person();
        person.setPermissions(Collections.singletonList(Role.DEPARTMENT_HEAD));
        when(sessionService.getSignedInUser()).thenReturn(person);

        Person departmentMember = new Person();
        when(departmentService.getManagedMembersOfDepartmentHead(person)).thenReturn(Collections.singletonList(departmentMember));

        when(applicationForLeaveStatisticsBuilder.build(person, filterPeriod.getStartDate(), filterPeriod.getEndDate()))
                .thenReturn(mock(ApplicationForLeaveStatistics.class));

        List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);

        assertThat(statistics.size(), is(1));
    }

    @Test
    public void getStatisticsForOtherThanDepartmentHead() {

        FilterPeriod filterPeriod = new FilterPeriod(java.util.Optional.ofNullable("01.01.2018"), java.util.Optional.ofNullable("31.12.2018"));

        Person person = new Person();
        person.setPermissions(Collections.singletonList(Role.BOSS));
        when(sessionService.getSignedInUser()).thenReturn(person);

        Person anyPerson = new Person();
        when(personService.getActivePersons()).thenReturn(Collections.singletonList(anyPerson));

        when(applicationForLeaveStatisticsBuilder.build(person, filterPeriod.getStartDate(), filterPeriod.getEndDate()))
                .thenReturn(mock(ApplicationForLeaveStatistics.class));

        List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);

        assertThat(statistics.size(), is(1));
    }

}
