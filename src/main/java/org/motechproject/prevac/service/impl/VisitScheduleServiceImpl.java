package org.motechproject.prevac.service.impl;

import org.joda.time.LocalDate;
import org.motechproject.prevac.constants.PrevacConstants;
import org.motechproject.prevac.domain.Subject;
import org.motechproject.prevac.domain.Visit;
import org.motechproject.prevac.domain.VisitScheduleOffset;
import org.motechproject.prevac.domain.enums.VisitType;
import org.motechproject.prevac.exception.VisitScheduleException;
import org.motechproject.prevac.repository.SubjectDataService;
import org.motechproject.prevac.repository.VisitBookingDetailsDataService;
import org.motechproject.prevac.service.ConfigService;
import org.motechproject.prevac.service.VisitScheduleOffsetService;
import org.motechproject.prevac.service.VisitScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("visitScheduleService")
public class VisitScheduleServiceImpl implements VisitScheduleService {

    @Autowired
    private SubjectDataService subjectDataService;

    @Autowired
    private VisitBookingDetailsDataService visitBookingDetailsDataService;

    @Autowired
    private VisitScheduleOffsetService visitScheduleOffsetService;

    @Autowired
    private ConfigService configService;

    @Override
    public Map<String, String> getPrimeVaccinationDateAndDateRange(String subjectId) {

        Subject subject = subjectDataService.findBySubjectId(subjectId);

        LocalDate primeVacDate = null;
        LocalDate earliestDate = null;
        LocalDate latestDate = null;

        if (subject != null) {
            Visit details = getPrimerVaccinationDetails(subject);

            if (subject.getPrimerVaccinationDate() != null) {
                primeVacDate = subject.getPrimerVaccinationDate();
            } else if (details != null) {
                primeVacDate = details.getBookingActualDate();
            }

            LocalDate screeningDate = getScreeningDate(subject);
            if (screeningDate != null) {
                if (!isFemaleChildBearingAge(details)) {
                    earliestDate = screeningDate.plusDays(PrevacConstants.EARLIEST_DATE);
                } else {
                    earliestDate = screeningDate.plusDays(PrevacConstants.EARLIEST_DATE_IF_FEMALE_CHILD_BEARING_AGE);
                }
                latestDate = screeningDate.plusDays(PrevacConstants.LATEST_DATE);
            }
        }

        Map<String, String> dates = new HashMap<>();
        dates.put("primeVacDate", primeVacDate != null ?
                primeVacDate.toString(PrevacConstants.SIMPLE_DATE_FORMAT) : "");
        dates.put("earliestDate", earliestDate != null ?
                earliestDate.toString(PrevacConstants.SIMPLE_DATE_FORMAT) : "");
        dates.put("latestDate", latestDate != null ?
                latestDate.toString(PrevacConstants.SIMPLE_DATE_FORMAT) : "");

        return dates;

    }

    @Override
    public Map<String, String> calculatePlannedVisitDates(String subjectId, LocalDate primeVaccinationDate) {
        Map<String, String> plannedDates = new HashMap<>();
        List<Visit> visits = visitBookingDetailsDataService.findBySubjectId(subjectId);

        if (visits == null || visits.isEmpty()) {
            throw new VisitScheduleException(String.format("Cannot save Planned Dates, because Participant with Id: %s has no Visits", subjectId));
        }

        Subject subject = visits.get(0).getSubject();

        if (subject.getPrimerVaccinationDate() == null) {
            for (Visit details : calculatePlannedDates(visits, primeVaccinationDate, subject.getStageId())) {
                if (VisitType.PRIME_VACCINATION_DAY.equals(details.getType())) {
                    plannedDates.put(details.getType().toString(), details.getBookingActualDate().toString(PrevacConstants.SIMPLE_DATE_FORMAT));
                } else {
                    plannedDates.put(details.getType().toString(), details.getBookingPlannedDate().toString(PrevacConstants.SIMPLE_DATE_FORMAT));
                }
            }
        } else {
            for (Visit visit : subject.getVisits()) {
                if (visit.getDateProjected() != null && !visit.getType().equals(VisitType.THIRD_LONG_TERM_FOLLOW_UP_VISIT)) {
                    plannedDates.put(visit.getType().toString(), visit.getDateProjected().toString(PrevacConstants.SIMPLE_DATE_FORMAT));
                }
            }
        }

        return plannedDates;
    }

    @Override
    public void savePlannedVisitDates(String subjectId, LocalDate primeVaccinationDate) {
        List<Visit> visits = visitBookingDetailsDataService.findBySubjectId(subjectId);

        if (visits == null || visits.isEmpty()) {
            throw new VisitScheduleException(String.format("Cannot save Planned Dates, because Participant with Id: %s has no Visits", subjectId));
        }

        Subject subject = visits.get(0).getSubject();

        if (subject.getPrimerVaccinationDate() != null) {
            throw new VisitScheduleException(String.format("Cannot save Planned Dates, because Participant with Id: %s has been vaccinated", subjectId));
        }

        for (Visit details : calculatePlannedDates(visits, primeVaccinationDate, subject.getStageId())) {
            visitBookingDetailsDataService.update(details);
        }
    }

    private List<Visit> calculatePlannedDates(List<Visit> visits, LocalDate primeVaccinationDate, Long stageId) {

        if (primeVaccinationDate == null) {
            throw new VisitScheduleException("Cannot calculate Planned Dates, because Prime Vaccination Date is empty");
        }

        Long actualStageId = getActualStageId(stageId);

        Map<VisitType, VisitScheduleOffset> offsetMap = visitScheduleOffsetService.getAsMapByStageId(actualStageId);

        if (offsetMap == null || offsetMap.isEmpty()) {
            throw new VisitScheduleException(String.format("Cannot calculate Planned Dates, because no Visit Schedule Offset found for stageId: %s",
                    stageId.toString()));
        }

        List<Visit> detailsList = new ArrayList<>();
        LocalDate screeningDate = null;
        Visit primeVacDetails = null;

        for (Visit details : visits) {
            if (VisitType.SCREENING.equals(details.getType())) {
                screeningDate = details.getDate();
            } else if (VisitType.PRIME_VACCINATION_DAY.equals(details.getType())) {
                details.setBookingActualDate(primeVaccinationDate);
                detailsList.add(details);
                primeVacDetails = details;
            } else if (VisitType.PRIME_VACCINATION_FIRST_FOLLOW_UP_VISIT.equals(details.getType())) {
                VisitScheduleOffset offset = offsetMap.get(details.getType());
                if (offset == null) {
                    throw new VisitScheduleException(String.format("Cannot calculate Planned Dates, because no Visit Schedule Offset found for visit: %s",
                            details.getType().getDisplayValue()));
                }
                details.setBookingPlannedDate(primeVaccinationDate.plusDays(offset.getTimeOffset()));
                detailsList.add(details);
            }
        }

        validateDate(primeVaccinationDate, screeningDate, primeVacDetails);

        return detailsList;
    }

    private Long getActualStageId(Long stageId) {
        Long actualStageId = stageId;
        if (actualStageId == null) {
            actualStageId = configService.getConfig().getActiveStageId();
        }

        if (actualStageId == null) {
            throw new VisitScheduleException("Cannot calculate Planned Dates, because Participant stageId is empty");
        }

        return actualStageId;
    }

    private void validateDate(LocalDate date, LocalDate screeningDate, Visit details) {

        if (screeningDate == null) {
            throw new VisitScheduleException("Couldn't save Planned Dates, because Participant didn't participate in screening visit");
        }

        LocalDate earliestDate;
        LocalDate latestDate = screeningDate.plusDays(PrevacConstants.LATEST_DATE);

        if (!isFemaleChildBearingAge(details)) {
            earliestDate = screeningDate.plusDays(PrevacConstants.EARLIEST_DATE);
        } else {
            earliestDate = screeningDate.plusDays(PrevacConstants.EARLIEST_DATE_IF_FEMALE_CHILD_BEARING_AGE);
        }

        if (date.isBefore(earliestDate) || date.isAfter(latestDate)) {
            throw new VisitScheduleException(String.format("The date should be between %s and %s but is %s",
                    earliestDate, latestDate, date));
        }
    }

    private boolean isFemaleChildBearingAge(Visit details) {
        return details != null && details.getSubject().getFemaleChildBearingAge() != null
                && details.getSubject().getFemaleChildBearingAge();
    }

    private LocalDate getScreeningDate(Subject subject) {
        LocalDate screeningDate = null;

        for (Visit visit : subject.getVisits()) {
            if (VisitType.SCREENING.equals(visit.getType())) {
                screeningDate = visit.getDate();
            }
        }

        if (screeningDate == null) {
            throw new VisitScheduleException(String.format("Couldn't save Planned Dates, because Participant with Id:" +
                    "%s didn't participate in screening visit", subject.getSubjectId()));
        }
        return screeningDate;
    }

    private Visit getPrimerVaccinationDetails(Subject subject) {
        return visitBookingDetailsDataService.findByParticipantIdAndVisitType(subject.getSubjectId(), VisitType.PRIME_VACCINATION_DAY);
    }
}
