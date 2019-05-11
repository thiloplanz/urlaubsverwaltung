package org.synyx.urlaubsverwaltung.overview.calendar;

import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.person.api.PersonResponse;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;


public class VacationResponse {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(RestApiDateFormat.DATE_PATTERN);
    private String from;
    private String to;
    private BigDecimal dayLength;
    private PersonResponse person;
    private String type;
    private String status;

    public VacationResponse(Application application) {

        this.from = application.getStartDate().format(formatter);
        this.to = application.getEndDate().format(formatter);
        this.dayLength = application.getDayLength().getDuration();
        this.person = new PersonResponse(application.getPerson());
        this.status = application.getStatus().name();

        VacationType vacationType = application.getVacationType();
        this.type = vacationType.getCategory().toString();
    }

    public String getFrom() {

        return from;
    }


    public void setFrom(String from) {

        this.from = from;
    }


    public String getTo() {

        return to;
    }


    public void setTo(String to) {

        this.to = to;
    }


    public BigDecimal getDayLength() {

        return dayLength;
    }


    public void setDayLength(BigDecimal dayLength) {

        this.dayLength = dayLength;
    }


    public PersonResponse getPerson() {

        return person;
    }


    public void setPerson(PersonResponse person) {

        this.person = person;
    }


    public String getType() {

        return type;
    }


    public void setType(String type) {

        this.type = type;
    }

    public String getStatus() {

        return status;
    }


    public void setStatus(String status) {

        this.status = status;
    }
}
