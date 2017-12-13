package org.motechproject.prevac.domain;

import lombok.Getter;
import lombok.Setter;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.NonEditable;
import org.motechproject.prevac.domain.enums.VisitType;

import javax.jdo.annotations.Unique;

@Entity
@Unique(name = "visitTypeAndStageId", members = { "visitType", "stageId" })
public class VisitScheduleOffset {

    @Field(required = true)
    @Getter
    @Setter
    private VisitType visitType;

    @Field(required = true)
    @Getter
    @Setter
    private Long stageId;

    @Field(required = true)
    @Getter
    @Setter
    private Integer timeOffset;

    @Field(required = true)
    @Getter
    @Setter
    private Integer earliestDateOffset;

    @Field(required = true)
    @Getter
    @Setter
    private Integer latestDateOffset;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;
}
