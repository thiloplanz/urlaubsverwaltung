package org.synyx.urlaubsverwaltung.absence.api;

import de.jollyday.Holiday;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.api.ResponseWrapper;
import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceMarker;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.AbsenceType;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.holiday.api.PublicHolidayResponse;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

@RestControllerAdviceMarker
@Api("Absences: Get all absences for a certain period")
@RestController("restApiAbsenceController")
@RequestMapping("/api")
public class AbsenceApiController {

    private final PersonService personService;
    private final ApplicationService applicationService;
    private final SickNoteService sickNoteService;

    @Autowired
    public AbsenceApiController(PersonService personService, ApplicationService applicationService,
                                SickNoteService sickNoteService) {

        this.personService = personService;
        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
    }

    @Autowired
    private PublicHolidaysService publicHolidayService;

    @Autowired
    private WorkingTimeService workingTimeService;

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation(
        value = "Get all absences for a certain period and person",
        notes = "Get all absences for a certain period and person"
    )
    @GetMapping(value="/absences", params="person")
    @PreAuthorize(IS_BOSS_OR_OFFICE +
        " or @userApiMethodSecurity.isSamePersonId(authentication, #personId)" +
        " or @userApiMethodSecurity.isInDepartmentOfDepartmentHead(authentication, #personId)")
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

        final Optional<Person> optionalPerson = personService.getPersonByID(personId);

        if (optionalPerson.isEmpty()) {
            throw new IllegalArgumentException("No person found for ID=" + personId);
        }

        final List<DayAbsence> absences = new ArrayList<>();
        final Person person = optionalPerson.get();

        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = getStartDate(year, Optional.ofNullable(month));
            endDate = getEndDate(year, Optional.ofNullable(month));
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(exception.getMessage());
        }

        if (type == null || DayAbsence.Type.valueOf(type).equals(DayAbsence.Type.VACATION)) {
            absences.addAll(getVacations(startDate, endDate, person));
        }

        if (type == null || DayAbsence.Type.valueOf(type).equals(DayAbsence.Type.SICK_NOTE)) {
            absences.addAll(getSickNotes(startDate, endDate, person));
        }

        return new ResponseWrapper<>(new DayAbsenceList(absences));
    }

    private static LocalDate getStartDate(String year, Optional<String> optionalMonth) {
        return optionalMonth.map(s -> DateUtil.getFirstDayOfMonth(parseInt(year), parseInt(s)))
            .orElseGet(() -> DateUtil.getFirstDayOfYear(parseInt(year)));
    }

    private static LocalDate getEndDate(String year, Optional<String> optionalMonth) {
        return optionalMonth.map(s -> DateUtil.getLastDayOfMonth(parseInt(year), parseInt(s)))
            .orElseGet(() -> DateUtil.getLastDayOfYear(parseInt(year)));
    }

    private List<DayAbsence> getVacations(LocalDate start, LocalDate end, Person person) {

        List<DayAbsence> absences = new ArrayList<>();

        List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(start, end,
            person)
            .stream()
            .filter(application ->
                application.hasStatus(WAITING)
                    || application.hasStatus(TEMPORARY_ALLOWED)
                    || application.hasStatus(ALLOWED))
            .collect(toList());

        for (Application application : applications) {
            LocalDate startDate = application.getStartDate();
            LocalDate endDate = application.getEndDate();

            LocalDate day = startDate;

            while (!day.isAfter(endDate)) {
                if (!day.isBefore(start) && !day.isAfter(end)) {
                    absences.add(new DayAbsence(day, application.getDayLength().getDuration(), application.getDayLength().toString(), application.getVacationType().getCategory(),
                        application.getStatus().name(), application.getId()));
                }

                day = day.plusDays(1);
            }
        }

        return absences;
    }


    private List<DayAbsence> getSickNotes(LocalDate start, LocalDate end, Person person) {

        List<DayAbsence> absences = new ArrayList<>();

        List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, start, end)
            .stream()
            .filter(SickNote::isActive)
            .collect(toList());

        for (SickNote sickNote : sickNotes) {
            LocalDate startDate = sickNote.getStartDate();
            LocalDate endDate = sickNote.getEndDate();

            LocalDate day = startDate;

            while (!day.isAfter(endDate)) {
                if (!day.isBefore(start) && !day.isAfter(end)) {
                    absences.add(new DayAbsence(day, sickNote.getDayLength().getDuration(),
                        sickNote.getDayLength().toString(), sickNote.getSickNoteType().getCategory(), "ACTIVE",
                        sickNote.getId()));
                }

                day = day.plusDays(1);
            }
        }

        return absences;
    }

    /** YADOS: This is a bit of facade method that combines the results of both /absences and /holidays
     * for all department members (to reduce the number of HTTP requests when showing the overview
     * calendar)
     */

    @ApiOperation(
        value = "Get all absences and public holidays for a certain period and department",
        notes = "Get all absences and public holidays for a certain period and department"
    )
    @GetMapping(value = "/absences", params = "department")
    public ResponseWrapper<DepartmentAbsences> departmentVacations(
        @ApiParam(value = "Year to get the absences for")
        @RequestParam("year")
            String year,
        @ApiParam(value = "Month of year to get the absences for")
        @RequestParam(value = "month", required = false)
            String month,
        @ApiParam(value = "ID of the department")
        @RequestParam("department")
            Integer departmentId) {

        boolean hasYear = StringUtils.hasText(year);
        boolean hasMonth = StringUtils.hasText(month);

        if (hasYear && departmentId != null) {
            try {
                Optional<Department> deptOptional = departmentService.getDepartmentById(departmentId);

                if (!deptOptional.isPresent()) {
                    return new ResponseWrapper<>(new DepartmentAbsences(null, null, null));
                }

                LocalDate periodStart;
                LocalDate periodEnd;

                if (hasMonth) {
                    periodStart = DateUtil.getFirstDayOfMonth(Integer.parseInt(year), Integer.parseInt(month));
                    periodEnd = DateUtil.getLastDayOfMonth(Integer.parseInt(year), Integer.parseInt(month));
                } else {
                    periodStart = DateUtil.getFirstDayOfYear(Integer.parseInt(year));
                    periodEnd = DateUtil.getLastDayOfYear(Integer.parseInt(year));
                }

                List<Person> persons = deptOptional.map(Department::getMembers).orElse(Collections.emptyList());

                Map<Integer, List<DayAbsence>> absenceMap = new HashMap<>();
                Map<Integer, String> calendars = new HashMap<>();
                Map<String, List<PublicHolidayResponse>> catalog = new HashMap<>();

                for (Person person : persons) {

                    List<DayAbsence> absences = new ArrayList<>();
                    absences.addAll(getVacations(periodStart, periodEnd, person));
                    absences.addAll(getSickNotes(periodStart, periodEnd, person));
                    absenceMap.put(person.getId(), absences);
                    // A person's federal state might have changed during the period. We use the one
                    // as of periodEnd. Since this is used for display only and not vacation days calculation
                    // (and unlikely anyway) that should not be a problem.
                    FederalState state = workingTimeService.getFederalStateForPerson(person, periodEnd);
                    calendars.put(person.getId(), state.name());
                    if (!catalog.containsKey(state.name())) {
                        Collection<Holiday> holidays =
                            hasMonth ? publicHolidayService.getHolidays(periodStart.getYear(), periodStart.getMonthValue(), state)
                                : publicHolidayService.getHolidays(periodStart.getYear(), state);

                        catalog.put(state.name(), holidays.stream().map(holiday ->
                            new PublicHolidayResponse(holiday,
                                publicHolidayService.getWorkingDurationOfDate(holiday.getDate(),
                                    state), publicHolidayService.getAbsenceTypeOfDate(holiday.getDate(), state).name()))
                            .collect(Collectors.toList()));

                    }
                }


                return new ResponseWrapper<>(new DepartmentAbsences(catalog, absenceMap, calendars));
            } catch (NumberFormatException ex) {
                return new ResponseWrapper<>(new DepartmentAbsences(null, null, null));
            }
        }

        return new ResponseWrapper<>(new DepartmentAbsences(null, null, null));
    }


    private class DepartmentAbsences {

        private final Map<String, List<PublicHolidayResponse>> publicHolidays;

        /** map personId => absences */
        private final Map<Integer, List<DayAbsence>> personAbsences;

        /** map personId => key into publicHolidays map */
        private final Map<Integer, String> personPublicHolidays;

        public DepartmentAbsences(Map<String, List<PublicHolidayResponse>> publicHolidays, Map<Integer, List<DayAbsence>> personAbsences, Map<Integer, String> personPublicHolidays) {

            this.publicHolidays = publicHolidays;
            this.personAbsences = personAbsences;
            this.personPublicHolidays = personPublicHolidays;
        }

        public Map<String, List<PublicHolidayResponse>> getPublicHolidays() {

            return publicHolidays;
        }

        public Map<Integer, List<DayAbsence>> getPersonAbsences() {

            return personAbsences;
        }

        public Map<Integer, String> getPersonPublicHolidays() {

            return personPublicHolidays;
        }
    }


}
