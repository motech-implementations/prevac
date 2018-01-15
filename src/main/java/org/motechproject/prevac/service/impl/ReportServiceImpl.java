package org.motechproject.prevac.service.impl;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.motechproject.commons.api.Range;
import org.motechproject.prevac.constants.PrevacConstants;
import org.motechproject.prevac.domain.Clinic;
import org.motechproject.prevac.domain.DateFilter;
import org.motechproject.prevac.domain.enums.ScreeningStatus;
import org.motechproject.prevac.domain.enums.VisitType;
import org.motechproject.prevac.dto.CapacityReportDto;
import org.motechproject.prevac.repository.ClinicDataService;
import org.motechproject.prevac.repository.ScreeningDataService;
import org.motechproject.prevac.repository.UnscheduledVisitDataService;
import org.motechproject.prevac.repository.VisitDataService;
import org.motechproject.prevac.service.LookupService;
import org.motechproject.prevac.service.ReportService;
import org.motechproject.prevac.web.domain.GridSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("reportService")
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ClinicDataService clinicDataService;

    @Autowired
    private VisitDataService visitDataService;

    @Autowired
    private ScreeningDataService screeningDataService;

    @Autowired
    private UnscheduledVisitDataService unscheduledVisitDataService;

    @Autowired
    private LookupService lookupService;

    @Override
    public List<CapacityReportDto> generateCapacityReports(GridSettings settings) {
        List<CapacityReportDto> reports = new ArrayList<>();

        List<Clinic> clinics = lookupService.getEntities(Clinic.class, settings, null);

        Range<LocalDate> dateRange = getDateRangeFromFilter(settings);

        if (dateRange != null) {
            for (LocalDate date = dateRange.getMin(); !date.isAfter(dateRange.getMax()); date = date.plusDays(1)) {
                for (Clinic clinic : clinics) {
                    int visitCount = (int) visitDataService.countFindByClinicIdAndPlannedVisitDate(clinic.getId(), date);
                    int primeVacCount = (int) visitDataService.countFindByClinicIdVisitTypeAndPlannedVisitDate(clinic.getId(),
                            VisitType.PRIME_VACCINATION_DAY, date);
                    int screeningCount = (int) screeningDataService.countFindByClinicIdAndDateAndStatus(clinic.getId(), date, ScreeningStatus.ACTIVE);
                    int unscheduledCount = (int) unscheduledVisitDataService.countFindByClinicIdAndDate(clinic.getId(), date);

                    int allVisitsCount = visitCount + screeningCount + unscheduledCount;
                    int maxCapacity = clinic.getMaxCapacityByDay();
                    int availableCapacity = maxCapacity - allVisitsCount;
                    int screeningSlotRemaining = clinic.getMaxScreeningVisits() - screeningCount;
                    int vaccineSlotRemaining = clinic.getMaxPrimeVisits() - primeVacCount;

                    reports.add(new CapacityReportDto(date.toString(PrevacConstants.SIMPLE_DATE_FORMAT),
                            clinic.getLocation(), maxCapacity, availableCapacity, screeningSlotRemaining, vaccineSlotRemaining));
                }
            }
        }
        return reports;
    }

    private Range<LocalDate> getDateRangeFromFilter(GridSettings settings) {
        DateFilter filter = settings.getDateFilter();

        if (filter == null) {
            return null;
        }

        if (DateFilter.DATE_RANGE.equals(filter)) {
            if (StringUtils.isNotBlank(settings.getStartDate()) && StringUtils.isNotBlank(settings.getEndDate())) {
                return new Range<>(LocalDate.parse(settings.getStartDate(), DateTimeFormat.forPattern(PrevacConstants.SIMPLE_DATE_FORMAT)),
                        LocalDate.parse(settings.getEndDate(), DateTimeFormat.forPattern(PrevacConstants.SIMPLE_DATE_FORMAT)));
            } else {
                return null;
            }
        } else {
            return filter.getRange();
        }
    }
}
