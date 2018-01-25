package org.motechproject.prevac.service;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.motechproject.commons.api.Range;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.prevac.domain.Clinic;
import org.motechproject.prevac.domain.DateFilter;
import org.motechproject.prevac.domain.enums.ScreeningStatus;
import org.motechproject.prevac.domain.enums.VisitType;
import org.motechproject.prevac.dto.CapacityInfoDto;
import org.motechproject.prevac.repository.ClinicDataService;
import org.motechproject.prevac.repository.ScreeningDataService;
import org.motechproject.prevac.repository.UnscheduledVisitDataService;
import org.motechproject.prevac.repository.VisitDataService;
import org.motechproject.prevac.service.impl.CapacityInfoServiceImpl;
import org.motechproject.prevac.web.domain.GridSettings;
import org.motechproject.prevac.web.domain.Records;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CapacityInfoService.class)
public class CapacityInfoServiceTest {

    @InjectMocks
    private CapacityInfoService capacityInfoService = new CapacityInfoServiceImpl();

    @Mock
    private ScreeningDataService screeningDataService;

    @Mock
    private UnscheduledVisitDataService unscheduledVisitDataService;

    @Mock
    private VisitDataService visitDataService;

    @Mock
    private ClinicDataService clinicDataService;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldCalculateClinicCapacity() {
        GridSettings gridSettings = createGridSettings(1, 10, DateFilter.DATE_RANGE, "2017-1-1", "2017-1-2");

        Clinic firstClinic = createClinic(1L, "siteId1", "first", 5, 4, 3);
        Clinic secondClinic = createClinic(2L, "siteId2", "second", 10, 2, 9);
        List<Clinic> clinics = new ArrayList<>(Arrays.asList(firstClinic, secondClinic));

        Range<LocalDate> dateRange = new Range<>(new LocalDate(2017, 1, 1), new LocalDate(2017, 1, 2));

        when(clinicDataService.retrieveAll(Mockito.any(QueryParams.class))).thenReturn(clinics);
        when(visitDataService.countFindByClinicIdAndPlannedVisitDateRange(firstClinic.getId(), dateRange)).thenReturn(8L);
        when(visitDataService.countFindByClinicIdVisitTypeAndPlannedVisitDateRange(firstClinic.getId(), VisitType.PRIME_VACCINATION_DAY, dateRange)).thenReturn(7L);
        when(screeningDataService.countFindByClinicIdAndDateRangeAndStatus(firstClinic.getId(), dateRange, ScreeningStatus.ACTIVE)).thenReturn(4L);
        when(unscheduledVisitDataService.countFindByClinicIdAndDateRange(firstClinic.getId(), dateRange)).thenReturn(3L);

        when(visitDataService.countFindByClinicIdAndPlannedVisitDateRange(secondClinic.getId(), dateRange)).thenReturn(4L);
        when(visitDataService.countFindByClinicIdVisitTypeAndPlannedVisitDateRange(secondClinic.getId(), VisitType.PRIME_VACCINATION_DAY, dateRange)).thenReturn(2L);
        when(screeningDataService.countFindByClinicIdAndDateRangeAndStatus(secondClinic.getId(), dateRange, ScreeningStatus.ACTIVE)).thenReturn(6L);
        when(unscheduledVisitDataService.countFindByClinicIdAndDateRange(secondClinic.getId(), dateRange)).thenReturn(1L);

        when(clinicDataService.count()).thenReturn(2L);

        List<CapacityInfoDto> capacityInfoDtos = new ArrayList<>(Arrays.asList(
                new CapacityInfoDto("first", 10, -5, 4, -1),
                new CapacityInfoDto("second", 20, 9, -2, 16)));

        Records<CapacityInfoDto> result = capacityInfoService.getCapacityInfoRecords(gridSettings);
        for (int i = 0; i < capacityInfoDtos.size(); i++) {
            checkIfCapacityDtoAreSame(capacityInfoDtos.get(i), result.getRows().get(i));
        }
    }

    @Test
    public void shouldReturnZerosForEmptyDateRange() {
        GridSettings gridSettings = createGridSettings(1, 10, null, "", "");
        List<Clinic> clinics = new ArrayList<>(Arrays.asList(
                createClinic(1L, "siteId1", "first", 5, 4, 3),
                createClinic(2L, "siteId2", "second", 10, 2, 9)));

        when(clinicDataService.retrieveAll(Mockito.any(QueryParams.class))).thenReturn(clinics);
        when(clinicDataService.count()).thenReturn(2L);

        List<CapacityInfoDto> capacityInfoDtos = new ArrayList<>(Arrays.asList(
                new CapacityInfoDto("first", 0, 0, 0, 0),
                new CapacityInfoDto("second", 0, 0, 0, 0)));

        Records<CapacityInfoDto> result = capacityInfoService.getCapacityInfoRecords(gridSettings);
        for (int i = 0; i < capacityInfoDtos.size(); i++) {
            checkIfCapacityDtoAreSame(capacityInfoDtos.get(i), result.getRows().get(i));
        }
    }

    private GridSettings createGridSettings(int page, int rows, DateFilter dateFilter, String startDate, String endDate) {
        GridSettings gridSettings = new GridSettings();
        gridSettings.setPage(page);
        gridSettings.setRows(rows);
        gridSettings.setDateFilter(dateFilter);
        gridSettings.setStartDate(startDate);
        gridSettings.setEndDate(endDate);
        return gridSettings;
    }

    private void checkIfCapacityDtoAreSame(CapacityInfoDto expected, CapacityInfoDto result) {
        assertEquals(expected.getClinic(), result.getClinic());
        assertEquals(expected.getMaxCapacity(), result.getMaxCapacity());
        assertEquals(expected.getAvailableCapacity(), result.getAvailableCapacity());
        assertEquals(expected.getScreeningSlotRemaining(), result.getScreeningSlotRemaining());
        assertEquals(expected.getVaccineSlotRemaining(), result.getVaccineSlotRemaining());
    }

    private Clinic createClinic(Long id, String siteId, String location, Integer maxCapacityByDay,
                                Integer maxScreeningVisits, Integer maxPrimeVisits) {
        Clinic clinic = new Clinic();
        clinic.setId(id);
        clinic.setSiteId(siteId);
        clinic.setLocation(location);
        clinic.setNumberOfRooms(20);
        clinic.setMaxCapacityByDay(maxCapacityByDay);
        clinic.setMaxScreeningVisits(maxScreeningVisits);
        clinic.setMaxPrimeVisits(maxPrimeVisits);
        clinic.setMaxPrimeFirstFollowUpVisits(0);
        clinic.setMaxPrimeSecondFollowUpVisits(0);
        clinic.setMaxPrimeThirdFollowUpVisits(0);
        clinic.setMaxBoosterVisits(0);
        clinic.setMaxBoosterFirstFollowUpVisits(0);
        clinic.setMaxThreeMonthsPostPrimeVisits(0);
        clinic.setMaxSixMonthsPostPrimeVisits(0);
        clinic.setMaxTwelveMonthsPostPrimeVisits(0);
        clinic.setMaxTwentyFourMonthsPostPrimeVisits(0);
        clinic.setMaxThirtySixMonthsPostPrimeVisits(0);
        clinic.setMaxFortyEightMonthsPostPrimeVisits(0);
        clinic.setMaxSixtyMonthsPostPrimeVisits(0);
        return clinic;
    }
}
