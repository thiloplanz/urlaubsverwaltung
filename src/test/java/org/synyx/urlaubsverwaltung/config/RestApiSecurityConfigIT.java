package org.synyx.urlaubsverwaltung.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.time.DayOfWeek.MONDAY;
import static java.util.Collections.singletonList;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "auth=default")
@Transactional
public class RestApiSecurityConfigIT {

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PersonService personService;

    @Autowired
    private WorkingTimeService workingTimeService;

    @Test
    public void getAbsencesWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/absences"));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void getAbsencesWithBasicAuthIsOk() throws Exception {

        createAuthenticatedPerson();

        final Person person = new Person("person", "Fresh", "Holy", "");
        final Person savedPerson = personService.save(person);
        final String savedPersonId = savedPerson.getId() == null ? "" : savedPerson.getId().toString();

        final ResultActions resultActions = perform(get("/api/absences")
            .param("year", String.valueOf(LocalDate.now().getYear()))
            .param("person", savedPersonId)
            .with(httpBasic("authenticated", "secret")));

        resultActions.andExpect(status().isOk());
    }

    @Test
    public void getAvailabilitiesWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/availabilities"));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void getAvailabilitiesWithBasicAuthIsOk() throws Exception {

        createAuthenticatedPerson();

        final Person person = new Person("John", "Fresh", "John", "");
        personService.save(person);

        List<Integer> workingDays = singletonList(MONDAY.getValue());
        workingTimeService.touch(workingDays, Optional.empty(), LocalDate.now(), person);

        LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/availabilities")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("person", "John")
            .with(httpBasic("authenticated", "secret")));

        resultActions.andExpect(status().isOk());
    }

    @Test
    public void getDepartmentsWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/departments"));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void getDepartmentsWithBasicAuthIsOk() throws Exception {

        createAuthenticatedPerson();

        final ResultActions resultActions = perform(
            get("/api/departments").with(httpBasic("authenticated", "secret"))
        );

        resultActions.andExpect(status().isOk());
    }

    @Test
    public void getPersonsWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons"));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void getPersonsWithBasicAuthIsOk() throws Exception {

        createAuthenticatedPerson();

        final ResultActions resultActions = perform(get("/api/persons").with(httpBasic("authenticated", "secret")));

        resultActions.andExpect(status().isOk());
    }

    @Test
    public void getHolidaysWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/holidays"));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void getHolidaysWithBasicAuthIsOk() throws Exception {

        createAuthenticatedPerson();

        final Person person = new Person("person", "Fresh", "Holy", "");
        final Person savedPerson = personService.save(person);
        final String savedPersonId = savedPerson.getId() == null ? "" : savedPerson.getId().toString();

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/holidays")
            .param("year", String.valueOf(now.getYear()))
            .param("month", String.valueOf(now.getMonthValue()))
            .param("personId", savedPersonId)
            .with(httpBasic("authenticated", "secret")));

        resultActions.andExpect(status().isOk());
    }

    @Test
    public void getSickNotesWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/sicknotes"));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void getSickNotesWithBasicAuthIsOk() throws Exception {

        final Person office = new Person("office", "Betty", "Secure", "");
        office.setPassword("bc49b860775c4e6a813800fe827f093d40cd34a84134af9c6c67f5b68b0ccc43be73479103f8b714"); // secret
        office.setPermissions(singletonList(OFFICE));
        personService.save(office);

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .with(httpBasic("office", "secret")));

        resultActions.andExpect(status().isOk());
    }

    @Test
    public void getVacationsWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/vacations"));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void getVacationsWithBasicAuthIsOk() throws Exception {

        createAuthenticatedPerson();

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .with(httpBasic("authenticated", "secret")));

        resultActions.andExpect(status().isOk());
    }

    @Test
    public void getVacationOverviewWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/vacationoverview"));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void getVacationOverviewWithBasicAuthIsOk() throws Exception {

        createAuthenticatedPerson();

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/vacationoverview")
            .param("selectedYear", String.valueOf(now.getYear()))
            .param("selectedMonth", String.valueOf(now.getMonthValue()))
            .param("selectedDepartment", "1")
            .with(httpBasic("authenticated", "secret")));

        resultActions.andExpect(status().isOk());
    }

    @Test
    public void getWorkdaysWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/vacationoverview"));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void getWorkdaysWithBasicAuthIsOk() throws Exception {

        createAuthenticatedPerson();

        final Person person = new Person("person", "Fresh", "Holy", "");
        final Person savedPerson = personService.save(person);
        final String savedPersonId = savedPerson.getId() == null ? "" : savedPerson.getId().toString();

        List<Integer> workingDays = singletonList(MONDAY.getValue());
        workingTimeService.touch(workingDays, Optional.empty(), LocalDate.now(), savedPerson);

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/workdays")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("length", "FULL")
            .param("person", savedPersonId)
            .with(httpBasic("authenticated", "secret")));

        resultActions.andExpect(status().isOk());
    }

    private void createAuthenticatedPerson() {
        final Person authenticated = new Person("authenticated", "Only", "Aussie", "");
        authenticated.setPassword("bc49b860775c4e6a813800fe827f093d40cd34a84134af9c6c67f5b68b0ccc43be73479103f8b714"); // secret
        personService.save(authenticated);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {

        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
