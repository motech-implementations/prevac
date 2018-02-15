package org.motechproject.prevac.service.impl;

import org.joda.time.LocalDate;
import org.motechproject.prevac.constants.PrevacConstants;
import org.motechproject.prevac.domain.Subject;
import org.motechproject.prevac.domain.Visit;
import org.motechproject.prevac.domain.VisitScheduleOffset;
import org.motechproject.prevac.domain.enums.VisitType;
import org.motechproject.prevac.exception.VisitScheduleException;
import org.motechproject.prevac.repository.SubjectDataService;
import org.motechproject.prevac.service.ConfigService;
import org.motechproject.prevac.service.VisitScheduleOffsetService;
import org.motechproject.prevac.service.VisitScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("visitScheduleService")
public class VisitScheduleServiceImpl implements VisitScheduleService {

    private static final String PRIME_VAC_DATE_KEY = "primeVacDateKey";
    private static final String SCREENING_DATE_KEY = "screeningDateKey";

    @Autowired
    private SubjectDataService subjectDataService;

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
            if (subject.getPrimerVaccinationDate() != null) {
                primeVacDate = subject.getPrimerVaccinationDate();
            }

            Map<String, LocalDate> visitDates = getScreeningAndPrimeVacDates(subject);
            if (!visitDates.isEmpty()) {
                if (subject.getFemaleChildBearingAge() == null || !subject.getFemaleChildBearingAge()) {
                    earliestDate = visitDates.get(PRIME_VAC_DATE_KEY);
                } else {
                    earliestDate = visitDates.get(PRIME_VAC_DATE_KEY).plusDays(PrevacConstants.EARLIEST_DATE_IF_FEMALE_CHILD_BEARING_AGE);
                }
                latestDate = visitDates.get(SCREENING_DATE_KEY).plusDays(PrevacConstants.LATEST_DATE);
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
    public Map<String, String> calculatePlannedVisitDates(String subjectId, LocalDate primeVaccinationDate, boolean ignoreLimits) {
        Map<String, String> plannedDates = new HashMap<>();
        Subject subject = subjectDataService.findBySubjectId(subjectId);

        validateDate(primeVaccinationDate, subject, ignoreLimits);
        for (Visit visit : calculatePlannedDates(subject, primeVaccinationDate)) {
            if (!VisitType.SCREENING.equals(visit.getType()) && !VisitType.PRIME_VACCINATION_DAY.equals(visit.getType())
                    && visit.getDate() == null) {
                plannedDates.put(visit.getType().getDisplayValue(), visit.getDateProjected().toString(PrevacConstants.SIMPLE_DATE_FORMAT));
            }
        }

        return plannedDates;
    }

    @Override
    public void savePlannedVisitDates(String subjectId, LocalDate primeVaccinationDate, boolean ignoreLimits) {
        Subject subject = subjectDataService.findBySubjectId(subjectId);

        validateDate(primeVaccinationDate, subject, ignoreLimits);
        calculatePlannedDates(subject, primeVaccinationDate);
        subject.setPrimerVaccinationDate(primeVaccinationDate);

        subjectDataService.update(subject);
    }

    private List<Visit> calculatePlannedDates(Subject subject, LocalDate primeVaccinationDate) {
        Map<VisitType, VisitScheduleOffset> offsetMap = visitScheduleOffsetService.getAllAsMap();

        if (offsetMap == null || offsetMap.isEmpty()) {
            throw new VisitScheduleException("Cannot calculate Planned Dates, because no Visit Schedule Offset found.");
        }

        Map<VisitType, Visit> visitMap = getAsMap(subject.getVisits());

        Visit screeningVisit = visitMap.get(VisitType.SCREENING);
        Visit primeVacVisit = visitMap.get(VisitType.PRIME_VACCINATION_DAY);

        if (screeningVisit == null || primeVacVisit == null) {
            throw new VisitScheduleException(String.format("Cannot save Planned Dates, because Participant with Id: %s has no " +
                    "Screening or Prime Vaccination Day Visit", subject.getSubjectId()));
        }

        primeVacVisit.setDate(primeVaccinationDate);

        VisitScheduleOffset boostVacOffset = offsetMap.get(VisitType.BOOST_VACCINATION_DAY);
        offsetMap.remove(VisitType.BOOST_VACCINATION_DAY);
        Visit boostVacVisit = visitMap.get(VisitType.BOOST_VACCINATION_DAY);
        List<String> boosterRelatedVisits = configService.getConfig().getBoosterRelatedVisits();

        if (boostVacVisit == null) {
            boostVacVisit = createVisit(primeVacVisit, boostVacOffset);
            subject.getVisits().add(boostVacVisit);
        } else {
            updateVisit(primeVacVisit, boostVacVisit, boostVacOffset);
        }

        for (VisitScheduleOffset offset : offsetMap.values()) {
            VisitType visitType = offset.getVisitType();
            Visit visit = visitMap.get(visitType);
            Visit baseVisit = boosterRelatedVisits.contains(visitType.getDisplayValue()) ? boostVacVisit : primeVacVisit;

            if (visit == null) {
                visit = createVisit(baseVisit, offset);
                subject.getVisits().add(visit);
            } else {
                updateVisit(baseVisit, visit, offset);
            }
        }

        return subject.getVisits();
    }

    private Visit createVisit(Visit baseVisit, VisitScheduleOffset offset) {
        Visit visit = new Visit();
        visit.setClinic(baseVisit.getClinic());
        visit.setSubject(baseVisit.getSubject());
        visit.setType(offset.getVisitType());

        setVisitPlannedDate(baseVisit, visit, offset);

        return visit;
    }

    private void updateVisit(Visit baseVisit, Visit visit, VisitScheduleOffset offset) {
        if (visit.getDate() == null) {
            setVisitPlannedDate(baseVisit, visit, offset);
        }
    }

    private void setVisitPlannedDate(Visit baseVisit, Visit visit, VisitScheduleOffset offset) {
        LocalDate actualVisitDate = baseVisit.getDate();

        if (actualVisitDate != null) {
            visit.setDateProjected(actualVisitDate.plusDays(offset.getTimeOffset()));
        } else {
            visit.setDateProjected(baseVisit.getDateProjected().plusDays(offset.getTimeOffset()));
        }
    }

    private void validateDate(LocalDate primeVacDate, Subject subject, boolean ignoreLimits) {
        if (primeVacDate == null) {
            throw new VisitScheduleException("Cannot calculate Planned Dates, because Prime Vaccination Date is empty");
        }

        if (!ignoreLimits) {
            Map<String, LocalDate> visitDates = getScreeningAndPrimeVacDates(subject);

            if (!visitDates.containsKey(SCREENING_DATE_KEY)) {
                throw new VisitScheduleException("Couldn't calculate Planned Dates, because Participant didn't participate in screening visit");
            }

            LocalDate earliestDate;
            LocalDate latestDate = visitDates.get(SCREENING_DATE_KEY).plusDays(PrevacConstants.LATEST_DATE);

            if (subject.getFemaleChildBearingAge() == null || !subject.getFemaleChildBearingAge()) {
                earliestDate = visitDates.get(PRIME_VAC_DATE_KEY);
            } else {
                earliestDate = visitDates.get(PRIME_VAC_DATE_KEY).plusDays(PrevacConstants.EARLIEST_DATE_IF_FEMALE_CHILD_BEARING_AGE);
            }

            if (primeVacDate.isBefore(earliestDate) || primeVacDate.isAfter(latestDate)) {
                throw new VisitScheduleException(String.format("The Prime Vaccination date should be between %s and %s but is %s",
                    earliestDate, latestDate, primeVacDate));
            }
        }
    }

    private Map<String, LocalDate> getScreeningAndPrimeVacDates(Subject subject) {
        Map<String, LocalDate> datesMap = new HashMap<>();

        for (Visit visit : subject.getVisits()) {
            if (VisitType.PRIME_VACCINATION_DAY.equals(visit.getType()) && visit.getDateProjected() != null) {
                datesMap.put(PRIME_VAC_DATE_KEY, visit.getDateProjected());
            }
            if (VisitType.SCREENING.equals(visit.getType()) && visit.getDate() != null) {
                datesMap.put(SCREENING_DATE_KEY, visit.getDate());
            }
        }

        if (datesMap.isEmpty()) {
            throw new VisitScheduleException(String.format("Couldn't save Planned Dates, because Participant with Id:" +
                "%s didn't participate in screening visit and doesn't have Prime Vaccination visit planned.", subject.getSubjectId()));
        }
        if (!datesMap.containsKey(PRIME_VAC_DATE_KEY)) {
            throw new VisitScheduleException(String.format("Couldn't save Planned Dates, because Participant with Id:" +
                    "%s doesn't have Prime Vaccination Visit planned yet.", subject.getSubjectId()));
        }
        if (!datesMap.containsKey(SCREENING_DATE_KEY)) {
            throw new VisitScheduleException(String.format("Couldn't save Planned Dates, because Participant with Id:" +
                "%s didn't participate in screening visit.", subject.getSubjectId()));
        }
        return datesMap;
    }

    private Map<VisitType, Visit> getAsMap(List<Visit> visits) {
        Map<VisitType, Visit> visitMap = new HashMap<>();

        for (Visit visit : visits) {
            visitMap.put(visit.getType(), visit);
        }

        return visitMap;
    }
}
