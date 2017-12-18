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

    @UIDisplayable(position = 9)
    @Field(required = true, defaultValue = "5")
    @Getter
    @Setter
    private Integer maxPrimeThirdFollowUpVisits;

    @UIDisplayable(position = 5)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxBoosterVisits;

    @UIDisplayable(position = 10)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxBoosterFirstFollowUpVisits;

    @UIDisplayable(position = 11)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxThreeMonthsPostPrimeVisits;

    @UIDisplayable(position = 12)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxSixMonthsPostPrimeVisits;

    @UIDisplayable(position = 13)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxTwelveMonthsPostPrimeVisit;

    @UIDisplayable(position = 14)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxTwentyFourMonthsPostPrimeVisits;

    @UIDisplayable(position = 15)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxThirtySixMonthsPostPrimeVisits;

    @UIDisplayable(position = 16)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxFortyEightMonthsPostPrimeVisits;

    @UIDisplayable(position = 17)
    @Field(required = true, defaultValue = "10")
    @Getter
    @Setter
    private Integer maxSixtyMonthsPostPrimeVisits;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;

    public Clinic(String siteId, String location, Integer numberOfRooms, Integer maxCapacityByDay, Integer maxScreeningVisits, //NO CHECKSTYLE ParameterNumber
                  Integer maxPrimeVisits, Integer maxPrimeFirstFollowUpVisits, Integer maxPrimeSecondFollowUpVisits,
                  Integer maxPrimeThirdFollowUpVisits, Integer maxBoosterVisits, Integer maxBoosterFirstFollowUpVisits,
                  Integer maxThreeMonthsPostPrimeVisits, Integer maxSixMonthsPostPrimeVisits, Integer maxTwelveMonthsPostPrimeVisit,
                  Integer maxTwentyFourMonthsPostPrimeVisits, Integer maxThirtySixMonthsPostPrimeVisits, Integer maxFortyEightMonthsPostPrimeVisits,
                  Integer maxSixtyMonthsPostPrimeVisits) {
        this.siteId = siteId;
        this.location = location;
        this.numberOfRooms = numberOfRooms;
        this.maxCapacityByDay = maxCapacityByDay;
        this.maxScreeningVisits = maxScreeningVisits;
        this.maxPrimeVisits = maxPrimeVisits;
        this.maxPrimeFirstFollowUpVisits = maxPrimeFirstFollowUpVisits;
        this.maxPrimeSecondFollowUpVisits = maxPrimeSecondFollowUpVisits;
        this.maxPrimeThirdFollowUpVisits = maxPrimeThirdFollowUpVisits;
        this.maxBoosterVisits = maxBoosterVisits;
        this.maxBoosterFirstFollowUpVisits = maxBoosterFirstFollowUpVisits;
        this.maxThreeMonthsPostPrimeVisits = maxThreeMonthsPostPrimeVisits;
        this.maxSixMonthsPostPrimeVisits = maxSixMonthsPostPrimeVisits;
        this.maxTwelveMonthsPostPrimeVisit = maxTwelveMonthsPostPrimeVisit;
        this.maxTwentyFourMonthsPostPrimeVisits = maxTwentyFourMonthsPostPrimeVisits;
        this.maxThirtySixMonthsPostPrimeVisits = maxThirtySixMonthsPostPrimeVisits;
        this.maxFortyEightMonthsPostPrimeVisits = maxFortyEightMonthsPostPrimeVisits;
        this.maxSixtyMonthsPostPrimeVisits = maxSixtyMonthsPostPrimeVisits;
    }

    @Override
    public String toString() {
        return location;
    }
}

