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
    private List<String> boosterRelatedVisits = new ArrayList<>();

    @Getter
    @Setter
    private Boolean showWarnings = true;
}
