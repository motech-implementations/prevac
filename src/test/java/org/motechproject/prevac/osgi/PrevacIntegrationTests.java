package org.motechproject.prevac.osgi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Prevac bundle integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        PrevacWebIT.class,
        PrevacLifecycleListenerIT.class,
        PrevacConfigServiceIT.class,
        LookupServiceIT.class
})
public class PrevacIntegrationTests {
}
