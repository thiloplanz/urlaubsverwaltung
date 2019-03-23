package org.synyx.urlaubsverwaltung.restapi.workday;

import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.restapi.ApiExceptionHandlerControllerAdvice;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.core.period.DayLength.FULL;


@RunWith(MockitoJUnitRunner.class)
public class WorkDayControllerTest {

    private WorkDayController sut;

    @Mock
    private PersonService personServiceMock;
    @Mock
    private WorkDaysService workDaysServiceMock;

    @Before
    public void setUp() {
        sut = new WorkDayController(personServiceMock, workDaysServiceMock);
    }

    @Test
    public void ensureReturnsWorkDays() throws Exception {

        Person person = TestDataCreator.createPerson();
        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));
        when(workDaysServiceMock.getWorkDays(any(DayLength.class), any(DateMidnight.class), any(DateMidnight.class), any(Person.class)))
            .thenReturn(BigDecimal.ONE);

        perform(get("/api/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL")
            .param("person", "23"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.response").exists())
            .andExpect(jsonPath("$.response.workDays").exists())
            .andExpect(jsonPath("$.response.workDays", is("1")));

        verify(personServiceMock).getPersonByID(23);
        verify(workDaysServiceMock).getWorkDays(FULL, new DateMidnight(2016, 1, 4), new DateMidnight(2016, 1, 4), person);
    }


    @Test
    public void ensureBadRequestForMissingFromParameter() throws Exception {

        perform(get("/api/workdays")
            .param("to", "2016-01-06")
            .param("length", "FULL")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidFromParameter() throws Exception {

        perform(get("/api/workdays")
            .param("from", "foo")
            .param("to", "2016-01-06")
            .param("length", "FULL")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForMissingToParameter() throws Exception {

        perform(get("/api/workdays")
            .param("from", "2016-01-01")
            .param("length", "FULL")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidToParameter() throws Exception {

        perform(get("/api/workdays")
            .param("from", "2016-01-01")
            .param("to", "foo")
            .param("length", "FULL")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForMissingPersonParameter() throws Exception {

        perform(get("/api/workdays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-06")
            .param("length", "FULL"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidPersonParameter() throws Exception {

        perform(get("/api/workdays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-06")
            .param("length", "FULL")
            .param("person", "foo"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestIfThereIsNoPersonForGivenID() throws Exception {

        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.empty());

        perform(get("/api/workdays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-06")
            .param("length", "FULL")
            .param("person", "23"))
            .andExpect(status().isBadRequest());

        verify(personServiceMock).getPersonByID(23);
    }


    @Test
    public void ensureBadRequestForMissingLengthParameter() throws Exception {

        perform(get("/api/workdays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-06")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidLengthParameter() throws Exception {

        Person person = TestDataCreator.createPerson("muster");
        when(personServiceMock.getPersonByID(anyInt())).thenReturn(Optional.of(person));

        perform(get("/api/workdays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-06")
            .param("length", "FOO")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidPeriod() throws Exception {

        perform(get("/api/workdays")
            .param("from", "2016-01-01")
            .param("to", "2015-01-06")
            .param("length", "FULL")
            .param("person", "23"))
            .andExpect(status().isBadRequest());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).setControllerAdvice(new ApiExceptionHandlerControllerAdvice()).build().perform(builder);
    }
}
