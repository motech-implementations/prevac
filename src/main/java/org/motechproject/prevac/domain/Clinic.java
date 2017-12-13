package org.motechproject.prevac.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.NonEditable;
import org.motechproject.mds.annotations.UIDisplayable;

import javax.jdo.annotations.Unique;

@Entity(recordHistory = true)
@NoArgsConstructor
public class Clinic {

    @Field
    @Getter
    @Setter
    private Long id;

    @Unique
    @UIDisplayable(position = 0)
    @Field(required = true)
    @Getter
    @Setter
    private String siteId;

    @UIDisplayable(position = 1)
    @Field(required = true)
    @Getter
    @Setter
    private String location;

    @UIDisplayable(position = 6)
    @Field(displayName = "Amount of Rooms", required = true, defaultValue = "1")
    @Getter
    @Setter
    private Integer numberOfRooms;

    @UIDisplayable(position = 2)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxCapacityByDay;

    @UIDisplayable(position = 3)
    @Field(required = true, defaultValue = "5")
    @Getter
    @Setter
    private Integer maxScreeningVisits;

    @UIDisplayable(position = 4)
    @Field(required = true, defaultValue = "5")
    @Getter
    @Setter
    private Integer maxPrimeVisits;

    @UIDisplayable(position = 7)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxPrimeFirstFollowUpVisits;

    @UIDisplayable(position = 8)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxPrimeSecondFollowUpVisits;

    @UIDisplayable(position = 5)
    @Field(required = true, defaultValue = "5")
    @Getter
    @Setter
    private Integer maxBoosterVisits;

    @UIDisplayable(position = 9)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxBoosterFirstFollowUpVisits;

    @UIDisplayable(position = 10)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxBoosterSecondFollowUpVisits;

    @UIDisplayable(position = 11)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxBoosterThirdFollowUpVisits;

    @UIDisplayable(position = 12)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxFirstLongTermFollowUpVisits;

    @UIDisplayable(position = 13)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxSecondLongTermFollowUpVisits;

    @UIDisplayable(position = 14)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxThirdLongTermFollowUpVisits;

    @UIDisplayable(position = 15)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxFourthLongTermFollowUpVisits;

    @UIDisplayable(position = 16)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxFifthLongTermFollowUpVisits;

    @UIDisplayable(position = 17)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxSixthLongTermFollowUpVisits;

    @UIDisplayable(position = 18)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxSeventhLongTermFollowUpVisits;

    @UIDisplayable(position = 19)
    @Field(required = true, defaultValue = "5")
    @Getter
    @Setter
    private Integer maxThirdVaccinationVisits;

    @UIDisplayable(position = 20)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxFirstPostThirdVaccinationVisits;

    @UIDisplayable(position = 21)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxSecondPostThirdVaccinationVisits;

    @UIDisplayable(position = 22)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxThirdPostThirdVaccinationVisits;

    @UIDisplayable(position = 23)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxFourthPostThirdVaccinationVisits;

    @UIDisplayable(position = 24)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxFifthPostThirdVaccinationVisits;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;

    public Clinic(String siteId, String location, Integer numberOfRooms, Integer maxCapacityByDay, Integer maxScreeningVisits, //NO CHECKSTYLE ParameterNumber
                  Integer maxPrimeVisits, Integer maxPrimeFirstFollowUpVisits, Integer maxPrimeSecondFollowUpVisits, Integer maxBoosterVisits,
                  Integer maxBoosterFirstFollowUpVisits, Integer maxBoosterSecondFollowUpVisits, Integer maxBoosterThirdFollowUpVisits,
                  Integer maxFirstLongTermFollowUpVisits, Integer maxSecondLongTermFollowUpVisits, Integer maxThirdLongTermFollowUpVisits,
                  Integer maxFourthLongTermFollowUpVisits, Integer maxFifthLongTermFollowUpVisits, Integer maxSixthLongTermFollowUpVisits,
                  Integer maxSeventhLongTermFollowUpVisits, Integer maxThirdVaccinationVisits, Integer maxFirstPostThirdVaccinationVisits,
                  Integer maxSecondPostThirdVaccinationVisits, Integer maxThirdPostThirdVaccinationVisits,
                  Integer maxFourthPostThirdVaccinationVisits, Integer maxFifthPostThirdVaccinationVisits) {
        this.siteId = siteId;
        this.location = location;
        this.numberOfRooms = numberOfRooms;
        this.maxCapacityByDay = maxCapacityByDay;
        this.maxScreeningVisits = maxScreeningVisits;
        this.maxPrimeVisits = maxPrimeVisits;
        this.maxPrimeFirstFollowUpVisits = maxPrimeFirstFollowUpVisits;
        this.maxPrimeSecondFollowUpVisits = maxPrimeSecondFollowUpVisits;
        this.maxBoosterVisits = maxBoosterVisits;
        this.maxBoosterFirstFollowUpVisits = maxBoosterFirstFollowUpVisits;
        this.maxBoosterSecondFollowUpVisits = maxBoosterSecondFollowUpVisits;
        this.maxBoosterThirdFollowUpVisits = maxBoosterThirdFollowUpVisits;
        this.maxFirstLongTermFollowUpVisits = maxFirstLongTermFollowUpVisits;
        this.maxSecondLongTermFollowUpVisits = maxSecondLongTermFollowUpVisits;
        this.maxThirdLongTermFollowUpVisits = maxThirdLongTermFollowUpVisits;
        this.maxFourthLongTermFollowUpVisits = maxFourthLongTermFollowUpVisits;
        this.maxFifthLongTermFollowUpVisits = maxFifthLongTermFollowUpVisits;
        this.maxSixthLongTermFollowUpVisits = maxSixthLongTermFollowUpVisits;
        this.maxSeventhLongTermFollowUpVisits = maxSeventhLongTermFollowUpVisits;
        this.maxThirdVaccinationVisits = maxThirdVaccinationVisits;
        this.maxFirstPostThirdVaccinationVisits = maxFirstPostThirdVaccinationVisits;
        this.maxSecondPostThirdVaccinationVisits = maxSecondPostThirdVaccinationVisits;
        this.maxThirdPostThirdVaccinationVisits = maxThirdPostThirdVaccinationVisits;
        this.maxFourthPostThirdVaccinationVisits = maxFourthPostThirdVaccinationVisits;
        this.maxFifthPostThirdVaccinationVisits = maxFifthPostThirdVaccinationVisits;
    }

    @Override
    public String toString() {
        return location;
    }
}

