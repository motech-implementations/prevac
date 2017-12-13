package org.motechproject.prevac.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Access;
import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;
import org.motechproject.mds.annotations.NonEditable;
import org.motechproject.mds.annotations.ReadAccess;
import org.motechproject.mds.annotations.UIDisplayable;
import org.motechproject.mds.util.SecurityMode;
import org.motechproject.prevac.domain.enums.Gender;
import org.motechproject.prevac.domain.enums.Language;
import org.motechproject.prevac.util.CustomDateDeserializer;
import org.motechproject.prevac.util.CustomDateSerializer;
import org.motechproject.prevac.util.CustomDateTimeDeserializer;
import org.motechproject.prevac.util.CustomDateTimeSerializer;
import org.motechproject.prevac.util.CustomVisitListDeserializer;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import java.util.ArrayList;
import java.util.List;

/**
 * Models data for registration of Subject in PREVAC
 */
@ReadAccess(value = SecurityMode.PERMISSIONS, members = { "managePrevac" })
@Access(value = SecurityMode.PERMISSIONS, members = { "manageSubjects" })
@Entity(recordHistory = true, name = "Participant")
@NoArgsConstructor
public class Subject {

    private static final String SUBJECT_ID_FIELD_DISPLAY_NAME = "Participant Id";

    /**
     * Fields captured in ZETES
     */

    @Unique
    @NonEditable
    @UIDisplayable(position = 0)
    @Field(required = true, displayName = SUBJECT_ID_FIELD_DISPLAY_NAME)
    @Getter
    @Setter
    private String subjectId;

    @UIDisplayable(position = 1)
    @Field(required = true)
    @Getter
    @Setter
    private String name;

    @UIDisplayable(position = 4)
    @Field
    @Getter
    @Setter
    private String phoneNumber;

    @UIDisplayable(position = 5)
    @Field
    @Getter
    @Setter
    private String address;

    @UIDisplayable(position = 7)
    @Column(length = 20)
    @Field(required = true)
    @Getter
    @Setter
    private Language language;

    @NonEditable
    @UIDisplayable(position = 8)
    @Field
    @Getter
    @Setter
    private String siteId;

    @NonEditable
    @UIDisplayable(position = 9)
    @Field(required = true)
    @Getter
    @Setter
    private String siteName;

    @UIDisplayable(position = 10)
    @Field
    @Getter
    @Setter
    private String community;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String chiefdom;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String section;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String district;

    @NonEditable
    @UIDisplayable(position = 6)
    @Field(required = true)
    @Getter
    @Setter
    private Gender gender;

    @Field(required = true)
    @Getter
    @Setter
    private Integer age;

    @Field
    @Getter
    @Setter
    private String guardianName;

    /**
     * Other fields
     */

    @Field
    @Getter
    @Setter
    private Boolean femaleChildBearingAge;

    @JsonDeserialize(using = CustomVisitListDeserializer.class)
    @Field
    @Persistent(mappedBy = "subject")
    @Cascade(delete = true)
    @Getter
    @Setter
    private List<Visit> visits = new ArrayList<>();

    @NonEditable
    @Field
    @Getter
    @Setter
    private Long stageId;

    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @NonEditable
    @Field
    @Getter
    @Setter
    private LocalDate dateOfBirth;

    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @NonEditable
    @Field
    @Getter
    @Setter
    private LocalDate primerVaccinationDate;

    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @NonEditable
    @Field
    @Getter
    @Setter
    private LocalDate boosterVaccinationDate;

    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @NonEditable
    @Field(displayName = "Date of Discontinuation Vac.")
    @Getter
    @Setter
    private LocalDate dateOfDisconVac;

    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @NonEditable
    @Field(displayName = "Withdrawal Date")
    @Getter
    @Setter
    private LocalDate dateOfDisconStd;

    /**
     * Motech internal fields
     */
    @Field
    @Getter
    @Setter
    private Long id;

    @Field(defaultValue = "false")
    @Getter
    @Setter
    private boolean changed;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @Field
    @Getter
    @Setter
    private DateTime creationDate;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @Field
    @Getter
    @Setter
    private DateTime modificationDate;

    public void setPhoneNumber(String phoneNumber) {
        if (StringUtils.isEmpty(phoneNumber)) {
            this.phoneNumber = null;
        } else {
            this.phoneNumber = phoneNumber;
        }
    }

    public void setAddress(String address) {
        if (StringUtils.isBlank(address)) {
            this.address = null;
        } else {
            this.address = address;
        }
    }

    @Ignore
    public String getLanguageCode() {
        if (language != null) {
            return language.getCode();
        } else {
            return null;
        }

    }

    public void setLanguageCode(String languageCode) {
        //this setter is needed, because json deserialization doesn't work properly without it
    }

    @Override
    public String toString() {
        return subjectId;
    }
}
