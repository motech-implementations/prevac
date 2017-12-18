package org.motechproject.prevac.domain.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public enum VisitType {
    SCREENING("Screening"),
    PRIME_VACCINATION_DAY("Prime Vaccination Day"),
    PRIME_VACCINATION_FIRST_FOLLOW_UP_VISIT("Prime Vaccination First Follow-up visit"),
    PRIME_VACCINATION_SECOND_FOLLOW_UP_VISIT("Prime Vaccination Second Follow-up visit"),
    PRIME_VACCINATION_THIRD_FOLLOW_UP_VISIT("Prime Vaccination Third Follow-up visit"),
    BOOST_VACCINATION_DAY("Boost Vaccination Day"),
    BOOST_VACCINATION_FIRST_FOLLOW_UP_VISIT("Boost Vaccination First Follow-up visit"),
    THREE_MONTHS_POST_PRIME_VISIT("3 Months Post Prime Visit"),
    SIX_MONTHS_POST_PRIME_VISIT("6 Months Post Prime Visit"),
    TWELVE_MONTHS_POST_PRIME_VISIT("12 Months Post Prime Visit"),
    TWENTY_FOUR_MONTHS_POST_PRIME_VISIT("24 Months Post Prime Visit"),
    THIRTY_SIX_MONTHS_POST_PRIME_VISIT("36 Months Post Prime Visit"),
    FORTY_EIGHT_MONTHS_POST_PRIME_VISIT("48 Months Post Prime Visit"),
    SIXTY_MONTHS_POST_PRIME_VISIT("60 Months Post Prime Visit");

    @Getter
    private String displayValue;

    VisitType(String displayValue) {
        this.displayValue = displayValue;
    }

    public static VisitType getByValue(String value) {
        for (VisitType visitType : VisitType.values()) {
            if (visitType.getDisplayValue().equalsIgnoreCase(value)) {
                return visitType;
            }
        }
        return null;
    }

    public static List<String> getDisplayValues() {
        List<String> displayValues = new ArrayList<>();
        for (VisitType visitType : VisitType.values()) {
            displayValues.add(visitType.getDisplayValue());
        }
        return displayValues;
    }
}
