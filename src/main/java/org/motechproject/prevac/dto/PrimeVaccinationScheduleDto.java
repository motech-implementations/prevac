package org.motechproject.prevac.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDate;
import org.motechproject.commons.date.model.Time;
import org.motechproject.prevac.domain.Visit;
import org.motechproject.prevac.domain.enums.Gender;
import org.motechproject.prevac.domain.enums.VisitType;
import org.motechproject.prevac.util.CustomBooleanDeserializer;
import org.motechproject.prevac.util.CustomBooleanSerializer;
import org.motechproject.prevac.util.CustomDateDeserializer;
import org.motechproject.prevac.util.CustomDateSerializer;
import org.motechproject.prevac.util.CustomTimeSerializer;

@NoArgsConstructor
public class PrimeVaccinationScheduleDto {

    @Getter
    @Setter
    private Long visitBookingDetailsId;

    @Getter
    @Setter
    private String location;

    @Getter
    @Setter
    private String participantId;

    @Getter
    @Setter
    private String participantName;

    @Getter
    @Setter
    private Gender participantGender;

    @JsonSerialize(using = CustomBooleanSerializer.class)
    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    @Getter
    @Setter
    private Boolean femaleChildBearingAge;

    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Getter
    @Setter
    private LocalDate actualScreeningDate;

    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Getter
    @Setter
    private LocalDate bookingScreeningActualDate;

    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Getter
    @Setter
    private LocalDate date;

    @JsonSerialize(using = CustomTimeSerializer.class)
    @Getter
    @Setter
    private Time startTime;

    @Getter
    @Setter
    private Boolean ignoreDateLimitation;

    public PrimeVaccinationScheduleDto(Visit details) {

        for (Visit visit : details.getSubject().getVisits()) {
            if (VisitType.SCREENING.equals(visit.getType())) {
                setActualScreeningDate(visit.getDate());
                break;
            }
        }
        if (actualScreeningDate != null) {
            setBookingScreeningActualDate(actualScreeningDate);
        } else {
            for (Visit bookingDetails : details.getSubject().getVisits()) {
                if (VisitType.SCREENING.equals(bookingDetails.getType())) {
                    setBookingScreeningActualDate(bookingDetails.getBookingActualDate());
                    break;
                }
            }
        }

        setStartTime(details.getStartTime());
        setParticipantId(details.getSubject().getSubjectId());
        setParticipantName(details.getSubject().getName());
        setDate(details.getBookingPlannedDate());
        if (details.getSubject().getGender() == null || details.getSubject().getGender().equals(Gender.Female)) {
            setFemaleChildBearingAge(details.getSubject().getFemaleChildBearingAge());
        } else {
            setFemaleChildBearingAge(false);
        }
        setVisitBookingDetailsId(details.getId());
        setParticipantGender(details.getSubject().getGender());
        if (details.getIgnoreDateLimitation() != null) {
            setIgnoreDateLimitation(details.getIgnoreDateLimitation());
        } else {
            setIgnoreDateLimitation(false);
        }
        if (details.getClinic() != null) {
            setLocation(details.getSubject().getSiteName());
        }
    }
}
