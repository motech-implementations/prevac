package org.motechproject.prevac.osgi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.prevac.service.ConfigService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

/**
 * Verify that PrevacSettingsService is present, functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class PrevacConfigServiceIT extends BasePaxIT {

    @Inject
    private ConfigService configService;

    @Test
    public void testPrevacSettingsServicePresent() {
        assertNotNull(configService.getConfig());
    }
}
