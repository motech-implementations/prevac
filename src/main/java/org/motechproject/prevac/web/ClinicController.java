package org.motechproject.prevac.web;

import org.motechproject.prevac.domain.Clinic;
import org.motechproject.prevac.service.ClinicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller("/clinics")
public class ClinicController {

    private static final List<String> CLINIC_FIELDS = new ArrayList<>(Arrays.asList("Site Id", "Location", "Max Capacity By Day",
            "Max Screening Visits", "Max Prime Visits", "Max Booster Visits", "Amount of Rooms", "Max Prime First Follow Up Visits",
            "Max Prime Second Follow Up Visits", "Max Booster First Follow Up Visits", "Max Booster Second Follow Up Visits",
            "Max Booster Third Follow Up Visits", "Max First Long Term Follow Up Visits", "Max Second Long Term Follow Up Visits",
            "Max Third Long Term Follow Up Visits", "Max Fourth Long Term Follow Up Visits", "Max Fifth Long Term Follow Up Visits",
            "Max Sixth Long Term Follow Up Visits", "Max Seventh Long Term Follow Up Visits", "Max Third Vaccination Visits",
            "Max First Post Third Vaccination Visits", "Max Second Post Third Vaccination Visits", "Max Third Post Third Vaccination Visits",
            "Max Fourth Post Third Vaccination Visits", "Max Fifth Post Third Vaccination Visits"));

    @Autowired
    private ClinicService clinicService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    private List<Clinic> getSites() {
        return clinicService.getClinics();
    }

    @RequestMapping(value = "/clinicFields", method = RequestMethod.GET)
    @ResponseBody
    private List<String> getClinicFields() {
        return CLINIC_FIELDS;
    }
}
