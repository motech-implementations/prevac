package org.motechproject.prevac.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class CapacityInfoDto {

    @Getter
    @Setter
    private String clinic;

    @Getter
    @Setter
    private Integer maxCapacity;

    @Getter
    @Setter
    private Integer availableCapacity;

    @Getter
    @Setter
    private Integer screeningSlotRemaining;

    @Getter
    @Setter
    private Integer vaccineSlotRemaining;

    public CapacityInfoDto(String clinic, Integer maxCapacity, Integer availableCapacity, Integer screeningSlotRemaining, Integer vaccineSlotRemaining) {
        this.clinic = clinic;
        this.maxCapacity = maxCapacity;
        this.availableCapacity = availableCapacity;
        this.screeningSlotRemaining = screeningSlotRemaining;
        this.vaccineSlotRemaining = vaccineSlotRemaining;
    }
}
