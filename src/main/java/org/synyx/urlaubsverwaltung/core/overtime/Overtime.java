package org.synyx.urlaubsverwaltung.core.overtime;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateMidnight;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.core.person.Person;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import java.math.BigDecimal;
import java.util.Date;


/**
 * Represents the overtime of a person for a certain period of time.
 *
 * @since  2.11.0
 */
@Entity
public class Overtime extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 67834589209309L;

    @ManyToOne
    private Person person;

    @Column(nullable = false)
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date startDate;

    @Column(nullable = false)
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;

    @Column(nullable = false)
    private BigDecimal hours;

    @Column(nullable = false)
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date lastModificationDate;

    Overtime() {

        // OK
    }


    public Overtime(Person person, DateMidnight startDate, DateMidnight endDate, BigDecimal numberOfHours) {

        Assert.notNull(person, "Person must be given.");
        Assert.notNull(startDate, "Start date must be given.");
        Assert.notNull(endDate, "End date must be given.");
        Assert.notNull(numberOfHours, "Number of hours must be given.");

        this.person = person;
        this.startDate = startDate.toDate();
        this.endDate = endDate.toDate();
        this.hours = numberOfHours;

        this.lastModificationDate = DateMidnight.now().toDate();
    }

    public Person getPerson() {

        return person;
    }


    public DateMidnight getStartDate() {

        if (startDate == null) {
            throw new IllegalStateException("Missing start date!");
        }

        return new DateMidnight(startDate.getTime());
    }


    public DateMidnight getEndDate() {

        if (endDate == null) {
            throw new IllegalStateException("Missing end date!");
        }

        return new DateMidnight(endDate.getTime());
    }


    public BigDecimal getHours() {

        return hours;
    }

    @Override
    public void setId(Integer id) { // NOSONAR - make it public instead of protected

        super.setId(id);
    }

    public void setPerson(Person person) {

        Assert.notNull(person, "Person must be given.");

        this.person = person;
    }


    public void setStartDate(DateMidnight startDate) {

        Assert.notNull(startDate, "Start date must be given.");

        this.startDate = startDate.toDate();
    }


    public void setEndDate(DateMidnight endDate) {

        Assert.notNull(endDate, "End date must be given.");

        this.endDate = endDate.toDate();
    }


    public void setHours(BigDecimal hours) {

        Assert.notNull(hours, "Hours must be given.");

        this.hours = hours;
    }


    public DateMidnight getLastModificationDate() {

        if (lastModificationDate == null) {
            throw new IllegalStateException("Missing last modification date!");
        }

        return new DateMidnight(lastModificationDate.getTime());
    }


    /**
     * Should be called whenever an overtime entity is updated.
     */
    public void onUpdate() {

        this.lastModificationDate = DateMidnight.now().toDate();
    }


    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) // NOSONAR - Formatting issue
            .append("id", getId())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("hours", getHours())
            .append("person", getPerson())
            .toString();
    }
}
