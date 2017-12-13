package org.motechproject.prevac.dto;

import lombok.Getter;
import lombok.Setter;

public class ScreeningDto {

    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String volunteerId;

    @Getter
    @Setter
    private String clinicId;

    @Getter
    @Setter
    private String date;

    @Getter
    @Setter
    private String startTime;
}
