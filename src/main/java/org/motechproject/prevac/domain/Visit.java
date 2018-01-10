package org.motechproject.prevac.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDate;
import org.motechproject.commons.date.model.Time;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.EnumDisplayName;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.NonEditable;
import org.motechproject.prevac.domain.enums.VisitType;
import org.motechproject.prevac.util.serializer.CustomDateDeserializer;
import org.motechproject.prevac.util.serializer.CustomDateSerializer;
import org.motechproject.prevac.util.serializer.CustomSubjectSerializer;
import org.motechproject.prevac.util.serializer.CustomVisitTypeDeserializer;
import org.motechproject.prevac.util.serializer.CustomVisitTypeSerializer;

@Entity(recordHistory = true, maxFetchDepth = 2)
@NoArgsConstructor
public class Visit {

    public static final String VISIT_TYPE_PROPERTY_NAME = "type";
    public static final String VISIT_PLANNED_DATE_PROPERTY_NAME = "dateProjected";
    public static final String SUBJECT_PRIME_VACCINATION_DATE_PROPERTY_NAME = "subject.primerVaccinationDate";
    public static final String SUBJECT_NAME_PROPERTY_NAME = "subject.name";

    @Field
    @Getter
    @Setter
    private Long id;

    @Field
    @Getter
    @Setter
    private Time startTime;

    @Field
    @Getter
    @Setter
    private Time endTime;

    @Field
    @Getter
    @Setter
    private Clinic clinic;

    @JsonSerialize(using = CustomSubjectSerializer.class)
    @Field(required = true, displayName = "Participant")
    @Getter
    @Setter
    private Subject subject;

    @JsonSerialize(using = CustomVisitTypeSerializer.class)
    @JsonDeserialize(using = CustomVisitTypeDeserializer.class)
    @Field(displayName = "Visit Type", required = true)
    @EnumDisplayName(enumField = "displayValue")
    @Getter
    @Setter
    private VisitType type;

    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Field(displayName = "Actual Visit Date")
    @Getter
    @Setter
    private LocalDate date;

    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Field(displayName = "Planned Visit Date")
    @Getter
    @Setter
    private LocalDate dateProjected;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;

    @Field
    @Getter
    @Setter
    private Boolean ignoreDateLimitation = false;

    @Override
    public String toString() {
        return type.getDisplayValue() +
                (getDateProjected() != null ? " / Planned Date: " + getDateProjected().toString() : "") +
                (getDate() != null ? " / Actual Date: " + getDate().toString() : "");
    }
}
