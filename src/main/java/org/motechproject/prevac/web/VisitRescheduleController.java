package org.motechproject.prevac.web;

import org.motechproject.mds.dto.LookupDto;
import org.motechproject.prevac.constants.PrevacConstants;
import org.motechproject.prevac.domain.Visit;
import org.motechproject.prevac.dto.VisitRescheduleDto;
import org.motechproject.prevac.exception.PrevacLookupException;
import org.motechproject.prevac.exception.LimitationExceededException;
import org.motechproject.prevac.helper.DtoLookupHelper;
import org.motechproject.prevac.service.LookupService;
import org.motechproject.prevac.service.VisitRescheduleService;
import org.motechproject.prevac.web.domain.GridSettings;
import org.motechproject.prevac.web.domain.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@PreAuthorize(PrevacConstants.HAS_VISIT_RESCHEDULE_TAB_ROLE)
public class VisitRescheduleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VisitRescheduleController.class);

    @Autowired
    private VisitRescheduleService visitRescheduleService;

    @Autowired
    private LookupService lookupService;

    @RequestMapping("/visitReschedule")
    @ResponseBody
    public Records<VisitRescheduleDto> getVisitBookingDetails(GridSettings settings) throws IOException {
        return visitRescheduleService.getVisitsRecords(DtoLookupHelper.changeLookupForVisitReschedule(settings));
    }

    @RequestMapping(value = "/saveVisitReschedule/{ignoreLimitation}", method = RequestMethod.POST)
    @ResponseBody
    public Object saveVisitReschedule(@PathVariable Boolean ignoreLimitation,
                                      @RequestBody VisitRescheduleDto visitRescheduleDto) {
        try {
            return visitRescheduleService.saveVisitReschedule(visitRescheduleDto, ignoreLimitation);
        } catch (LimitationExceededException e) {
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/getLookupsForVisitReschedule", method = RequestMethod.GET)
    @ResponseBody
    public List<LookupDto> getLookupsForVisitReschedule() {
        List<LookupDto> ret = new ArrayList<>();
        List<LookupDto> availableLookups;
        try {
            availableLookups = lookupService.getAvailableLookups(Visit.class.getName());
        } catch (PrevacLookupException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
        List<String> lookupList = PrevacConstants.AVAILABLE_LOOKUPS_FOR_VISIT_RESCHEDULE;
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
