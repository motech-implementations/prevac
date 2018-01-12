package org.motechproject.prevac.scheduler;

import org.joda.time.Period;
import org.motechproject.event.MotechEvent;
import org.motechproject.prevac.constants.PrevacConstants;
import org.motechproject.scheduler.contract.RepeatingPeriodSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class PrevacScheduler {
    private MotechSchedulerService motechSchedulerService;

    @Autowired
    public PrevacScheduler(MotechSchedulerService motechSchedulerService) {
        this.motechSchedulerService = motechSchedulerService;
    }

    public void scheduleZetesUpdateJob(Date startDate, String zetesUrl, String zetesUsername, String zetesPassword) {
        Map<String, Object> eventParameters = new HashMap<>();
        eventParameters.put(PrevacConstants.ZETES_URL, zetesUrl);
        eventParameters.put(PrevacConstants.ZETES_USERNAME, zetesUsername);
        eventParameters.put(PrevacConstants.ZETES_PASSWORD, zetesPassword);

        MotechEvent event = new MotechEvent(PrevacConstants.ZETES_UPDATE_EVENT, eventParameters);

        Period period = Period.days(1);

        RepeatingPeriodSchedulableJob job = new RepeatingPeriodSchedulableJob(event, startDate, null, period, true);
        motechSchedulerService.safeScheduleRepeatingPeriodJob(job);
    }

    public void unscheduleZetesUpdateJob() {
        motechSchedulerService.safeUnscheduleAllJobs(PrevacConstants.ZETES_UPDATE_EVENT);
    }
}
