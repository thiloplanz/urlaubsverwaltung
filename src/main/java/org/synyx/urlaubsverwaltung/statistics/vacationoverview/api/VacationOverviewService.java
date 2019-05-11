package org.synyx.urlaubsverwaltung.statistics.vacationoverview.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.holiday.DayOfMonth;
import org.synyx.urlaubsverwaltung.holiday.VacationOverview;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.api.PersonResponse;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.synyx.urlaubsverwaltung.holiday.DayOfMonth.TypeOfDay.WEEKEND;
import static org.synyx.urlaubsverwaltung.holiday.DayOfMonth.TypeOfDay.WORKDAY;

@Component
public class VacationOverviewService {

    private final DepartmentService departmentService;
    private final WorkingTimeService workingTimeService;
    private final PublicHolidaysService publicHolidayService;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    public VacationOverviewService(DepartmentService departmentService,
                                   WorkingTimeService workingTimeService,
                                   PublicHolidaysService publicHolidayService) {

        this.departmentService = departmentService;
        this.workingTimeService = workingTimeService;
        this.publicHolidayService = publicHolidayService;
    }

    public List<VacationOverview> getVacationOverviews(String selectedDepartment,
                                                       Integer selectedYear,
                                                       Integer selectedMonth) {
        List<VacationOverview> holidayOverviewList = new ArrayList<>();

        Department department = getDepartmentByName(selectedDepartment);

        if (department != null) {

            for (Person person : department.getMembers()) {

                LocalDate date = LocalDate.now(UTC);
                int year = selectedYear != null ? selectedYear : date.getYear();
                int month = selectedMonth != null ? selectedMonth : date.getMonthValue();
                LocalDate lastDay = DateUtil.getLastDayOfMonth(year, month);

                VacationOverview holidayOverview = getVacationOverview(person);

                for (int i = 1; i <= lastDay.getDayOfMonth(); i++) {

                    DayOfMonth dayOfMonth = new DayOfMonth();
                    LocalDate currentDay = LocalDate.of(year, month, i);
                    dayOfMonth.setDayText(currentDay.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
                    dayOfMonth.setDayNumber(i);

                    dayOfMonth.setTypeOfDay(getTypeOfDay(person, currentDay));
                    holidayOverview.getDays().add(dayOfMonth);
                }
                holidayOverviewList.add(holidayOverview);
            }
        }
        return holidayOverviewList;
    }

    private DayOfMonth.TypeOfDay getTypeOfDay(Person person, LocalDate currentDay) {
        DayOfMonth.TypeOfDay typeOfDay;

        FederalState state = workingTimeService.getFederalStateForPerson(person, currentDay);
        if (DateUtil.isWorkDay(currentDay)
                && (publicHolidayService.getWorkingDurationOfDate(currentDay, state).longValue() > 0)) {

            typeOfDay = WORKDAY;
        } else {
            typeOfDay = WEEKEND;
        }
        return typeOfDay;
    }

    private Department getDepartmentByName(@RequestParam("selectedDepartment") String selectedDepartment) {
        Department department = null;
        for (Department item : departmentService.getAllDepartments()) {
            if (item.getName().equals(selectedDepartment)) {
                department = item;
                break;
            }
        }
        return department;
    }

    private VacationOverview getVacationOverview(Person person) {
        VacationOverview vacationOverview = new VacationOverview();
        vacationOverview.setDays(new ArrayList<>());
        vacationOverview.setPerson(new PersonResponse(person));
        vacationOverview.setPersonID(person.getId());
        return vacationOverview;
    }

}
