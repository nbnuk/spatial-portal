/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package au.org.emii.portal.factory;

import au.org.emii.portal.settings.Settings;
import au.org.emii.portal.util.PortalProperties;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Support for validation, reading and writing of configuration file
 * <p/>
 * File backed implementation of PortalDocumentFactory
 *
 * @author geoff
 */
public class PortalDocumentFactoryFileImpl implements PortalDocumentFactory, InitializingBean {

    /**
     * Logger instance
     */
    private static final Logger LOGGER = Logger.getLogger(PortalDocumentFactoryFileImpl.class);

    private Settings settings = null;

    /**
     * Lookup the the name of the config file we should be reading from the
     * environement then validate and parse it returning a pointer to the root
     * element.
     * <p/>
     * If an error occurs here (null returned) then the system is FUBAR
     *
     * @return PortalDocument instance if reading succeeded, null if an error
     * was encountered
     */
    @Override
    public Properties createPortalDocumentInstance() {
        Properties portalDocument = new PortalProperties();

        // Have xmlbeans read the file and parse it
        InputStream is = null;
        try {
            is = new FileInputStream(getConfigFilename());

            portalDocument.load(is);

        } catch (FileNotFoundException e) {
            portalDocument = null;
            LOGGER.error("Could not load portal configuration file from: " + getConfigFilename());
        } catch (IOException e) {
            portalDocument = null;
            LOGGER.error("IOException reading configuration - should never happen, you may have big problems! - check this stack trace", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                LOGGER.error("Error closing " + getConfigFilename(), e);
            }
        }

        return portalDocument;
    }

    public Settings getSettings() {
        return settings;
    }

    @Required
    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    /**
     * Return full path and filename of config file
     *
     * @return
     */
    public String getConfigFilename() {
        return "/data/webportal/config/webportal-config.properties";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(settings);
    }

}
