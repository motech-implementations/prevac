package org.motechproject.prevac.scheduler;


import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.event.MotechEvent;
import org.motechproject.prevac.constants.PrevacConstants;
import org.motechproject.scheduler.contract.RepeatingPeriodSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class PrevacSchedulerTest {

    @Mock
    private MotechSchedulerService motechSchedulerService;

    private PrevacScheduler prevacScheduler;

    @Before
    public void setUp() {
        initMocks(this);
        prevacScheduler = new PrevacScheduler(motechSchedulerService);
    }

    @After
    public void cleanup() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void shouldScheduleZetesUpdateJob() {
        Date startDate = LocalDate.now().toDate();
        String zetesUrl = "zetesUrl";
        String zetesUsername = "username";
        String zetesPassword = "password";

        prevacScheduler.scheduleZetesUpdateJob(startDate, zetesUrl, zetesUsername, zetesPassword);

        Map<String, Object> eventParameters = new HashMap<>();
        eventParameters.put(PrevacConstants.ZETES_URL, zetesUrl);
        eventParameters.put(PrevacConstants.ZETES_USERNAME, zetesUsername);
        eventParameters.put(PrevacConstants.ZETES_PASSWORD, zetesPassword);
        MotechEvent event = new MotechEvent(PrevacConstants.ZETES_UPDATE_EVENT, eventParameters);
        Period period = Period.days(1);
        RepeatingPeriodSchedulableJob job = new RepeatingPeriodSchedulableJob(event, startDate, null, period, true);

        verify(motechSchedulerService).safeScheduleRepeatingPeriodJob(job);
    }

    @Test
    public void shouldUnscheduleZetesUpdateJob() {
        prevacScheduler.unscheduleZetesUpdateJob();
        verify(motechSchedulerService).safeUnscheduleAllJobs(PrevacConstants.ZETES_UPDATE_EVENT);
    }
}
