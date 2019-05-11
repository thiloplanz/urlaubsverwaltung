package org.synyx.urlaubsverwaltung.availability.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
class VacationAbsenceProvider extends AbstractTimedAbsenceProvider {

    private final ApplicationService applicationService;

    @Autowired
    VacationAbsenceProvider(ApplicationService applicationService) {

        super(null);

        this.applicationService = applicationService;
    }

    @Override
    TimedAbsenceSpans addAbsence(TimedAbsenceSpans knownAbsences, Person person, LocalDate date) {

        Optional<TimedAbsence> vacationAbsence = checkForVacation(date, person);

        if (vacationAbsence.isPresent()) {
            List<TimedAbsence> knownAbsencesList = knownAbsences.getAbsencesList();
            knownAbsencesList.add(vacationAbsence.get());

            return new TimedAbsenceSpans(knownAbsencesList);
        }

        return knownAbsences;
    }


    @Override
    boolean isLastPriorityProvider() {

        return true;
    }


    private Optional<TimedAbsence> checkForVacation(LocalDate date, Person person) {

        List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(date, date,
                    person)
                .stream()
                .filter(application ->
                            application.hasStatus(ApplicationStatus.WAITING)
                            || application.hasStatus(ApplicationStatus.TEMPORARY_ALLOWED)
                            || application.hasStatus(ApplicationStatus.ALLOWED))
                .collect(Collectors.toList());

        if (applications.isEmpty()) {
            return Optional.empty();
        }

        Application application = applications.get(0);

        return Optional.of(new TimedAbsence(application.getDayLength(), TimedAbsence.Type.VACATION));
    }
}
