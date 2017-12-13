package org.motechproject.prevac.helper;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;
import org.motechproject.prevac.constants.PrevacConstants;
import org.motechproject.prevac.domain.DateFilter;
import org.motechproject.prevac.domain.Screening;
import org.motechproject.prevac.domain.Visit;
import org.motechproject.prevac.domain.enums.VisitType;
import org.motechproject.prevac.web.domain.GridSettings;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class DtoLookupHelper {

    private static final String NOT_BLANK_REGEX = ".";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Set<VisitType> AVAILABLE_VISIT_TYPES_FOR_RESCHEDULE_SCREEN = new HashSet<>(Arrays.asList(VisitType.BOOST_VACCINATION_DAY,
            VisitType.PRIME_VACCINATION_FIRST_FOLLOW_UP_VISIT, VisitType.PRIME_VACCINATION_SECOND_FOLLOW_UP_VISIT,
            VisitType.BOOST_VACCINATION_FIRST_FOLLOW_UP_VISIT, VisitType.BOOST_VACCINATION_SECOND_FOLLOW_UP_VISIT,
            VisitType.BOOST_VACCINATION_THIRD_FOLLOW_UP_VISIT, VisitType.FIRST_LONG_TERM_FOLLOW_UP_VISIT,
            VisitType.SECOND_LONG_TERM_FOLLOW_UP_VISIT, VisitType.THIRD_LONG_TERM_FOLLOW_UP_VISIT,
            VisitType.FOURTH_LONG_TERM_FOLLOW_UP_VISIT, VisitType.FIFTH_LONG_TERM_FOLLOW_UP_VISIT,
            VisitType.SIXTH_LONG_TERM_FOLLOW_UP_VISIT, VisitType.SEVENTH_LONG_TERM_FOLLOW_UP_VISIT,
            VisitType.THIRD_VACCINATION_DAY, VisitType.FIRST_POST_THIRD_VACCINATION_VISIT,
            VisitType.SECOND_POST_THIRD_VACCINATION_VISIT, VisitType.THIRD_POST_THIRD_VACCINATION_VISIT,
            VisitType.FOURTH_POST_THIRD_VACCINATION_VISIT, VisitType.FIFTH_POST_THIRD_VACCINATION_VISIT));

    private DtoLookupHelper() {
    }

    public static GridSettings changeLookupForScreeningAndUnscheduled(GridSettings settings) throws IOException {
        Map<String, Object> fieldsMap = new HashMap<>();
        DateFilter dateFilter = settings.getDateFilter();

        if (dateFilter != null) {

            if (StringUtils.isBlank(settings.getFields())) {
                settings.setFields("{}");
            }

            if (StringUtils.isBlank(settings.getLookup())) {
                settings.setLookup("Find By Date");
            } else {
                fieldsMap = getFields(settings.getFields());
                settings.setLookup(settings.getLookup() + " And Date");
            }

            Map<String, String> rangeMap = getDateRangeFromFilter(settings);

            fieldsMap.put(Screening.DATE_PROPERTY_NAME, rangeMap);
            settings.setFields(OBJECT_MAPPER.writeValueAsString(fieldsMap));
        }
        return settings;
    }

    public static GridSettings changeLookupForPrimeVaccinationSchedule(GridSettings settings) throws IOException {
        Map<String, Object> fieldsMap = new HashMap<>();

        if (StringUtils.isBlank(settings.getFields())) {
            settings.setFields("{}");
        }

        if (StringUtils.isBlank(settings.getLookup())) {
            settings.setLookup("Find By Participant Name Prime Vaccination Date And Visit Type And Booking Planned Date");
            fieldsMap.put(Visit.SUBJECT_NAME_PROPERTY_NAME, NOT_BLANK_REGEX);
        } else {
            fieldsMap = getFields(settings.getFields());
            if ("Find By Participant Name".equals(settings.getLookup())) {
                settings.setLookup(settings.getLookup() + " Prime Vaccination Date And Visit Type And Booking Planned Date");
            } else {
                settings.setLookup(settings.getLookup() + " Visit Type And Participant Prime Vaccination Date And Name And Booking Planned Date");
                fieldsMap.put(Visit.SUBJECT_NAME_PROPERTY_NAME, NOT_BLANK_REGEX);
            }
        }

        Map<String, String> rangeMap = getDateRangeFromFilter(settings);

        if (rangeMap != null && (StringUtils.isNotBlank(rangeMap.get("min")) || StringUtils.isNotBlank(rangeMap.get("max")))) {
            settings.setLookup(settings.getLookup() + " Range");
            fieldsMap.put(Visit.BOOKING_PLANNED_DATE_PROPERTY_NAME, rangeMap);
        } else {
            fieldsMap.put(Visit.BOOKING_PLANNED_DATE_PROPERTY_NAME, null);
        }

        fieldsMap.put(Visit.VISIT_TYPE_PROPERTY_NAME, VisitType.PRIME_VACCINATION_DAY.toString());
        fieldsMap.put(Visit.SUBJECT_PRIME_VACCINATION_DATE_PROPERTY_NAME, null);
        settings.setFields(OBJECT_MAPPER.writeValueAsString(fieldsMap));
        return settings;
    }

    public static GridSettings changeLookupForVisitReschedule(GridSettings settings) throws IOException {  //NO CHECKSTYLE CyclomaticComplexity
        Map<String, Object> fieldsMap = new HashMap<>();

        if (StringUtils.isBlank(settings.getFields())) {
            settings.setFields("{}");
        }

        if (StringUtils.isBlank(settings.getLookup())) {
            settings.setLookup("Find By Visit Type Set And Planned Date");
            fieldsMap.put(Visit.VISIT_TYPE_PROPERTY_NAME, AVAILABLE_VISIT_TYPES_FOR_RESCHEDULE_SCREEN);
        } else {
            fieldsMap = getFields(settings.getFields());
            switch (settings.getLookup()) {
                case "Find By Visit Type":
                    String type = (String) fieldsMap.get(Visit.VISIT_TYPE_PROPERTY_NAME);
                    if (StringUtils.isBlank(type) || !AVAILABLE_VISIT_TYPES_FOR_RESCHEDULE_SCREEN.contains(VisitType.valueOf(type))) {
                        fieldsMap.put(Visit.VISIT_TYPE_PROPERTY_NAME, null);
                    }
                    settings.setLookup(settings.getLookup() + " And Planned Date");
                    break;
                default:
                    fieldsMap.put(Visit.VISIT_TYPE_PROPERTY_NAME, AVAILABLE_VISIT_TYPES_FOR_RESCHEDULE_SCREEN);
                    settings.setLookup(settings.getLookup() + " And Visit Type Set And Planned Date");
                    break;
            }
        }

        Map<String, String> rangeMap = getDateRangeFromFilter(settings);

        if (rangeMap != null && (StringUtils.isNotBlank(rangeMap.get("min")) || StringUtils.isNotBlank(rangeMap.get("max")))) {
            settings.setLookup(settings.getLookup() + " Range");
            fieldsMap.put(Visit.VISIT_PLANNED_DATE_PROPERTY_NAME, rangeMap);
        } else {
            fieldsMap.put(Visit.VISIT_PLANNED_DATE_PROPERTY_NAME, null);
        }

        settings.setFields(OBJECT_MAPPER.writeValueAsString(fieldsMap));
        return settings;
    }

    private static Map<String, String> getDateRangeFromFilter(GridSettings settings) {
        DateFilter dateFilter = settings.getDateFilter();

        if (dateFilter == null) {
            return null;
        }

        Map<String, String> rangeMap = new HashMap<>();

        if (DateFilter.DATE_RANGE.equals(dateFilter)) {
            rangeMap.put("min", settings.getStartDate());
            rangeMap.put("max", settings.getEndDate());
        } else {
            Range<LocalDate> dateRange = dateFilter.getRange();
            rangeMap.put("min", dateRange.getMin().toString(PrevacConstants.SIMPLE_DATE_FORMAT));
            rangeMap.put("max", dateRange.getMax().toString(PrevacConstants.SIMPLE_DATE_FORMAT));
        }

        return rangeMap;
    }

    private static Map<String, Object> getFields(String lookupFields) throws IOException {
        return OBJECT_MAPPER.readValue(lookupFields, new TypeReference<HashMap>() {
        }); //NO CHECKSTYLE WhitespaceAround
    }

    private static Map<String, String> getFieldsMap(String lookupFields) throws IOException {
        return OBJECT_MAPPER.readValue(lookupFields, new TypeReference<HashMap>() {
        }); //NO CHECKSTYLE WhitespaceAround
    }
}
