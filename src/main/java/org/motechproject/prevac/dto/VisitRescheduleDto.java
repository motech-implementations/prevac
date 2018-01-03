package org.motechproject.prevac.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;
import org.motechproject.commons.date.model.Time;
import org.motechproject.prevac.domain.Visit;
import org.motechproject.prevac.domain.enums.VisitType;
import org.motechproject.prevac.util.CustomDateDeserializer;
import org.motechproject.prevac.util.CustomDateSerializer;
import org.motechproject.prevac.util.CustomTimeSerializer;
import org.motechproject.prevac.util.CustomVisitTypeDeserializer;
import org.motechproject.prevac.util.CustomVisitTypeSerializer;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class VisitRescheduleDto {

    @Getter
    @Setter
    private String location;

    @Getter
    @Setter
    private String participantId;

    @Getter
    @Setter
    private String participantName;

    @JsonSerialize(using = CustomVisitTypeSerializer.class)
    @JsonDeserialize(using = CustomVisitTypeDeserializer.class)
    @Getter
    @Setter
    private VisitType visitType;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Getter
    @Setter
    private LocalDate actualDate;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Getter
    @Setter
    private LocalDate plannedDate;

    @JsonSerialize(using = CustomTimeSerializer.class)
    @Getter
    @Setter
    private Time startTime;

    @Getter
    @Setter
    private Long visitBookingDetailsId;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Getter
    @Setter
    private LocalDate earliestDate;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Getter
    @Setter
    private LocalDate latestDate;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Getter
    @Setter
    private LocalDate earliestWindowDate;

    @Getter
    @Setter
    private Boolean ignoreDateLimitation;

    @Getter
    @Setter
    private Boolean boosterRelated;

    @Getter
    @Setter
    private Boolean notVaccinated;

    public VisitRescheduleDto(Visit details) {
        setParticipantId(details.getSubject().getSubjectId());
        setParticipantName(details.getSubject().getName());
        setVisitType(details.getType());
        setActualDate(details.getDate());
        setPlannedDate(details.getDateProjected());
        setStartTime(details.getStartTime());
        setVisitBookingDetailsId(details.getId());
        if (details.getIgnoreDateLimitation() != null) {
            setIgnoreDateLimitation(details.getIgnoreDateLimitation());
        } else {
            setIgnoreDateLimitation(false);
        }
        if (details.getClinic() != null) {
            setLocation(details.getSubject().getSiteName());
        }
    }

    public VisitRescheduleDto(Visit details, Range<LocalDate> dateRange,
                              Boolean boosterRelated, Boolean notVaccinated) {
        this(details);
        this.boosterRelated = boosterRelated;
        this.notVaccinated = notVaccinated;
        calculateEarliestAndLatestDate(dateRange);
    }

    private void calculateEarliestAndLatestDate(Range<LocalDate> dateRange) {
        if (dateRange != null) {
            LocalDate maxDate = dateRange.getMax();
            LocalDate minDate = dateRange.getMin();
            earliestWindowDate = minDate;

            if (minDate.isBefore(LocalDate.now())) {
                minDate = LocalDate.now();
            }
            earliestDate = minDate;

            if (!maxDate.isBefore(LocalDate.now())) {
                latestDate = maxDate;
            }
        }
    }
}
