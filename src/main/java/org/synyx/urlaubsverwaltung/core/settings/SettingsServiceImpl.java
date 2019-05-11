package org.synyx.urlaubsverwaltung.core.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Implementation for {@link org.synyx.urlaubsverwaltung.core.settings.SettingsService}.
 */
@Service
public class SettingsServiceImpl implements SettingsService {

    private static final Logger LOG = LoggerFactory.getLogger(SettingsServiceImpl.class);

    private final SettingsDAO settingsDAO;

    @Autowired
    public SettingsServiceImpl(SettingsDAO settingsDAO) {

        this.settingsDAO = settingsDAO;
    }

    @Override
    public void save(Settings settings) {

        settingsDAO.save(settings);

        LOG.info("Updated settings: {}", settings);
    }


    @Override
    public Settings getSettings() {

        // TODO: Maybe fixed in future for different settings (based on date,...)
        return settingsDAO.findById(1)
            .orElseThrow(() -> new IllegalStateException("No settings in database found."));
    }
}
