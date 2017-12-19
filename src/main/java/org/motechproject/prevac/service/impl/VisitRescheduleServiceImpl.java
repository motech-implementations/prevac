package org.motechproject.prevac.service.impl;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;
import org.motechproject.commons.date.model.Time;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.prevac.constants.PrevacConstants;
import org.motechproject.prevac.domain.Clinic;
import org.motechproject.prevac.domain.Visit;
import org.motechproject.prevac.domain.VisitScheduleOffset;
import org.motechproject.prevac.domain.enums.VisitType;
import org.motechproject.prevac.dto.VisitRescheduleDto;
import org.motechproject.prevac.exception.LimitationExceededException;
import org.motechproject.prevac.helper.VisitLimitationHelper;
import org.motechproject.prevac.repository.VisitBookingDetailsDataService;
import org.motechproject.prevac.service.ConfigService;
import org.motechproject.prevac.service.LookupService;
import org.motechproject.prevac.service.VisitRescheduleService;
import org.motechproject.prevac.service.VisitScheduleOffsetService;
import org.motechproject.prevac.util.QueryParamsBuilder;
import org.motechproject.prevac.web.domain.GridSettings;
import org.motechproject.prevac.web.domain.Records;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("visitRescheduleService")
public class VisitRescheduleServiceImpl implements VisitRescheduleService {

    @Autowired
    private LookupService lookupService;

    @Autowired
    private VisitBookingDetailsDataService visitBookingDetailsDataService;

    @Autowired
    private VisitScheduleOffsetService visitScheduleOffsetService;

    @Autowired
    private VisitLimitationHelper visitLimitationHelper;

    @Autowired
    private ConfigService configService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Records<VisitRescheduleDto> getVisitsRecords(GridSettings settings) throws IOException {
        QueryParams queryParams = QueryParamsBuilder.buildQueryParams(settings, getFields(settings.getFields()));
        Records<Visit> detailsRecords = lookupService.getEntities(Visit.class, settings.getLookup(), settings.getFields(), queryParams);

        Map<Long, Map<VisitType, VisitScheduleOffset>> offsetMap = visitScheduleOffsetService.getAllAsMap();
        List<String> boosterRelatedMessages = configService.getConfig().getBoosterRelatedMessages();
        List<String> thirdVaccinationRelatedMessages = configService.getConfig().getThirdVaccinationRelatedMessages();
        Long activeStageId = configService.getConfig().getActiveStageId();

        List<VisitRescheduleDto> dtos = new ArrayList<>();

        for (Visit details : detailsRecords.getRows()) {
            Long stageId = details.getSubject().getStageId();

            if (stageId == null) {
                stageId = activeStageId;
            }

            Boolean boosterRelated = isBoosterRelated(details.getType(), boosterRelatedMessages, stageId);
            Boolean thirdVaccinationRelated = isThirdVaccinationRelated(details.getType(), thirdVaccinationRelatedMessages, stageId);
            LocalDate vaccinationDate = getVaccinationDate(details, boosterRelated, thirdVaccinationRelated);
            Boolean notVaccinated = true;
            Range<LocalDate> dateRange = null;

            if (vaccinationDate != null) {
                dateRange = calculateEarliestAndLatestDate(details.getType(), offsetMap, vaccinationDate, stageId);
                notVaccinated = false;
            }

            dtos.add(new VisitRescheduleDto(details, dateRange, boosterRelated, thirdVaccinationRelated, notVaccinated));
        }

        return new Records<>(detailsRecords.getPage(), detailsRecords.getTotal(), detailsRecords.getRecords(), dtos);
    }

    @Override
    public VisitRescheduleDto saveVisitReschedule(VisitRescheduleDto visitRescheduleDto, Boolean ignoreLimitation) {
        Visit visit = visitBookingDetailsDataService.findById(visitRescheduleDto.getVisitBookingDetailsId());

        if (visit == null) {
            throw new IllegalArgumentException("Cannot reschedule, because details for Visit not found");
        }

        Clinic clinic = visit.getClinic();

        validateDate(visitRescheduleDto, visit);

        if (clinic != null && !ignoreLimitation) {
            checkNumberOfPatients(visitRescheduleDto, clinic);
        }

        updateVisitPlannedDate(visit, visitRescheduleDto);

        return new VisitRescheduleDto(updateVisitDetailsWithDto(visit, visitRescheduleDto));
    }

    private void checkNumberOfPatients(VisitRescheduleDto dto, Clinic clinic) { //NO CHECKSTYLE CyclomaticComplexity

        List<Visit> visits = visitBookingDetailsDataService
                .findByClinicIdVisitPlannedDateAndType(clinic.getId(), dto.getPlannedDate(), dto.getVisitType());

        visitLimitationHelper.checkCapacityForVisitBookingDetails(dto.getPlannedDate(), clinic, dto.getVisitBookingDetailsId());

        if (visits != null && !visits.isEmpty()) {
            int numberOfRooms = clinic.getNumberOfRooms();
            int maxVisits = visitLimitationHelper.getMaxVisitCountForVisitType(dto.getVisitType(), clinic);
            int patients = 0;

            Time startTime = dto.getStartTime();
            Time endTime = null;

            if (startTime != null) {
                endTime = calculateEndTime(startTime);
            }

            for (Visit visit : visits) {
                if (visit.getId().equals(dto.getVisitBookingDetailsId())) {
                    maxVisits++;
                } else if (startTime != null && visit.getStartTime() != null) {
                    if (startTime.isBefore(visit.getStartTime())) {
                        if (visit.getStartTime().isBefore(endTime)) {
                            patients++;
                        }
                    } else {
                        if (startTime.isBefore(visit.getEndTime())) {
                            patients++;
                        }
                    }
                }
            }

            if (visits.size() >= maxVisits) {
                throw new LimitationExceededException("The booking limit for this type of visit has been reached");
            }
            if (patients >= numberOfRooms) {
                throw new LimitationExceededException("Too many visits at the same time");
            }
        }
    }

    private void validateDate(VisitRescheduleDto dto, Visit visit) {
        if (visit.getDate() != null) {
            throw new IllegalArgumentException("Cannot reschedule, because Visit already took place");
        }

        if (dto.getPlannedDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Date cannot be in the past");
        }

        if (!dto.getIgnoreDateLimitation()) {
            Map<Long, Map<VisitType, VisitScheduleOffset>> offsetMap = visitScheduleOffsetService.getAllAsMap();
            List<String> boosterRelatedMessages = configService.getConfig().getBoosterRelatedMessages();
            List<String> thirdVaccinationRelatedMessages = configService.getConfig().getThirdVaccinationRelatedMessages();
            Long activeStageId = configService.getConfig().getActiveStageId();

            Range<LocalDate> dateRange = calculateEarliestAndLatestDate(visit, offsetMap, boosterRelatedMessages,
                    thirdVaccinationRelatedMessages, activeStageId);

            if (dateRange == null) {
                throw new IllegalArgumentException("Cannot calculate Earliest and Latest Date");
            }

            LocalDate earliestDate = dateRange.getMin();
            LocalDate latestDate = dateRange.getMax();

            if (dto.getPlannedDate().isBefore(earliestDate) || dto.getPlannedDate().isAfter(latestDate)) {
                throw new IllegalArgumentException(String.format("The date should be between %s and %s but is %s",
                        earliestDate, latestDate, dto.getPlannedDate()));
            }
        }
    }

    private Visit updateVisitDetailsWithDto(Visit details, VisitRescheduleDto dto) {
        details.setStartTime(dto.getStartTime());
        details.setEndTime(calculateEndTime(dto.getStartTime()));
        details.setIgnoreDateLimitation(dto.getIgnoreDateLimitation());
        return visitBookingDetailsDataService.update(details);
    }

    private Visit updateVisitPlannedDate(Visit visit, VisitRescheduleDto visitRescheduleDto) {
        visit.setDateProjected(visitRescheduleDto.getPlannedDate());

        return visitBookingDetailsDataService.update(visit);
    }

    private Map<String, Object> getFields(String json) throws IOException {
        if (json == null) {
            return null;
        } else {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap>() {
            });  //NO CHECKSTYLE WhitespaceAround
        }
    }

    private Time calculateEndTime(Time startTime) {
        int endTimeHour = (startTime.getHour() + PrevacConstants.TIME_OF_THE_VISIT) % PrevacConstants.MAX_TIME_HOUR;
        return new Time(endTimeHour, startTime.getMinute());
    }

    private Range<LocalDate> calculateEarliestAndLatestDate(Visit visit, Map<Long, Map<VisitType, VisitScheduleOffset>> offsetMap,
                                                            List<String> boosterRelatedMessages, List<String> thirdVaccinationRelatedMessages,
                                                            Long activeStageId) {
        Long stageId = visit.getSubject().getStageId();

        if (stageId == null) {
            stageId = activeStageId;
        }

        Boolean boosterRelated = isBoosterRelated(visit.getType(), boosterRelatedMessages, stageId);
        Boolean thirdVaccinationRelated = isThirdVaccinationRelated(visit.getType(), thirdVaccinationRelatedMessages, stageId);
        LocalDate vaccinationDate = getVaccinationDate(visit, boosterRelated, thirdVaccinationRelated);

        if (vaccinationDate == null) {
            return null;
        }

        return calculateEarliestAndLatestDate(visit.getType(), offsetMap, vaccinationDate, stageId);
    }

    private Range<LocalDate> calculateEarliestAndLatestDate(VisitType visitType, Map<Long, Map<VisitType, VisitScheduleOffset>> offsetMap,
                                                            LocalDate vaccinationDate, Long stageId) {
        if (stageId == null) {
            return null;
        }

        Map<VisitType, VisitScheduleOffset> visitTypeOffset = offsetMap.get(stageId);

        if (visitTypeOffset == null) {
            return null;
        }

        VisitScheduleOffset offset = visitTypeOffset.get(visitType);

        if (offset == null) {
            return null;
        }

        LocalDate minDate = vaccinationDate.plusDays(offset.getEarliestDateOffset());
        LocalDate maxDate = vaccinationDate.plusDays(offset.getLatestDateOffset());

        return new Range<>(minDate, maxDate);
    }

    private LocalDate getVaccinationDate(Visit visit, Boolean boosterRelated, Boolean thirdVaccinationRelated) {
        if (boosterRelated) {
            return visit.getSubject().getBoosterVaccinationDate();
        } else {
            return visit.getSubject().getPrimerVaccinationDate();
        }
    }

    private Boolean isBoosterRelated(VisitType visitType, List<String> boosterRelatedMessages, Long stageId) {
        String campaignName = getCampaignNameWithStage(visitType, stageId);
        return boosterRelatedMessages.contains(campaignName);
    }

    private Boolean isThirdVaccinationRelated(VisitType visitType, List<String> thirdVaccinationRelatedMessages, Long stageId) {
        String campaignName = getCampaignNameWithStage(visitType, stageId);
        return thirdVaccinationRelatedMessages.contains(campaignName);
    }

    private String getCampaignNameWithStage(VisitType visitType, Long stageId) {
        if (stageId == null) {
            return null;
        }

        if (stageId > 1) {
            return visitType.getDisplayValue() + PrevacConstants.STAGE + stageId;
        }

        return visitType.getDisplayValue();
    }
}
