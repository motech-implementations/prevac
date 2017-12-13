package org.motechproject.prevac.service;

import org.motechproject.prevac.domain.Config;

public interface ConfigService {

    Config getConfig();

    void updateConfig(Config config);
}
