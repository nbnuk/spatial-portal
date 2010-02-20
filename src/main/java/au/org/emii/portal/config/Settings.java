/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package au.org.emii.portal.config;

import au.org.emii.portal.BoundingBox;
import au.org.emii.portal.mest.MestConfiguration;
import java.util.Map;

/**
 *
 * @author geoff
 */
public interface Settings {

    public int getCacheMaxAge();

    public String getCacheParameter();

    public String getCacheUrl();

    public int getConfigRereadInitialInterval();

    public int getConfigRereadInterval();

    public BoundingBox getDefaultBoundingBox();

    public String getDefaultSelection();

    public Map<String, MestConfiguration> getMestConfigurations();

    public int getNetConnectSlowTimeout();

    public int getNetConnectTimeout();

    public int getNetReadSlowTimeout();

    public int getNetReadTimeout();

    public String getProxyAllowedHosts();

    public String getProxyHost();

    public String getProxyNonProxyHosts();

    public String getProxyPassword();

    public int getProxyPort();

    public String getProxyUsername();

    public String getXmlMimeType();

    public boolean isDisableDepthServlet();

    public boolean isProxyRequired();

    public void setCacheMaxAge(int cacheMaxAge);

    public void setCacheParameter(String cacheParameter);

    public void setCacheUrl(String cacheUrl);

    public void setConfigRereadInitialInterval(int configRereadInitialInterval);

    public void setConfigRereadInterval(int configRereadInterval);

    public void setDefaultBoundingBox(BoundingBox defaultBoundingBox);

    public void setDefaultSelection(String defaultSelection);

    public void setDisableDepthServlet(boolean disableDepthServlet);

    public void setMestConfigurations(Map<String, MestConfiguration> mestConfigurations);

    public void setNetConnectSlowTimeout(int netConnectSlowTimeout);

    public void setNetConnectTimeout(int netConnectTimeout);

    public void setNetReadSlowTimeout(int netReadSlowTimeout);

    public void setNetReadTimeout(int netReadTimeout);

    public void setProxyAllowedHosts(String proxyAllowedHosts);

    public void setProxyHost(String proxyHost);

    public void setProxyNonProxyHosts(String proxyNonProxyHosts);

    public void setProxyPassword(String proxyPassword);

    public void setProxyPort(int proxyPort);

    public void setProxyRequired(boolean proxyRequired);

    public void setProxyUsername(String proxyUsername);

    public void setXmlMimeType(String xmlMimeType);

    public String getIsoCountriesFilename();

    public void setIsoCountriesFilename(String isoCountriesFilename);

    public String getPortalName();

    public void setPortalName(String portalName);

    public boolean isPortalUsersDisabled();

    public boolean isAdminConsoleDisabled();

    public void setPortalUsersDisabled(boolean disabled);

    public void setAdminConsoleDisabled(boolean disabled);

}
