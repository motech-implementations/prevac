package org.motechproject.prevac.helper;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;
import org.motechproject.prevac.domain.DateFilter;
import org.motechproject.prevac.web.domain.GridSettings;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DtoLookupHelperTest {

    private static final String START_DATE = "2017-01-01";
    private static final String END_DATE = "2017-01-20";
    private static final String START_DATE_2 = "2017-02-01";
    private static final String END_DATE_2 = "2017-02-20";
    private static final String LOOKUP = "Find By Parameter";
    private static final String DEFAULT_PRIME_VAC_LOOKUP = "Find By Participant Name Prime Vaccination Date And Visit Type And Planned Visit Date Range";
    private static final String FIND_BY_PARTICIPANT_NAME_LOOKUP = "Find By Participant Name Prime Vaccination Date And Visit Type And Planned Visit Date Range";
    private static final String SPECIAL_PRIME_VAC_LOOKUP_PARAMS = " Visit Type And Participant Prime Vaccination Date And Name And Planned Visit Date Range";
    private static final String DEFAULT_VISIT_RESCHEDULE_LOOKUP = "Find By Visit Type Set And Planned Date Range";
    private static final String BOOST_VAC_DAY = "BOOST_VACCINATION_DAY";
    private static final String ACTUAL_DATE = "date";
    private static final String PLANNED_DATE = "dateProjected";
    private static final String SUBJECT_NAME_FIELD = "subject.name";
    private static final String SUBJECT_ID_FIELD = "subject.subjectId";
    private static final String CLINIC_LOCATION_FIELD = "clinic.location";
    private static final String SUBJECT_NAME_VAL = "name";
    private static final String SUBJECT_ID_VAL = "subjectId";
    private static final String CLINIC_LOCATION_VAL = "location";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String TYPE = "type";
    private static final String NOT_BLANK_REGEX = ".";
    private static final String PARAM_KEY = "param";
    private static final String PARAM_VALUE = "val";
    private static final String OPEN_BRACKET = "{";
    private static final String CLOSE_BRACKET = "}";
    private static final String PARAM_FIELD = "\"%s\":\"%s\"";
    private static final String DATE_PARAM_FIELD = "\"%s\":{\"min\":\"%s\",\"max\":\"%s\"}";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldChangeLookupToFindByDate() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE);
        settings.setEndDate(END_DATE);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForScreeningAndUnscheduled(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MAX);

        assertEquals("Find By Date", returnedSettings.getLookup());
        assertEquals(1, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
    }

    @Test
    public void shouldAddDateToLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE);
        settings.setEndDate(END_DATE);
        settings.setLookup(LOOKUP);
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, PARAM_KEY, PARAM_VALUE) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForScreeningAndUnscheduled(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MAX);
        String paramValue = (String) returnedFields.get(PARAM_KEY);

        assertEquals(LOOKUP + " And Date", returnedSettings.getLookup());
        assertEquals(2, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
        assertEquals(PARAM_VALUE, paramValue);
    }

    @Test
    public void shouldReturnDefaultLookupWithDateRange() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE);
        settings.setEndDate(END_DATE);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForPrimeVaccinationSchedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        assertPrimeVacLookup(NOT_BLANK_REGEX, DEFAULT_PRIME_VAC_LOOKUP, returnedFields, returnedSettings.getLookup(), 4);
    }

    @Test
    public void shouldReturnDefaultLookupWithDate() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE);
        settings.setEndDate(END_DATE);
        settings.setLookup("Find By Participant Name");
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, SUBJECT_NAME_FIELD, SUBJECT_NAME_VAL) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForPrimeVaccinationSchedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        assertPrimeVacLookup(SUBJECT_NAME_VAL, FIND_BY_PARTICIPANT_NAME_LOOKUP, returnedFields, returnedSettings.getLookup(), 4);
    }

    @Test
    public void shouldAddPrimeVacSpecialParamsToLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE);
        settings.setEndDate(END_DATE);
        settings.setLookup(LOOKUP);
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, PARAM_KEY, PARAM_VALUE) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForPrimeVaccinationSchedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        String paramValue = (String) returnedFields.get(PARAM_KEY);
        assertPrimeVacLookup(NOT_BLANK_REGEX, LOOKUP + SPECIAL_PRIME_VAC_LOOKUP_PARAMS, returnedFields, returnedSettings.getLookup(), 5);
        assertEquals(paramValue, PARAM_VALUE);
    }

    @Test
    public void shouldReturnVisitRescheduleDefaultLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE);
        settings.setEndDate(END_DATE);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MAX);
        List<String> returnedVisitTypes = (List<String>) (returnedFields.get(TYPE));

        assertEquals(DEFAULT_VISIT_RESCHEDULE_LOOKUP, returnedSettings.getLookup());
        assertEquals(2, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
        assertEquals(12, returnedVisitTypes.size());
    }

    @Test
    public void testVisitRescheduleFindByVisitTypeAndPlannedDateLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(null);
        settings.setEndDate(null);
        settings.setLookup("Find By Visit Type And Planned Date");
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, TYPE, BOOST_VAC_DAY) + "," +
                String.format(DATE_PARAM_FIELD, PLANNED_DATE, START_DATE, END_DATE) + CLOSE_BRACKET
        );

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MAX);
        String returnedVisitType = (String) returnedFields.get(TYPE);

        assertEquals("Find By Visit Type And Planned Date", returnedSettings.getLookup());
        assertEquals(2, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
        assertEquals(BOOST_VAC_DAY, returnedVisitType);
    }

    @Test
    public void testVisitRescheduleFindByPlannedDateLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE_2);
        settings.setEndDate(START_DATE_2);
        settings.setLookup("Find By Visit Planned Date");
        settings.setFields(OPEN_BRACKET + String.format(DATE_PARAM_FIELD, PLANNED_DATE, START_DATE, END_DATE) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MAX);
        List<String> returnedVisitTypes = (List<String>) (returnedFields.get(TYPE));

        assertEquals("Find By Visit Planned Date Range And Visit Type Set", returnedSettings.getLookup());
        assertEquals(2, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
        assertEquals(12, returnedVisitTypes.size());
    }

    @Test
    public void testVisitRescheduleFindByVisitTypeAndActualDateLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setLookup("Find By Visit Type And Actual Date");
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, TYPE, BOOST_VAC_DAY) + "," +
                String.format(DATE_PARAM_FIELD, ACTUAL_DATE, START_DATE, END_DATE) + CLOSE_BRACKET
        );

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MAX);
        String returnedVisitType = (String) returnedFields.get(TYPE);

        assertEquals("Find By Visit Type And Actual Date", returnedSettings.getLookup());
        assertEquals(2, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
        assertEquals(BOOST_VAC_DAY, returnedVisitType);
    }

    @Test
    public void testVisitRescheduleFindByVisitActualDateLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(null);
        settings.setEndDate(null);
        settings.setLookup("Find By Visit Actual Date");
        settings.setFields(OPEN_BRACKET + String.format(DATE_PARAM_FIELD, ACTUAL_DATE, START_DATE, END_DATE) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MAX);
        List<String> returnedVisitTypes = (List<String>) (returnedFields.get(TYPE));

        assertEquals("Find By Visit Actual Date And Visit Type Set", returnedSettings.getLookup());
        assertEquals(2, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
        assertEquals(12, returnedVisitTypes.size());
    }

    @Test
    public void testVisitRescheduleFindByParticipantIdLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setLookup("Find By Participant Id");
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, SUBJECT_ID_FIELD, SUBJECT_ID_VAL) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        String subjectId = (String) returnedFields.get(SUBJECT_ID_FIELD);
        List<String> returnedVisitTypes = (List<String>) (returnedFields.get(TYPE));

        assertEquals("Find By Participant Id And Visit Type Set", returnedSettings.getLookup());
        assertEquals(2, returnedFields.keySet().size());
        assertEquals(SUBJECT_ID_VAL, subjectId);
        assertEquals(12, returnedVisitTypes.size());
    }

    @Test
    public void testVisitRescheduleFindByParticipantNameLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setLookup("Find By Participant Name");
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, SUBJECT_NAME_FIELD, SUBJECT_NAME_VAL) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        String participantName = (String) returnedFields.get(SUBJECT_NAME_FIELD);
        List<String> returnedVisitTypes = (List<String>) (returnedFields.get(TYPE));

        assertEquals("Find By Participant Name And Visit Type Set", returnedSettings.getLookup());
        assertEquals(2, returnedFields.keySet().size());
        assertEquals(SUBJECT_NAME_VAL, participantName);
        assertEquals(12, returnedVisitTypes.size());
    }

    @Test
    public void testVisitRescheduleFindByClinicLocationLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setLookup("Find By Clinic Location");
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, CLINIC_LOCATION_FIELD, CLINIC_LOCATION_VAL) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        String participantName = (String) returnedFields.get(CLINIC_LOCATION_FIELD);
        List<String> returnedVisitTypes = (List<String>) (returnedFields.get(TYPE));

        assertEquals("Find By Clinic Location And Visit Type Set", returnedSettings.getLookup());
        assertEquals(2, returnedFields.keySet().size());
        assertEquals(CLINIC_LOCATION_VAL, participantName);
        assertEquals(12, returnedVisitTypes.size());
    }

    @Test
    public void testVisitRescheduleFindByParticipantIdLookupRange() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE);
        settings.setEndDate(END_DATE);
        settings.setLookup("Find By Participant Id");
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, SUBJECT_ID_FIELD, SUBJECT_ID_VAL) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        String subjectId = (String) returnedFields.get(SUBJECT_ID_FIELD);
        List<String> returnedVisitTypes = (List<String>) (returnedFields.get(TYPE));
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MAX);

        assertEquals("Find By Participant Id And Visit Type Set And Planned Date Range", returnedSettings.getLookup());
        assertEquals(3, returnedFields.keySet().size());
        assertEquals(SUBJECT_ID_VAL, subjectId);
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
        assertEquals(12, returnedVisitTypes.size());
    }

    @Test
    public void testVisitRescheduleFindByParticipantNameLookupRange() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE);
        settings.setEndDate(END_DATE);
        settings.setLookup("Find By Participant Name");
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, SUBJECT_NAME_FIELD, SUBJECT_NAME_VAL) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        String participantName = (String) returnedFields.get(SUBJECT_NAME_FIELD);
        List<String> returnedVisitTypes = (List<String>) (returnedFields.get(TYPE));
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MAX);

        assertEquals("Find By Participant Name And Visit Type Set And Planned Date Range", returnedSettings.getLookup());
        assertEquals(3, returnedFields.keySet().size());
        assertEquals(SUBJECT_NAME_VAL, participantName);
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
        assertEquals(12, returnedVisitTypes.size());
    }

    @Test
    public void testVisitRescheduleFindByClinicLocationLookupRange() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE);
        settings.setEndDate(END_DATE);
        settings.setLookup("Find By Clinic Location");
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, CLINIC_LOCATION_FIELD, CLINIC_LOCATION_VAL) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        String participantName = (String) returnedFields.get(CLINIC_LOCATION_FIELD);
        List<String> returnedVisitTypes = (List<String>) (returnedFields.get(TYPE));
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MAX);

        assertEquals("Find By Clinic Location And Visit Type Set And Planned Date Range", returnedSettings.getLookup());
        assertEquals(3, returnedFields.keySet().size());
        assertEquals(CLINIC_LOCATION_VAL, participantName);
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
        assertEquals(12, returnedVisitTypes.size());
    }

    @Test
    public void testVisitRescheduleFindByVisitActualDateLookupRange() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE_2);
        settings.setEndDate(END_DATE_2);
        settings.setLookup("Find By Visit Actual Date");
        settings.setFields(OPEN_BRACKET + String.format(DATE_PARAM_FIELD, ACTUAL_DATE, START_DATE, END_DATE) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        String returnedMinActualDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MIN);
        String returnedMaxActualDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MAX);
        String returnedMinPlannedDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MIN);
        String returnedMaxPlannedDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MAX);
        List<String> returnedVisitTypes = (List<String>) (returnedFields.get(TYPE));

        assertEquals("Find By Visit Actual Date And Visit Type Set And Planned Date Range", returnedSettings.getLookup());
        assertEquals(3, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinActualDate);
        assertEquals(END_DATE, returnedMaxActualDate);
        assertEquals(START_DATE_2, returnedMinPlannedDate);
        assertEquals(END_DATE_2, returnedMaxPlannedDate);
        assertEquals(12, returnedVisitTypes.size());
    }

    @Test
    public void testVisitRescheduleFindByVisitTypeAndActualDateLookupRange() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE_2);
        settings.setEndDate(END_DATE_2);
        settings.setLookup("Find By Visit Type And Actual Date");
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, TYPE, BOOST_VAC_DAY) + "," +
                String.format(DATE_PARAM_FIELD, ACTUAL_DATE, START_DATE, END_DATE) + CLOSE_BRACKET
        );

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() {});
        String returnedMinActualDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MIN);
        String returnedMaxActualDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MAX);
        String returnedMinPlannedDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MIN);
        String returnedMaxPlannedDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MAX);
        String returnedVisitType = (String) returnedFields.get(TYPE);

        assertEquals("Find By Visit Type And Actual Date Range And Planned Date Range", returnedSettings.getLookup());
        assertEquals(3, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinActualDate);
        assertEquals(END_DATE, returnedMaxActualDate);
        assertEquals(START_DATE_2, returnedMinPlannedDate);
        assertEquals(END_DATE_2, returnedMaxPlannedDate);
        assertEquals(BOOST_VAC_DAY, returnedVisitType);
    }

    private void assertPrimeVacLookup(String expectedSubjectName, String expectedLookup, Map<String, Object> returnedFields, String returnedLookup, int paramsCount) {
        String primerVacDate = (String) returnedFields.get("subject.primerVaccinationDate");
        String visitType = (String) returnedFields.get(TYPE);
        String subjectName = (String) returnedFields.get(SUBJECT_NAME_FIELD);
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MAX);

        assertEquals(expectedLookup, returnedLookup);
        assertEquals(paramsCount, returnedFields.keySet().size());
        assertEquals(null, primerVacDate);
        assertEquals("PRIME_VACCINATION_DAY", visitType);
        assertEquals(expectedSubjectName, subjectName);
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
    }
}
