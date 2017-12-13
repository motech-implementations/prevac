package org.motechproject.prevac.domain.enums;

import lombok.Getter;

public enum VisitType {
    SCREENING("Screening"),
    PRIME_VACCINATION_DAY("Prime Vaccination Day"),
    PRIME_VACCINATION_FIRST_FOLLOW_UP_VISIT("Prime Vaccination First Follow-up visit"),
    PRIME_VACCINATION_SECOND_FOLLOW_UP_VISIT("Prime Vaccination Second Follow-up visit"),
    BOOST_VACCINATION_DAY("Boost Vaccination Day"),
    BOOST_VACCINATION_FIRST_FOLLOW_UP_VISIT("Boost Vaccination First Follow-up visit"),
    BOOST_VACCINATION_SECOND_FOLLOW_UP_VISIT("Boost Vaccination Second Follow-up visit"),
    BOOST_VACCINATION_THIRD_FOLLOW_UP_VISIT("Boost Vaccination Third Follow-up visit"),
    FIRST_LONG_TERM_FOLLOW_UP_VISIT("First Long-term Follow-up visit"),
    SECOND_LONG_TERM_FOLLOW_UP_VISIT("Second Long-term Follow-up visit"),
    THIRD_LONG_TERM_FOLLOW_UP_VISIT("Third Long-term Follow-up visit"),
    FOURTH_LONG_TERM_FOLLOW_UP_VISIT("Fourth Long-term Follow-up visit"),
    FIFTH_LONG_TERM_FOLLOW_UP_VISIT("Fifth Long-term Follow-up visit"),
    SIXTH_LONG_TERM_FOLLOW_UP_VISIT("Sixth Long-term Follow-up visit"),
    SEVENTH_LONG_TERM_FOLLOW_UP_VISIT("Seventh Long-term Follow-up visit"),
    THIRD_VACCINATION_DAY("Third Vaccination Day"),
    FIRST_POST_THIRD_VACCINATION_VISIT("First Post Third Vaccination visit"),
    SECOND_POST_THIRD_VACCINATION_VISIT("Second Post Third Vaccination visit"),
    THIRD_POST_THIRD_VACCINATION_VISIT("Third Post Third Vaccination visit"),
    FOURTH_POST_THIRD_VACCINATION_VISIT("Fourth Post Third Vaccination visit"),
    FIFTH_POST_THIRD_VACCINATION_VISIT("Fifth Post Third Vaccination visit"),
    UNSCHEDULED_VISIT("Unscheduled Visit");

    @Getter
    private String displayValue;

    VisitType(String displayValue) {
        this.displayValue = displayValue;
    }

    public static VisitType getByValue(String value) {
        if (value != null && value.startsWith(UNSCHEDULED_VISIT.getDisplayValue())) {
            return UNSCHEDULED_VISIT;
        }
        for (VisitType visitType : VisitType.values()) {
            if (visitType.getDisplayValue().equalsIgnoreCase(value)) {
                return visitType;
            }
        }
        return null;
    }
}
