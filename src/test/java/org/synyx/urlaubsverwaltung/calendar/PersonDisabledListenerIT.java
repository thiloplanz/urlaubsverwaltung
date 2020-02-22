package org.synyx.urlaubsverwaltung.calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.synyx.urlaubsverwaltung.person.PersonDisabledEvent;

import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {PersonDisabledListener.class})
public class PersonDisabledListenerIT {

    @MockBean
    private PersonCalendarService personCalendarService;
    @MockBean
    private DepartmentCalendarService departmentCalendarService;
    @MockBean
    private CompanyCalendarService companyCalendarService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    public void ensureDeletedPersonalCalendarOnPersonDisabledEvent() {

        applicationEventPublisher.publishEvent(new PersonDisabledEvent(this, 42));

        verify(personCalendarService).deletePersonalCalendarForPerson(42);
        verify(departmentCalendarService).deleteDepartmentsCalendarsForPerson(42);
        verify(companyCalendarService).deleteCalendarForPerson(42);
    }
}
