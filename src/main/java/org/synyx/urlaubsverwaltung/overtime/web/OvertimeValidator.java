package org.synyx.urlaubsverwaltung.overtime.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.settings.WorkingTimeSettings;
import org.synyx.urlaubsverwaltung.util.CalcUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;


/**
 * Validates overtime record.
 */
@Component
public class OvertimeValidator implements Validator {

    private static final int MAX_CHARS = 200;

    private static final String ERROR_MANDATORY = "error.entry.mandatory";
    private static final String ERROR_MAX_CHARS = "error.entry.tooManyChars";
    private static final String ERROR_INVALID_PERIOD = "error.entry.invalidPeriod";
    private static final String ERROR_MAX_OVERTIME = "overtime.data.numberOfHours.error.maxOvertime";
    private static final String ERROR_MIN_OVERTIME = "overtime.data.numberOfHours.error.minOvertime";
    private static final String ERROR_OVERTIME_DEACTIVATED = "overtime.record.error.deactivated";

    private static final String ATTRIBUTE_START_DATE = "startDate";
    private static final String ATTRIBUTE_END_DATE = "endDate";
    private static final String ATTRIBUTE_NUMBER_OF_HOURS = "numberOfHours";
    private static final String ATTRIBUTE_COMMENT = "comment";

    private final OvertimeService overtimeService;
    private final SettingsService settingsService;

    @Autowired
    public OvertimeValidator(OvertimeService overtimeService, SettingsService settingsService) {

        this.overtimeService = overtimeService;
        this.settingsService = settingsService;
    }

    @Override
    public boolean supports(Class<?> clazz) {

        return OvertimeForm.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {

        OvertimeForm overtimeForm = (OvertimeForm) target;

        WorkingTimeSettings settings = settingsService.getSettings().getWorkingTimeSettings();

        if (!settings.isOvertimeActive()) {
            errors.reject(ERROR_OVERTIME_DEACTIVATED);

            // if overtime management is deactivated, no need to execute further validation
            return;
        }

        validatePeriod(overtimeForm, errors);
        validateNumberOfHours(overtimeForm, errors);
        validateMaximumOvertimeNotReached(settings, overtimeForm, errors);
        validateComment(overtimeForm, errors);
    }


    private void validatePeriod(OvertimeForm overtimeForm, Errors errors) {

        LocalDate startDate = overtimeForm.getStartDate();
        LocalDate endDate = overtimeForm.getEndDate();

        validateDateNotNull(startDate, ATTRIBUTE_START_DATE, errors);
        validateDateNotNull(endDate, ATTRIBUTE_END_DATE, errors);

        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            errors.rejectValue(ATTRIBUTE_END_DATE, ERROR_INVALID_PERIOD);
        }
    }


    private void validateDateNotNull(LocalDate date, String field, Errors errors) {

        // may be that date field is null because of cast exception, than there is already a field error
        if (date == null && !errors.hasFieldErrors(field)) {
            errors.rejectValue(field, ERROR_MANDATORY);
        }
    }


    private void validateNumberOfHours(OvertimeForm overtimeForm, Errors errors) {

        BigDecimal numberOfHours = overtimeForm.getNumberOfHours();

        // may be that number of hours field is null because of cast exception, than there is already a field error
        if (numberOfHours == null && !errors.hasFieldErrors(ATTRIBUTE_NUMBER_OF_HOURS)) {
            errors.rejectValue(ATTRIBUTE_NUMBER_OF_HOURS, ERROR_MANDATORY);
        }
    }


    private void validateMaximumOvertimeNotReached(WorkingTimeSettings settings, OvertimeForm overtimeForm,
                                                   Errors errors) {

        BigDecimal numberOfHours = overtimeForm.getNumberOfHours();

        if (numberOfHours != null) {
            BigDecimal maximumOvertime = new BigDecimal(settings.getMaximumOvertime());
            BigDecimal minimumOvertime = new BigDecimal(settings.getMinimumOvertime());

            if (CalcUtil.isZero(maximumOvertime)) {
                errors.reject(ERROR_OVERTIME_DEACTIVATED);

                return;
            }

            BigDecimal leftOvertime = overtimeService.getLeftOvertimeForPerson(overtimeForm.getPerson());

            Integer overtimeRecordId = overtimeForm.getId();

            if (overtimeRecordId != null) {
                Optional<Overtime> overtimeRecordOptional = overtimeService.getOvertimeById(overtimeRecordId);

                if (overtimeRecordOptional.isPresent()) {
                    leftOvertime = leftOvertime.subtract(overtimeRecordOptional.get().getHours());
                }
            }

            // left overtime + overtime record must not be greater than maximum overtime
            if (leftOvertime.add(numberOfHours).compareTo(maximumOvertime) > 0) {
                errors.rejectValue(ATTRIBUTE_NUMBER_OF_HOURS, ERROR_MAX_OVERTIME, new Object[]{maximumOvertime},
                    null);
            }

            // left overtime + overtime record must be greater than minimum overtime
            // minimum overtime are missing hours (means negative)
            if (leftOvertime.add(numberOfHours).compareTo(minimumOvertime.negate()) < 0) {
                errors.rejectValue(ATTRIBUTE_NUMBER_OF_HOURS, ERROR_MIN_OVERTIME, new Object[]{minimumOvertime},
                    null);
            }
        }
    }


    private void validateComment(OvertimeForm overtimeForm, Errors errors) {

        String comment = overtimeForm.getComment();

        if (StringUtils.hasText(comment) && comment.length() > MAX_CHARS) {
            errors.rejectValue(ATTRIBUTE_COMMENT, ERROR_MAX_CHARS);
        }
    }
}
