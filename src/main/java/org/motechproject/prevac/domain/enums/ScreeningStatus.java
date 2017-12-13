package org.motechproject.prevac.domain.enums;

import lombok.Getter;

public enum ScreeningStatus {
    ACTIVE("Active"),
    CANCELED("Canceled");

    @Getter
    private String value;

    ScreeningStatus(String value) {
        this.value = value;
    }
}
