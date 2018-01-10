package org.motechproject.prevac.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PrevacConstants {

    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";

    public static final int MAX_TIME_HOUR = 24;
    public static final int TIME_OF_THE_VISIT = 1;

    public static final int EARLIEST_DATE = 1;
    public static final int EARLIEST_DATE_IF_FEMALE_CHILD_BEARING_AGE = 14;
    public static final int LATEST_DATE = 28;

    public static final String CSV_EXPORT_FORMAT = "csv";

    public static final Map<String, Float> REPORT_COLUMN_WIDTHS = new LinkedHashMap<String, Float>() {
        {
            put("Participant Id", 64f); //NO CHECKSTYLE MagicNumber
            put("Gender", 48f); //NO CHECKSTYLE MagicNumber
            put("Age", 24f);
            put("SMS", 32f);
        }
    };

    public static final String SCREENING_TAB_PERMISSION = "prevacScreeningBookingTab";
    public static final String PRIME_VAC_TAB_PERMISSION = "prevacPrimeVaccinationBookingTab";
    public static final String CLINIC_VISIT_SCHEDULE_TAB_PERMISSION = "prevacClinicVisitBookingTab";
    public static final String VISIT_RESCHEDULE_TAB_PERMISSION = "prevacVisitRescheduleBookingTab";
    public static final String ADVANCED_SETTINGS_TAB_PERMISSION = "prevacAdvancedSettings";
    public static final String UNSCHEDULED_VISITS_TAB_PERMISSION = "unscheduledVisitsTab";
    public static final String CAPACITY_INFO_TAB_PERMISSION = "prevacCapacityInfoTab";
    public static final String REPORTS_TAB_PERMISSION = "prevacReportsTab";
    public static final String SUBJECTS_TAB_PERMISSION = "prevacSubjectsTab";

    public static final String HAS_SCREENING_TAB_ROLE = "hasRole('" + SCREENING_TAB_PERMISSION + "')";
    public static final String HAS_PRIME_VAC_TAB_ROLE = "hasRole('" + PRIME_VAC_TAB_PERMISSION + "')";
    public static final String HAS_CLINIC_VISIT_SCHEDULE_TAB_ROLE = "hasRole('" + CLINIC_VISIT_SCHEDULE_TAB_PERMISSION + "')";
    public static final String HAS_VISIT_RESCHEDULE_TAB_ROLE = "hasRole('" + VISIT_RESCHEDULE_TAB_PERMISSION + "')";
    public static final String HAS_ADVANCED_SETTINGS_TAB_ROLE = "hasRole('" + ADVANCED_SETTINGS_TAB_PERMISSION + "')";
    public static final String HAS_UNSCHEDULED_VISITS_TAB_ROLE = "hasRole('" + UNSCHEDULED_VISITS_TAB_PERMISSION + "')";
    public static final String HAS_CAPACITY_INFO_TAB_ROLE = "hasRole('" + CAPACITY_INFO_TAB_PERMISSION + "')";
    public static final String HAS_REPORTS_TAB_ROLE = "hasRole('" + REPORTS_TAB_PERMISSION + "')";
    public static final String HAS_SUBJECTS_TAB_ROLE = "hasRole('" + SUBJECTS_TAB_PERMISSION + "')";

    public static final String SCREENING_NAME = "Screening";
    public static final String PRIME_VACCINATION_SCHEDULE_NAME = "PrimeVaccinationSchedule";
    public static final String UNSCHEDULED_VISITS_NAME = "Unscheduled_Visits";
    public static final String VISIT_RESCHEDULE_NAME = "VisitReschedule";
    public static final String CAPACITY_REPORT_NAME = "CapacityReport";

    public static final Map<String, String> SCREENING_FIELDS_MAP = new LinkedHashMap<String, String>() {
        {
            put("Clinic Location", "clinic.location");
            put("Booking Id", "volunteer.id");
            put("Screening Date", "date");
            put("Screening Time", "startTime");
        }
    };

    public static final Map<String, String> PRIME_VACCINATION_SCHEDULE_FIELDS_MAP = new LinkedHashMap<String, String>() {
        {
            put("Clinic Location", "location");
            put("Participant Id", "participantId");
            put("Participant Name", "participantName");
            put("Female Child Bearing Age", "femaleChildBearingAge");
            put("Actual Screening Date", "actualScreeningDate");
            put("Prime Vac. Date", "date");
            put("Prime Vac. Time", "startTime");
        }
    };

    public static final Map<String, String> VISIT_RESCHEDULE_FIELDS_MAP = new LinkedHashMap<String, String>() {
        {
            put("Clinic Location", "location");
            put("Participant Id", "participantId");
            put("Participant Name", "participantName");
            put("Visit Type", "visitType");
            put("Actual Date", "actualDate");
            put("Planned Date", "plannedDate");
            put("Planned Visit Time", "startTime");
        }
    };

    public static final Map<String, String> UNSCHEDULED_VISIT_FIELDS_MAP = new LinkedHashMap<String, String>() {
        {
            put("Participant Id", "participantId");
            put("Location", "clinicName");
            put("Date", "date");
            put("Start Time", "startTime");
            put("Purpose of the visit", "purpose");
        }
    };

    public static final Map<String, String> CAPACITY_REPORT_FIELDS_MAP = new LinkedHashMap<String, String>() {
        {
            put("Date", "date");
            put("Clinic Location", "location");
            put("Max. Capacity", "maxCapacity");
            put("Available Capacity", "availableCapacity");
            put("Screening Slot Remaining", "screeningSlotRemaining");
            put("Vaccine Slot Remaining", "vaccineSlotRemaining");
        }
    };

    public static final List<String> AVAILABLE_LOOKUPS_FOR_PRIME_VACCINATION_SCHEDULE = new ArrayList<>(Arrays.asList(
            "Find By Participant Id", "Find By Participant Name"));

    public static final List<String> AVAILABLE_LOOKUPS_FOR_SCREENINGS = new ArrayList<>(Arrays.asList(
            "Find By Clinic Location", "Find By Volunteer Name", "Find By Booking Id"));

    public static final List<String> AVAILABLE_LOOKUPS_FOR_VISIT_RESCHEDULE = new ArrayList<>(Arrays.asList("Find By Participant Id",
            "Find By Participant Name", "Find By Visit Type", "Find By Clinic Location", "Find By Visit Actual Date",
            "Find By Visit Actual Date Range"));

    public static final List<String> AVAILABLE_LOOKUPS_FOR_UNSCHEDULED = new ArrayList<>(Arrays.asList(
            "Find By Participant Id", "Find By Clinic Location"));

    public static final List<String> AVAILABLE_LOOKUPS_FOR_CAPACITY_REPORT = new ArrayList<>(Collections.singletonList(
            "Find By Location"));

    private PrevacConstants() {
    }
}
