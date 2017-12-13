package org.motechproject.prevac.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Config {

    @Getter
    @Setter
    private List<String> clinicMainFields = new ArrayList<>();

    @Getter
    @Setter
    private List<String> clinicExtendedFields = new ArrayList<>();

    @Getter
    @Setter
    private List<String> boosterRelatedMessages = new ArrayList<>();

    @Getter
    @Setter
    private List<String> thirdVaccinationRelatedMessages = new ArrayList<>();

    @Getter
    @Setter
    private Long activeStageId;
}
