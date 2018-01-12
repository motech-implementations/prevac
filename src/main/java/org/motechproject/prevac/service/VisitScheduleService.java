package org.motechproject.prevac.service;

import org.joda.time.LocalDate;


import java.util.Map;

public interface VisitScheduleService {

    Map<String, String> getPrimeVaccinationDateAndDateRange(String subjectId);

    Map<String, String> calculatePlannedVisitDates(String subjectId, LocalDate primeVaccinationDate);

    void savePlannedVisitDates(String subjectId, LocalDate primeVaccinationDate);
}
