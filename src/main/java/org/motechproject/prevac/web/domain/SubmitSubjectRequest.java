package org.motechproject.prevac.web.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.motechproject.prevac.domain.enums.Language;
import org.motechproject.prevac.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for request coming from Zetes
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class SubmitSubjectRequest {

    private static final double VALIDATION_FACTOR = 97D;

    @JsonProperty("PID")
    @Getter
    @Setter
    private String subjectId;

    @Getter
    @Setter
    private String siteName;

    @Getter
    @Setter
    private String siteId;

    @Getter
    @Setter
    private String district;

    @Getter
    @Setter
    private String chiefdom;

    @Getter
    @Setter
    private String section;

    @Getter
    @Setter
    private String community;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Integer age;

    @Getter
    @Setter
    private String gender;

    @Getter
    @Setter
    private String address;

    @Getter
    @Setter
    private String phoneNumber;

    @Getter
    @Setter
    private String language;

    @Getter
    @Setter
    private String guardianName;

    @Getter
    private List<ValidationError> validationErrors = new ArrayList<>();

    public List<ValidationError> validate() {
        if (StringUtils.isBlank(siteName)) {
            validationErrors.add(new ValidationError(ValidationError.SITE_NAME_NULL));
        }

        if (StringUtils.isBlank(community)) {
            validationErrors.add(new ValidationError(ValidationError.COMMUNITY_NULL));
        }

        if (StringUtils.isBlank(name)) {
            validationErrors.add(new ValidationError(ValidationError.NAME_NULL));
        } else if (name.matches(".*\\d.*")) {
            validationErrors.add(new ValidationError(ValidationError.NAME_HAS_DIGITS));
        }

        if (age == null) {
            validationErrors.add(new ValidationError(ValidationError.AGE_NULL));
        } else if (age < 0) {
            validationErrors.add(new ValidationError(ValidationError.AGE_NEGATIVE));
        }

        validateEnums();

        validateSubjectID();

        return validationErrors;
    }

    private void validateEnums() {
        if (StringUtils.isBlank(gender)) {
            validationErrors.add(new ValidationError(ValidationError.GENDER_NULL));
        } else if (!Language.getListOfCodes().contains(gender)) {
            validationErrors.add(new ValidationError(ValidationError.GENDER_NOT_CORRECT));
        }

        if (StringUtils.isBlank(language)) {
            validationErrors.add(new ValidationError(ValidationError.LANGUAGE_NULL));
        } else if (!Language.getListOfCodes().contains(language)) {
            validationErrors.add(new ValidationError(ValidationError.LANGUAGE_NOT_CORRECT));
        }
    }

    private void validateSubjectID() {

        if (StringUtils.isBlank(subjectId)) {
            validationErrors.add(new ValidationError(ValidationError.SUBJECT_ID_NULL));
        } else if (!StringUtils.isNumeric(subjectId)) {
            validationErrors.add(new ValidationError(ValidationError.SUBJECT_ID_NOT_VERIFIED));
        }
    }

}
