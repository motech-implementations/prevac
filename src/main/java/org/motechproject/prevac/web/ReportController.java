package org.motechproject.prevac.web;

import org.motechproject.mds.dto.LookupDto;
import org.motechproject.prevac.constants.PrevacConstants;
import org.motechproject.prevac.domain.Clinic;
import org.motechproject.prevac.dto.CapacityReportDto;
import org.motechproject.prevac.exception.PrevacLookupException;
import org.motechproject.prevac.service.LookupService;
import org.motechproject.prevac.service.ReportService;
import org.motechproject.prevac.web.domain.GridSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

@Controller
@PreAuthorize(PrevacConstants.HAS_REPORTS_TAB_ROLE)
public class ReportController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    @Autowired
    private LookupService lookupService;

    @RequestMapping(value = "getCapacityReports", method = RequestMethod.GET)
    @ResponseBody
    public List<CapacityReportDto> getCapacityReports(GridSettings settings) {
        return reportService.generateCapacityReports(settings);
    }

    @RequestMapping(value = "/getLookupsForCapacityReport", method = RequestMethod.GET)
    @ResponseBody
    public List<LookupDto> getLookupsForScreening() {
        List<LookupDto> ret = new ArrayList<>();
        List<LookupDto> availableLookups;
        try {
            availableLookups = lookupService.getAvailableLookups(Clinic.class.getName());
        } catch (PrevacLookupException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
        List<String> lookupList = PrevacConstants.AVAILABLE_LOOKUPS_FOR_CAPACITY_REPORT;
        for (LookupDto lookupDto : availableLookups) {
            if (lookupList.contains(lookupDto.getLookupName())) {
                ret.add(lookupDto);
            }
        }
        return ret;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception e) {
        LOGGER.error(e.getMessage(), e);
        return e.getMessage();
    }
}
