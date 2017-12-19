package org.motechproject.prevac.validation;

import org.apache.commons.lang.StringUtils;
import org.motechproject.prevac.domain.enums.Gender;
import org.motechproject.prevac.domain.enums.Language;
import org.motechproject.prevac.web.domain.SubjectZetesDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SubjectValidator {

    private static final List<ValidationError> VALIDATION_ERRORS = new ArrayList<>();
    private static final Map<String, Set<String>> SITES_IN_COUNTRIES = new HashMap<String, Set<String>>() {
        {
            put("1", new HashSet<>(Arrays.asList("01", "02")));
            put("2", new HashSet<>(Collections.singletonList("03")));
            put("3", new HashSet<>(Collections.singletonList("04")));
            put("5", new HashSet<>(Arrays.asList("05", "06")));
        }
    };

    private SubjectValidator() {
    }

    public static List<ValidationError> validate(SubjectZetesDto subjectToValidate) {
        validateSubjectID(subjectToValidate.getSubjectId());
        validateName(subjectToValidate.getName());
        validateAge(subjectToValidate.getAge());
        validateGender(subjectToValidate.getGender());
        validateLanguage(subjectToValidate.getLanguage());
        validateSiteName(subjectToValidate.getSiteName());
        validateCommunity(subjectToValidate.getCommunity());
        validatePhoneNumber(subjectToValidate.getPhoneNumber());

        return VALIDATION_ERRORS;
    }

    //region validation methods
    private static void validateGender(String gender) {
        if (StringUtils.isBlank(gender)) {
            addValidationError(ValidationError.GENDER_NULL);
        } else if (!Gender.getListOfValues().contains(gender)) {
            addValidationError(ValidationError.GENDER_NOT_CORRECT);
        }
    }

    private static void validateLanguage(String language) {
        if (StringUtils.isBlank(language)) {
            addValidationError(ValidationError.LANGUAGE_NULL);
        } else if (!Language.getListOfCodes().contains(language)) {
            addValidationError(ValidationError.LANGUAGE_NOT_CORRECT);
        }
    }

    private static void validateAge(Integer age) {
        if (age == null) {
            addValidationError(ValidationError.AGE_NULL);
        } else if (age < 0) {
            addValidationError(ValidationError.AGE_NEGATIVE);
        }
    }

    private static void validateName(String name) {
        if (StringUtils.isBlank(name)) {
            addValidationError(ValidationError.NAME_NULL);
        } else if (name.matches(".*\\d.*")) {
            addValidationError(ValidationError.NAME_HAS_DIGITS);
        }
    }

    private static void validateSubjectID(String subjectId) {
        if (StringUtils.isBlank(subjectId)) {
            addValidationError(ValidationError.SUBJECT_ID_NULL);
            return;
        }
        String plainSubjectId = removeDashesFromSubjectId(subjectId);
        if (plainSubjectId.length() != 8) {
            addValidationError(ValidationError.SUBJECT_ID_NOT_VERIFIED);
        } else if (!(validateCountryAndSiteNumber(plainSubjectId) && validateChecksum(plainSubjectId))) {
            addValidationError(ValidationError.SUBJECT_ID_NOT_VERIFIED);
        }
    }

    private static void validateSiteName(String siteName) {
        if (StringUtils.isBlank(siteName)) {
            addValidationError(ValidationError.SITE_NAME_NULL);
        }
    }

    private static void validateCommunity(String community) {
        if (StringUtils.isBlank(community)) {
            addValidationError(ValidationError.COMMUNITY_NULL);
        }
    }

    private static void validatePhoneNumber(String phoneNumber) {
        if (!phoneNumber.matches("[0-9]+")) {
            addValidationError(ValidationError.PHONE_NUMBER_HAS_NON_DIGITS);
        }
    }

    /**
     * Method uses Luhn Algorithm to validate subject's id.
     */
    private static boolean validateChecksum(String subjectId) {
        int sum = 0;
        int parity = subjectId.length() % 2;
        for (int i = 0; i < subjectId.length(); i++) {
            int digit = Integer.parseInt(subjectId.substring(i, i + 1));
            if (i % 2 == parity) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
        }
        return sum % 10 == 0;
    }

    private static boolean validateCountryAndSiteNumber(String subjectId) {
        String countryNumber = subjectId.substring(0, 1);
        String siteNumber = subjectId.substring(1, 3);

        return SITES_IN_COUNTRIES.keySet().contains(countryNumber)
                && SITES_IN_COUNTRIES.get(countryNumber).contains(siteNumber);
    }
    //endregion

    private static String removeDashesFromSubjectId(String subjectId) {
        return subjectId.replaceAll("[\\s\\-]", "");
    }

    private static void addValidationError(String message) {
        VALIDATION_ERRORS.add(new ValidationError(message));
    }
}
