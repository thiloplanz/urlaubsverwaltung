package org.synyx.urlaubsverwaltung.core.statistics;

import liquibase.util.csv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.statistics.ApplicationForLeaveStatistics;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

@Service
public class ApplicationForLeaveStatisticsCsvExportServiceImpl implements ApplicationForLeaveStatisticsCsvExportService {

    private static final Locale LOCALE = Locale.GERMAN;
    private static final String DATE_FORMAT = "ddMMyyyy";

    // made fields package private to enable testing
    // normally it would be good to use private fields
    // and constructor injection, but at the moment this
    // is not possible in combination with the used spring
    // version 1.4.2.RELEASE and the spring version is defined
    // by the usage of JSPs (older spring versions does not
    // support JSPs)
    // So this is a workaround that should be removed as soon
    // as the spring-version is updated.
    @Autowired
    MessageSource messageSource;

    @Autowired
    VacationTypeService vacationTypeService;

    @Override
    public void writeStatistics(FilterPeriod period, List<ApplicationForLeaveStatistics> statistics, CSVWriter csvWriter) {
        final String[] csvHeader = { getTranslation("person.data.firstName", "Vorname"),
                getTranslation("person.data.lastName", "Nachname"), "",
                getTranslation("applications.statistics.allowed", "genehmigt"),
                getTranslation("applications.statistics.waiting", "noch nicht genehmigt"),
                getTranslation("applications.statistics.left", "verbleibend") + " (" + period.getStartDate().getYear() + ")", "" };

        final String[] csvSubHeader = { "", "", "", "", "", getTranslation("duration.vacationDays", "Urlaubstage"),
                getTranslation("duration.overtime", "Überstunden") };

        String headerNote = getTranslation("absence.period", "Zeitraum") + ": " + period.getStartDateAsString() + " - "
                + period.getEndDateAsString();

        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(LOCALE);
        DecimalFormatSymbols newSymbols = new DecimalFormatSymbols(LOCALE);
        newSymbols.setDecimalSeparator(',');
        newSymbols.setGroupingSeparator('.');
        decimalFormat.setDecimalFormatSymbols(newSymbols);

        csvWriter.writeNext(new String[] { headerNote });
        csvWriter.writeNext(csvHeader);
        csvWriter.writeNext(csvSubHeader);

        String translatedTextTotal = getTranslation("applications.statistics.total", "gesamt");

        for (ApplicationForLeaveStatistics applicationForLeaveStatistics : statistics) {

            String[] csvRow = new String[csvHeader.length];

            csvRow[0] = applicationForLeaveStatistics.getPerson().getFirstName();
            csvRow[1] = applicationForLeaveStatistics.getPerson().getLastName();
            csvRow[2] = translatedTextTotal;
            csvRow[3] = decimalFormat.format(applicationForLeaveStatistics.getTotalAllowedVacationDays());
            csvRow[4] = decimalFormat.format(applicationForLeaveStatistics.getTotalWaitingVacationDays());
            csvRow[5] = decimalFormat.format(applicationForLeaveStatistics.getLeftVacationDays());
            csvRow[6] = decimalFormat.format(applicationForLeaveStatistics.getLeftOvertime());

            csvWriter.writeNext(csvRow);

            for (VacationType type : vacationTypeService.getVacationTypes()) {

                String[] csvRowVacationTypes = new String[csvHeader.length];

                csvRowVacationTypes[2] = type.getDisplayName();
                csvRowVacationTypes[3] = decimalFormat
                        .format(applicationForLeaveStatistics.getAllowedVacationDays().get(type));
                csvRowVacationTypes[4] = decimalFormat
                        .format(applicationForLeaveStatistics.getWaitingVacationDays().get(type));

                csvWriter.writeNext(csvRowVacationTypes);
            }
        }

    }

    @Override
    public String getFileName(FilterPeriod period) {
        return String.format("%s_%s_%s.csv",
                getTranslation("applications.statistics", "Statistik"),
                period.getStartDate().toString(DATE_FORMAT),
                period.getEndDate().toString(DATE_FORMAT));
    }

    private String getTranslation(String key, Object... args) {

        return messageSource.getMessage(key, args, LOCALE);
    }
}
