package org.synyx.urlaubsverwaltung.absence.api;

import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 * Represents an absence for a day.
 */
public class DayAbsence {

    public enum Type {

        VACATION,
        SICK_NOTE
    }

    private final String date;
    private final BigDecimal dayLength;
    private final String absencePeriodName;
    private final String type;
    private final String status;
    private final String href;
    private final String category;

    public DayAbsence(LocalDate date, BigDecimal dayLength, String absencePeriodName, VacationCategory category, String status, Integer id) {

        this.date = date.format(DateTimeFormatter.ofPattern(RestApiDateFormat.DATE_PATTERN));
        this.dayLength = dayLength;
        this.absencePeriodName = absencePeriodName;
        this.type = Type.VACATION.name();
        this.status = status;
        this.href = id == null ? "" : id.toString();
        this.category = category.name();
    }

    public DayAbsence(LocalDate date, BigDecimal dayLength, String absencePeriodName, SickNoteCategory category, String status, Integer id) {

        this.date = date.format(DateTimeFormatter.ofPattern(RestApiDateFormat.DATE_PATTERN));
        this.dayLength = dayLength;
        this.absencePeriodName = absencePeriodName;
        this.type = Type.SICK_NOTE.name();
        this.status = status;
        this.href = id == null ? "" : id.toString();
        this.category = category.name();
    }

    public String getDate() {

        return date;
    }


    public BigDecimal getDayLength() {

        return dayLength;
    }


    public String getAbsencePeriodName() {
        return absencePeriodName;
    }


    public String getType() {

        return type;
    }


    public String getStatus() {

        return status;
    }


    public String getHref() {

        return href;
    }

    public String getCategory() {

        return category;
    }

}
