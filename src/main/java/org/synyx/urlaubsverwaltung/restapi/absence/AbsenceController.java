package org.synyx.urlaubsverwaltung.restapi.absence;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.restapi.ResponseWrapper;
import org.synyx.urlaubsverwaltung.restapi.RestApiDateFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;


@Api("Absences: Get all absences for a certain period")
@RestController("restApiAbsenceController")
@RequestMapping("/api")
public class AbsenceController {

    private final PersonService personService;
    private final ApplicationService applicationService;
    private final SickNoteService sickNoteService;

    @Autowired
    AbsenceController(PersonService personService, ApplicationService applicationService,
        SickNoteService sickNoteService) {

        this.personService = personService;
        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
    }

    @ApiOperation(
        value = "Get all absences for a certain period and person",
        notes = "Get all absences for a certain period and person"
    )
    @GetMapping("/absences")
    public ResponseWrapper<DayAbsenceList> personsVacations(
        @ApiParam(value = "Year to get the absences for", defaultValue = RestApiDateFormat.EXAMPLE_YEAR)
        @RequestParam("year")
        String year,
        @ApiParam(value = "Month of year to get the absences for")
        @RequestParam(value = "month", required = false)
        String month,
        @ApiParam(value = "ID of the person")
        @RequestParam("person")
        Integer personId,
        @ApiParam(value = "Type of absences, vacation or sick notes", allowableValues = "VACATION, SICK_NOTE")
        @RequestParam(value = "type", required = false)
        String type) {


        Optional<Person> optionalPerson = personService.getPersonByID(personId);

        if (!optionalPerson.isPresent()) {
            throw new IllegalArgumentException("No person found for ID=" + personId);
        }

        List<DayAbsence> absences = new ArrayList<>();
        Person person = optionalPerson.get();

        DateMidnight startDate = getStartDate(year, Optional.ofNullable(month));
        DateMidnight endDate = getEndDate(year, Optional.ofNullable(month));

        if (type == null || DayAbsence.Type.valueOf(type).equals(DayAbsence.Type.VACATION)) {
            absences.addAll(getVacations(startDate, endDate, person));
        }

        if (type == null || DayAbsence.Type.valueOf(type).equals(DayAbsence.Type.SICK_NOTE)) {
            absences.addAll(getSickNotes(startDate, endDate, person));
        }

        return new ResponseWrapper<>(new DayAbsenceList(absences));
    }


    private static DateMidnight getStartDate(String year, Optional<String> optionalMonth) {

        return optionalMonth.map(s -> DateUtil.getFirstDayOfMonth(parseInt(year), parseInt(s)))
            .orElseGet(() -> DateUtil.getFirstDayOfYear(parseInt(year)));

    }


    private static DateMidnight getEndDate(String year, Optional<String> optionalMonth) {

        return optionalMonth.map(s -> DateUtil.getLastDayOfMonth(parseInt(year), parseInt(s)))
            .orElseGet(() -> DateUtil.getLastDayOfYear(parseInt(year)));

    }


    private List<DayAbsence> getVacations(DateMidnight start, DateMidnight end, Person person) {

        List<DayAbsence> absences = new ArrayList<>();

        List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(start, end,
                    person)
                .stream()
                .filter(application ->
                            application.hasStatus(ApplicationStatus.WAITING)
                            || application.hasStatus(ApplicationStatus.TEMPORARY_ALLOWED)
                            || application.hasStatus(ApplicationStatus.ALLOWED))
                .collect(Collectors.toList());

        for (Application application : applications) {
            DateMidnight startDate = application.getStartDate();
            DateMidnight endDate = application.getEndDate();

            DateMidnight day = startDate;

            while (!day.isAfter(endDate)) {
                if (!day.isBefore(start) && !day.isAfter(end)) {
                    absences.add(new DayAbsence(day, application.getDayLength(), DayAbsence.Type.VACATION,
                            application.getStatus().name(), application.getId()));
                }

                day = day.plusDays(1);
            }
        }

        return absences;
    }


    private List<DayAbsence> getSickNotes(DateMidnight start, DateMidnight end, Person person) {

        List<DayAbsence> absences = new ArrayList<>();

        List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, start, end)
                .stream()
                .filter(SickNote::isActive)
                .collect(Collectors.toList());

        for (SickNote sickNote : sickNotes) {
            DateMidnight startDate = sickNote.getStartDate();
            DateMidnight endDate = sickNote.getEndDate();

            DateMidnight day = startDate;

            while (!day.isAfter(endDate)) {
                if (!day.isBefore(start) && !day.isAfter(end)) {
                    absences.add(new DayAbsence(day, sickNote.getDayLength(), DayAbsence.Type.SICK_NOTE, "ACTIVE",
                            sickNote.getId()));
                }

                day = day.plusDays(1);
            }
        }

        return absences;
    }
}
