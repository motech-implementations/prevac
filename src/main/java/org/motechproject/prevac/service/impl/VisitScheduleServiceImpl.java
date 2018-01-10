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
            Visit visit = getPrimerVaccinationVisit(subject);

            if (subject.getPrimerVaccinationDate() != null) {
                primeVacDate = subject.getPrimerVaccinationDate();
            } else if (visit != null) {
                primeVacDate = visit.getDateProjected();
            }

            LocalDate screeningDate = getScreeningDate(subject);
            if (screeningDate != null) {
                if (!isFemaleChildBearingAge(visit)) {
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

        Subject subject = subjectDataService.findBySubjectId(subjectId);

        if (subject.getPrimerVaccinationDate() == null) {
            for (Visit visit : calculatePlannedDates(visits, primeVaccinationDate)) {
                if (VisitType.PRIME_VACCINATION_DAY.equals(visit.getType())) {
                    plannedDates.put(visit.getType().getDisplayValue(), visit.getDate().toString(PrevacConstants.SIMPLE_DATE_FORMAT));
                } else {
                    plannedDates.put(visit.getType().getDisplayValue(), visit.getDateProjected().toString(PrevacConstants.SIMPLE_DATE_FORMAT));
                }
            }
        } else {
            for (Visit visit : subject.getVisits()) {
                if (visit.getDateProjected() != null && !VisitType.PRIME_VACCINATION_DAY.equals(visit.getType())) {
                    plannedDates.put(visit.getType().getDisplayValue(), visit.getDateProjected().toString(PrevacConstants.SIMPLE_DATE_FORMAT));
                }
            }
        }

        return plannedDates;
    }

    @Override
    public void savePlannedVisitDates(String subjectId, LocalDate primeVaccinationDate) {
        Subject subject = subjectDataService.findBySubjectId(subjectId);

        if (subject.getPrimerVaccinationDate() != null) {
            throw new VisitScheduleException(String.format("Cannot save Planned Dates, because Participant with Id: %s has been vaccinated", subjectId));
        }

        List<Visit> visits = subject.getVisits();

        if (visits == null || visits.isEmpty()) {
            throw new VisitScheduleException(String.format("Cannot save Planned Dates, because Participant with Id: %s has no Visits", subjectId));
        }

        List<Visit> visitsToSave = new ArrayList<>();
        visitsToSave.add(visitBookingDetailsDataService.findByParticipantIdAndVisitType(subjectId, VisitType.SCREENING));
        visitsToSave.addAll(calculatePlannedDates(visits, primeVaccinationDate));
        subject.setPrimerVaccinationDate(primeVaccinationDate);
        subject.setVisits(visitsToSave);
        subjectDataService.update(subject);
    }

    private List<Visit> calculatePlannedDates(List<Visit> visits, LocalDate primeVaccinationDate) {

        if (primeVaccinationDate == null) {
            throw new VisitScheduleException("Cannot calculate Planned Dates, because Prime Vaccination Date is empty");
        }

        Map<VisitType, VisitScheduleOffset> offsetMap = visitScheduleOffsetService.getAllAsMap();

        if (offsetMap == null || offsetMap.isEmpty()) {
            throw new VisitScheduleException(String.format("Cannot calculate Planned Dates, because no Visit Schedule Offset found."));
        }

        List<Visit> visitList = new ArrayList<>();
        LocalDate screeningDate = null;
        Visit primeVacVisit = null;

        for (Visit visit : visits) {
            if (VisitType.SCREENING.equals(visit.getType())) {
                screeningDate = visit.getDate();
            } else if (VisitType.PRIME_VACCINATION_DAY.equals(visit.getType())) {
                visit.setDate(primeVaccinationDate);
                visitList.add(visit);
                primeVacVisit = visit;
            }
        }

        validateDate(primeVaccinationDate, screeningDate, primeVacVisit);

        visitList.addAll(planVisits(primeVacVisit, offsetMap));

        return visitList;
    }

    //todo can it be simpler??? think!
    private List<Visit> planVisits(Visit primeVacVisit, Map<VisitType, VisitScheduleOffset> offsetMap) {
        List<Visit> visits = new ArrayList<>();

        visits.addAll(planBoosterVisits(primeVacVisit, offsetMap));

        for (VisitScheduleOffset offset : offsetMap.values()) {
            Visit visit = createVisit(primeVacVisit, offset);
            visits.add(visit);
        }
        return visits;
    }

    private List<Visit> planBoosterVisits(Visit primeVacVisit, Map<VisitType, VisitScheduleOffset> offsetMap) {
        List<Visit> boosterVisits = new ArrayList<>();

        Visit boostVacVisit = createVisit(primeVacVisit, offsetMap.get(VisitType.BOOST_VACCINATION_DAY));
        boosterVisits.add(boostVacVisit);

        Visit boostFirstFollowUpVisit = createVisit(boostVacVisit, offsetMap.get(VisitType.BOOST_VACCINATION_FIRST_FOLLOW_UP_VISIT));
        boosterVisits.add(boostFirstFollowUpVisit);

        //todo check if they are removed
        offsetMap.remove(VisitType.BOOST_VACCINATION_DAY);
        offsetMap.remove(VisitType.BOOST_VACCINATION_FIRST_FOLLOW_UP_VISIT);

        return boosterVisits;
    }

    private Visit createVisit(Visit baseVisit, VisitScheduleOffset offset) {
        Visit visit = new Visit();

        LocalDate actualVisitDate = baseVisit.getDate();
        //todo should this be only for BOOST 1st FOLLOW UP???
        if (actualVisitDate != null) {
            visit.setDateProjected(actualVisitDate.plusDays(offset.getTimeOffset()));
        } else {
            visit.setDateProjected(baseVisit.getDateProjected().plusDays(offset.getTimeOffset()));
        }

        visit.setClinic(baseVisit.getClinic());
        visit.setSubject(baseVisit.getSubject());
        visit.setType(offset.getVisitType());
        visit.setIgnoreDateLimitation(baseVisit.getIgnoreDateLimitation());
        visit.setStartTime(baseVisit.getStartTime());
        visit.setEndTime(baseVisit.getEndTime());
        return visit;
    }

    private void validateDate(LocalDate date, LocalDate screeningDate, Visit visit) {

        if (screeningDate == null) {
            throw new VisitScheduleException("Couldn't save Planned Dates, because Participant didn't participate in screening visit");
        }

        LocalDate earliestDate;
        LocalDate latestDate = screeningDate.plusDays(PrevacConstants.LATEST_DATE);

        if (!isFemaleChildBearingAge(visit)) {
            earliestDate = screeningDate.plusDays(PrevacConstants.EARLIEST_DATE);
        } else {
            earliestDate = screeningDate.plusDays(PrevacConstants.EARLIEST_DATE_IF_FEMALE_CHILD_BEARING_AGE);
        }

        if (date.isBefore(earliestDate) || date.isAfter(latestDate)) {
            throw new VisitScheduleException(String.format("The date should be between %s and %s but is %s",
                    earliestDate, latestDate, date));
        }
    }

    private boolean isFemaleChildBearingAge(Visit visit) {
        return visit != null && visit.getSubject().getFemaleChildBearingAge() != null
                && visit.getSubject().getFemaleChildBearingAge();
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

    private Visit getPrimerVaccinationVisit(Subject subject) {
        return visitBookingDetailsDataService.findByParticipantIdAndVisitType(subject.getSubjectId(), VisitType.PRIME_VACCINATION_DAY);
    }
}
