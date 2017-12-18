package org.motechproject.prevac.validation;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.motechproject.prevac.constants.PrevacConstants;
import org.motechproject.prevac.domain.enums.Gender;
import org.motechproject.prevac.domain.enums.Language;
import org.motechproject.prevac.web.domain.SubmitSubjectRequest;

import java.util.ArrayList;
import java.util.List;

public class SubjectValidator {

    @Getter
    private final List<ValidationError> validationErrors;
    private SubmitSubjectRequest subjectRequest;

    public SubjectValidator() {
        validationErrors = new ArrayList<>();
    }

    public List<ValidationError> validate(SubmitSubjectRequest subjectRequestToValidate) {
        subjectRequest = subjectRequestToValidate;
        validateSubjectID();
        validateName();
        validateAge();
        validateGender();
        validateLanguage();
        validateSiteName();
        validateCommunity();
        validatePhoneNumber();

        return validationErrors;
    }

    //region validation methods
    private void validateGender() {
        String gender = subjectRequest.getGender();
        if (StringUtils.isBlank(gender)) {
            addValidationError(ValidationError.GENDER_NULL);
        } else if (!Gender.getListOfValues().contains(gender)) {
            addValidationError(ValidationError.GENDER_NOT_CORRECT);
        }
    }

    private void validateLanguage() {
        String language = subjectRequest.getLanguage();
        if (StringUtils.isBlank(language)) {
            addValidationError(ValidationError.LANGUAGE_NULL);
        } else if (!Language.getListOfCodes().contains(language)) {
            addValidationError(ValidationError.LANGUAGE_NOT_CORRECT);
        }
    }

    private void validateAge() {
        Integer age = subjectRequest.getAge();
        if (age == null) {
            addValidationError(ValidationError.AGE_NULL);
        } else if (age < 0) {
            addValidationError(ValidationError.AGE_NEGATIVE);
        }
    }

    private void validateName() {
        String name = subjectRequest.getName();
        if (StringUtils.isBlank(name)) {
            addValidationError(ValidationError.NAME_NULL);
        } else if (name.matches(".*\\d.*")) {
            addValidationError(ValidationError.NAME_HAS_DIGITS);
        }
    }

    private void validateSubjectID() {
        String subjectId = subjectRequest.getSubjectId();
        if (StringUtils.isBlank(subjectId)) {
            addValidationError(ValidationError.SUBJECT_ID_NULL);
            return;
        }
        subjectId = removeDashesFromSubjectId(subjectId);
        if (subjectId.length() != 8) {
            addValidationError(ValidationError.SUBJECT_ID_NOT_VERIFIED);
        } else if (!(validateCountryAndSiteNumber(subjectId) && validateChecksum(subjectId))) {
            addValidationError(ValidationError.SUBJECT_ID_NOT_VERIFIED);
        }
    }

    private void validateSiteName() {
        if (StringUtils.isBlank(subjectRequest.getSiteName())) {
            addValidationError(ValidationError.SITE_NAME_NULL);
        }
    }

    private void validateCommunity() {
        if (StringUtils.isBlank(subjectRequest.getCommunity())) {
            addValidationError(ValidationError.COMMUNITY_NULL);
        }
    }

    private void validatePhoneNumber() {
        if (!subjectRequest.getPhoneNumber().matches("[0-9]+")) {
            addValidationError(ValidationError.PHONE_NUMBER_HAS_NON_DIGITS);
        }
    }

    /**
     * Method uses Luhn Algorithm to validate subject's id.
     */
    private boolean validateChecksum(String subjectId) {
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

    private boolean validateCountryAndSiteNumber(String subjectId) {
        String countryNumber = subjectId.substring(0, 1);
        String siteNumber = subjectId.substring(1, 3);

        return PrevacConstants.SITES_IN_COUNTRIES.keySet().contains(countryNumber)
                && PrevacConstants.SITES_IN_COUNTRIES.get(countryNumber).contains(siteNumber);
    }
    //endregion

    private String removeDashesFromSubjectId(String subjectId) {
        return subjectId.replaceAll("[\\s\\-]", "");
    }

    private void addValidationError(String message) {
        validationErrors.add(new ValidationError(message));
    }
}
