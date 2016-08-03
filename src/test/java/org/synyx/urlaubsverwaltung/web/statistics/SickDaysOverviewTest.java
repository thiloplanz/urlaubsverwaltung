package org.synyx.urlaubsverwaltung.web.statistics;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteStatus;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;

import java.math.BigDecimal;

import java.util.Arrays;
import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickDaysOverviewTest {

    private WorkDaysService calendarService;

    @Before
    public void setUp() {

        calendarService = Mockito.mock(WorkDaysService.class);
    }


    @Test
    public void ensureGeneratesCorrectSickDaysOverview() {

        SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(SickNoteCategory.SICK_NOTE);
        sickNoteType.setDisplayName("Krankmeldung");

        SickNoteType sickNoteTypeChild = new SickNoteType();
        sickNoteTypeChild.setCategory(SickNoteCategory.SICK_NOTE_CHILD);
        sickNoteTypeChild.setDisplayName("Kind-Krankmeldung");

        SickNote sickNoteWithoutAUB = new SickNote();
        sickNoteWithoutAUB.setSickNoteType(sickNoteType);
        sickNoteWithoutAUB.setStatus(SickNoteStatus.ACTIVE);
        sickNoteWithoutAUB.setStartDate(new DateMidnight(2014, 10, 13));
        sickNoteWithoutAUB.setEndDate(new DateMidnight(2014, 10, 13));

        SickNote sickNoteWithAUB = new SickNote();
        sickNoteWithAUB.setSickNoteType(sickNoteType);
        sickNoteWithAUB.setStatus(SickNoteStatus.ACTIVE);
        sickNoteWithAUB.setStartDate(new DateMidnight(2014, 10, 14));
        sickNoteWithAUB.setEndDate(new DateMidnight(2014, 10, 14));
        sickNoteWithAUB.setAubStartDate(new DateMidnight(2014, 10, 14));
        sickNoteWithAUB.setAubEndDate(new DateMidnight(2014, 10, 14));

        SickNote childSickNoteWithoutAUB = new SickNote();
        childSickNoteWithoutAUB.setSickNoteType(sickNoteTypeChild);
        childSickNoteWithoutAUB.setStatus(SickNoteStatus.ACTIVE);
        childSickNoteWithoutAUB.setStartDate(new DateMidnight(2014, 10, 15));
        childSickNoteWithoutAUB.setEndDate(new DateMidnight(2014, 10, 15));

        SickNote childSickNoteWithAUB = new SickNote();
        childSickNoteWithAUB.setSickNoteType(sickNoteTypeChild);
        childSickNoteWithAUB.setStatus(SickNoteStatus.ACTIVE);
        childSickNoteWithAUB.setStartDate(new DateMidnight(2014, 10, 16));
        childSickNoteWithAUB.setEndDate(new DateMidnight(2014, 10, 16));
        childSickNoteWithAUB.setAubStartDate(new DateMidnight(2014, 10, 16));
        childSickNoteWithAUB.setAubEndDate(new DateMidnight(2014, 10, 16));

        SickNote inactiveSickNote = new SickNote();
        inactiveSickNote.setSickNoteType(sickNoteTypeChild);
        inactiveSickNote.setStatus(SickNoteStatus.CANCELLED);
        inactiveSickNote.setStartDate(new DateMidnight(2014, 10, 17));
        inactiveSickNote.setEndDate(new DateMidnight(2014, 10, 17));

        SickNote inactiveChildSickNote = new SickNote();
        inactiveChildSickNote.setSickNoteType(sickNoteTypeChild);
        inactiveChildSickNote.setStatus(SickNoteStatus.CANCELLED);
        inactiveChildSickNote.setStartDate(new DateMidnight(2014, 10, 18));
        inactiveChildSickNote.setEndDate(new DateMidnight(2014, 10, 18));

        List<SickNote> sickNotes = Arrays.asList(sickNoteWithoutAUB, sickNoteWithAUB, childSickNoteWithoutAUB,
                childSickNoteWithAUB, inactiveSickNote, inactiveChildSickNote);

        // just return 1 day for each sick note
        Mockito.when(calendarService.getWorkDays(Mockito.any(DayLength.class), Mockito.any(DateMidnight.class),
                    Mockito.any(DateMidnight.class), Mockito.any(Person.class)))
            .thenReturn(BigDecimal.ONE);

        SickDaysOverview sickDaysOverview = new SickDaysOverview(sickNotes, calendarService);

        SickDays sickDays = sickDaysOverview.getSickDays();
        Assert.assertNotNull("Should not be null", sickDays.getDays());
        Assert.assertEquals("Wrong number of sick days without AUB", new BigDecimal("2"),
            sickDays.getDays().get("TOTAL"));
        Assert.assertEquals("Wrong number of sick days with AUB", BigDecimal.ONE, sickDays.getDays().get("WITH_AUB"));

        SickDays childSickDays = sickDaysOverview.getChildSickDays();
        Assert.assertNotNull("Should not be null", childSickDays.getDays());
        Assert.assertEquals("Wrong number of child sick days without AUB", new BigDecimal("2"),
            childSickDays.getDays().get("TOTAL"));
        Assert.assertEquals("Wrong number of child sick days with AUB", BigDecimal.ONE,
            childSickDays.getDays().get("WITH_AUB"));
    }
}
