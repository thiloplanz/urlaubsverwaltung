package org.synyx.urlaubsverwaltung.workingtime;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.util.DateFormat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * Service for handling {@link WorkingTime} entities.
 */
@Service
@Transactional
public class WorkingTimeService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final WorkingTimeDAO workingTimeDAO;
    private final SettingsService settingsService;

    @Autowired
    public WorkingTimeService(WorkingTimeDAO workingTimeDAO, SettingsService settingsService) {

        this.workingTimeDAO = workingTimeDAO;
        this.settingsService = settingsService;
    }

    public void touch(List<Integer> workingDays, Optional<FederalState> federalState, LocalDate validFrom,
                      Person person) {

        WorkingTime workingTime = workingTimeDAO.findByPersonAndValidityDate(person, validFrom);

        /*
         * create a new WorkingTime object if no one existent for the given person and date
         */
        if (workingTime == null) {
            workingTime = new WorkingTime();
            workingTime.setPerson(person);
            workingTime.setValidFrom(validFrom);
        }

        /*
         * else just change the working days of the current working time object
         */
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        if (federalState.isPresent()) {
            workingTime.setFederalStateOverride(federalState.get());
        } else {
            // reset federal state override, use system default federal state for this user
            workingTime.setFederalStateOverride(null);
        }

        workingTimeDAO.save(workingTime);
    }


    public List<WorkingTime> getByPerson(Person person) {

        return workingTimeDAO.findByPerson(person);
    }


    public Optional<WorkingTime> getByPersonAndValidityDateEqualsOrMinorDate(Person person, LocalDate date) {

        return Optional.ofNullable(workingTimeDAO.findByPersonAndValidityDateEqualsOrMinorDate(person, date));
    }


    public Optional<WorkingTime> getCurrentOne(Person person) {

        return Optional.ofNullable(workingTimeDAO.findLastOneByPerson(person));
    }


    public FederalState getFederalStateForPerson(Person person, LocalDate date) {

        Optional<WorkingTime> optionalWorkingTime = getByPersonAndValidityDateEqualsOrMinorDate(person, date);

        if (!optionalWorkingTime.isPresent()) {
            LOG.debug("No working time found for user '{}' equals or minor {}, using system federal state as fallback",
                    person.getLoginName(), date.format(DateTimeFormatter.ofPattern(DateFormat.PATTERN)));

            return getSystemDefaultFederalState();
        }

        return getFederalState(optionalWorkingTime.get());
    }


    private FederalState getFederalState(WorkingTime workingTime) {

        Optional<FederalState> optionalFederalStateOverride = workingTime.getFederalStateOverride();
        return optionalFederalStateOverride.orElseGet(this::getSystemDefaultFederalState);
    }

    private FederalState getSystemDefaultFederalState() {

        return settingsService.getSettings().getWorkingTimeSettings().getFederalState();
    }
}
