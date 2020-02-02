package org.synyx.urlaubsverwaltung.sicknote.statistics.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatistics;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatisticsService;

import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;


/**
 * Controller for statistics of sick notes resp. sick days.
 */
@Controller
@RequestMapping("/web")
public class SickNoteStatisticsViewController {

    private final SickNoteStatisticsService statisticsService;

    @Autowired
    public SickNoteStatisticsViewController(SickNoteStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping("/sicknote/statistics")
    public String sickNotesStatistics(@RequestParam(value = "year", required = false) Integer requestedYear,
                                      Model model) {

        int year = requestedYear == null ? ZonedDateTime.now(UTC).getYear() : requestedYear;
        SickNoteStatistics statistics = statisticsService.createStatistics(year);

        model.addAttribute("statistics", statistics);

        return "sicknote/sick_notes_statistics";
    }
}
