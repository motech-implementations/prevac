package org.motechproject.prevac.service.impl;

import org.apache.commons.lang.StringUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.motechproject.commons.api.Range;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.prevac.constants.PrevacConstants;
import org.motechproject.prevac.domain.Clinic;
import org.motechproject.prevac.domain.DateFilter;
import org.motechproject.prevac.domain.enums.ScreeningStatus;
import org.motechproject.prevac.domain.enums.VisitType;
import org.motechproject.prevac.dto.CapacityInfoDto;
import org.motechproject.prevac.repository.ClinicDataService;
import org.motechproject.prevac.repository.ScreeningDataService;
import org.motechproject.prevac.repository.UnscheduledVisitDataService;
import org.motechproject.prevac.repository.VisitBookingDetailsDataService;
import org.motechproject.prevac.service.CapacityInfoService;
import org.motechproject.prevac.util.QueryParamsBuilder;
import org.motechproject.prevac.web.domain.GridSettings;
import org.motechproject.prevac.web.domain.Records;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("capacityInfoService")
public class CapacityInfoServiceImpl implements CapacityInfoService {

    @Autowired
    private ClinicDataService clinicDataService;

    @Autowired
    private ScreeningDataService screeningDataService;

    @Autowired
    private UnscheduledVisitDataService unscheduledVisitDataService;

    @Autowired
    private VisitBookingDetailsDataService visitBookingDetailsDataService;

    @Override
    public Records<CapacityInfoDto> getCapacityInfoRecords(GridSettings settings) {
        QueryParams queryParams = QueryParamsBuilder.buildQueryParams(settings);

        List<CapacityInfoDto> capacityInfoDtos = new ArrayList<>();
        List<Clinic> clinics = clinicDataService.retrieveAll(queryParams);

        Range<LocalDate> dateRange = getDateRangeFromFilter(settings);

        if (dateRange != null) {
            int numberOfDays = Days.daysBetween(dateRange.getMin(), dateRange.getMax()).getDays() + 1;
            numberOfDays = numberOfDays < 0 ? 0 : numberOfDays;

            for (Clinic clinic : clinics) {
                int visitCount = (int) visitBookingDetailsDataService.countFindByClinicIdAndBookingPlannedDateRange(clinic.getId(), dateRange);
                int primeVacCount = (int) visitBookingDetailsDataService.countFindByClinicIdVisitTypeAndBookingPlannedDateRange(clinic.getId(),
                        VisitType.PRIME_VACCINATION_DAY, dateRange);
                int screeningCount = (int) screeningDataService.countFindByClinicIdAndDateRangeAndStatus(clinic.getId(), dateRange, ScreeningStatus.ACTIVE);
                int unscheduledCount = (int) unscheduledVisitDataService.countFindByClinicIdAndDateRange(clinic.getId(), dateRange);

                int allVisitsCount = visitCount + screeningCount + unscheduledCount;
                int maxCapacity = clinic.getMaxCapacityByDay() * numberOfDays;
                int availableCapacity = maxCapacity - allVisitsCount;
                int screeningSlotRemaining = clinic.getMaxScreeningVisits() * numberOfDays - screeningCount;
                int vaccineSlotRemaining = clinic.getMaxPrimeVisits() * numberOfDays - primeVacCount;

                capacityInfoDtos.add(new CapacityInfoDto(clinic.getLocation(), maxCapacity, availableCapacity,
                        screeningSlotRemaining, vaccineSlotRemaining));
            }
        } else {
            for (Clinic clinic : clinics) {
                capacityInfoDtos.add(new CapacityInfoDto(clinic.getLocation(), 0, 0, 0, 0));
            }
        }

        long recordCount;
        int rowCount;

        recordCount = clinicDataService.count();
        rowCount = (int) Math.ceil(recordCount / (double) settings.getRows());

        return new Records<>(settings.getPage(), rowCount, (int) recordCount, capacityInfoDtos);
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
