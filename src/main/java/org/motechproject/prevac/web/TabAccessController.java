package org.motechproject.prevac.web;

import static org.motechproject.prevac.constants.PrevacConstants.ADVANCED_SETTINGS_TAB_PERMISSION;
import static org.motechproject.prevac.constants.PrevacConstants.CLINIC_VISIT_SCHEDULE_TAB_PERMISSION;
import static org.motechproject.prevac.constants.PrevacConstants.PRIME_VAC_TAB_PERMISSION;
import static org.motechproject.prevac.constants.PrevacConstants.REPORTS_TAB_PERMISSION;
import static org.motechproject.prevac.constants.PrevacConstants.SCREENING_TAB_PERMISSION;
import static org.motechproject.prevac.constants.PrevacConstants.SUBJECTS_TAB_PERMISSION;
import static org.motechproject.prevac.constants.PrevacConstants.UNSCHEDULED_VISITS_TAB_PERMISSION;
import static org.motechproject.prevac.constants.PrevacConstants.VISIT_RESCHEDULE_TAB_PERMISSION;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TabAccessController {

    @RequestMapping(value = "/available/prevacTabs", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getAvailableTabs() {
        List<String> availableTabs = new ArrayList<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority(REPORTS_TAB_PERMISSION))) {
            availableTabs.add("reports");
        }

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority(SUBJECTS_TAB_PERMISSION))) {
            availableTabs.add("subjects");
        }

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority(SCREENING_TAB_PERMISSION))) {
            availableTabs.add("screening");
        }

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority(PRIME_VAC_TAB_PERMISSION))) {
            availableTabs.add("primeVaccination");
        }

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority(CLINIC_VISIT_SCHEDULE_TAB_PERMISSION))) {
            availableTabs.add("clinicVisitSchedule");
        }

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority(VISIT_RESCHEDULE_TAB_PERMISSION))) {
            availableTabs.add("reschedule");
        }

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority(UNSCHEDULED_VISITS_TAB_PERMISSION))) {
            availableTabs.add("unscheduledVisit");
        }

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority(ADVANCED_SETTINGS_TAB_PERMISSION))) {
            availableTabs.add("visitLimitation");
        }

        return availableTabs;
    }

}
