package org.motechproject.prevac.web;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.motechproject.prevac.constants.PrevacConstants;
import org.motechproject.prevac.domain.Visit;
import org.motechproject.prevac.domain.enums.VisitType;
import org.motechproject.prevac.exception.VisitScheduleException;
import org.motechproject.prevac.repository.VisitBookingDetailsDataService;
import org.motechproject.prevac.service.VisitScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/schedule")
@PreAuthorize(PrevacConstants.HAS_CLINIC_VISIT_SCHEDULE_TAB_ROLE)
public class VisitScheduleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VisitScheduleController.class);

    @Autowired
    private VisitBookingDetailsDataService visitBookingDetailsDataService;

    @Autowired
    private VisitScheduleService visitScheduleService;

    @RequestMapping(value = "/getScreeningVisits", method = RequestMethod.GET)
    @ResponseBody
    public List<Visit> getScreeningVisits() {
        return visitBookingDetailsDataService.findByVisitTypeAndActualDateLess(VisitType.SCREENING, LocalDate.now());
    }

    @RequestMapping(value = "/getPrimeVacDate/{subjectId}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getPrimeVacDate(@PathVariable String subjectId) {
        return visitScheduleService.getPrimeVaccinationDateAndDateRange(subjectId);
    }

    @RequestMapping(value = "/getPlannedDates/{subjectId}/{plannedDate}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getPlannedDates(@PathVariable String subjectId, @PathVariable String plannedDate) {
        LocalDate date = null;

        if (StringUtils.isBlank(subjectId)) {
            return new HashMap<>();
        }

        if (StringUtils.isNotBlank(plannedDate)) {
            date = LocalDate.parse(plannedDate, DateTimeFormat.forPattern(PrevacConstants.SIMPLE_DATE_FORMAT));
        }

        return visitScheduleService.calculatePlannedVisitDates(subjectId, date);
    }

    @RequestMapping(value = "/savePlannedDates/{subjectId}/{plannedDate}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> savePlannedDates(@PathVariable String subjectId, @PathVariable String plannedDate) {
        LocalDate date;

        if (StringUtils.isBlank(subjectId)) {
            return new ResponseEntity<>("Cannot save Planned Dates, because Participant Id is empty", HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isNotBlank(plannedDate)) {
            date = LocalDate.parse(plannedDate, DateTimeFormat.forPattern(PrevacConstants.SIMPLE_DATE_FORMAT));
        } else {
            return new ResponseEntity<>("Cannot save Planned Dates, because Prime Vaccination Date is empty", HttpStatus.BAD_REQUEST);
        }

        visitScheduleService.savePlannedVisitDates(subjectId, date);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ExceptionHandler(VisitScheduleException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleBadRequest(Exception e) {
        return e.getMessage();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception e) {
        LOGGER.error(e.getMessage(), e);
        return e.getMessage();
    }
}
