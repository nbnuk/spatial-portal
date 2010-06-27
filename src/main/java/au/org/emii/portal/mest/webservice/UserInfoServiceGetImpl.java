/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package au.org.emii.portal.mest.webservice;

import au.org.emii.portal.service.UserInfoService;
import au.org.emii.portal.mest.webservice.MestWebService;
import au.org.emii.portal.session.PortalUser;
import au.org.emii.portal.session.PortalUserImpl;
import au.org.emii.portal.webservice.WebServiceSession;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import javax.ws.rs.core.MultivaluedMap;
import org.springframework.beans.factory.annotation.Required;
import org.w3c.dom.Node;

/**
 *
 * Implementation of the UserInfo service - works against xml.user.get
 * @author geoff
 */
public class UserInfoServiceGetImpl extends MestWebService implements UserInfoService {


    /**
     * apache commons http client instance - used because it processes cookies
     * required to continue session on server
     */
    private WebServiceSession webServiceSession = null;

    /**
     * Check the profile described in the xml info for this user to see if
     * they are an administrator or just a regular user.
     *
     * @param profile value of the xml profile field
     * @return integer constant from PortalUser indicating whether the user
     * is an administrator or not.
     */
    private int profileType(String profile) {
        return (profile.equals(parameters.getAdministratorProfile()))
                ? PortalUser.USER_ADMIN : PortalUser.USER_REGULAR;
    }

    @Override
    public PortalUser userInfo(String username) {
        // assumes already logged into mest...

        String uri = parameters.userInfoServiceUri();
        // setup the web service parameters (username)
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("username", username);

        PortalUser portalUser = null;

        // now get the info for this user
        xmlWebService.makeRequest(
                webServiceSession.getClient(),
                uri,
                queryParams);

        if (xmlWebService.isResponseXml()) {
            // looks like the lookup succeeded - process reply
            portalUser = processXmlResponse(username);
        } else {
            // non-xml reply - did you login first?
            // login again and retry once
            xmlWebService.close();
            webServiceSession.login();
            xmlWebService.makeRequest(
                    webServiceSession.getClient(),
                    uri,
                    queryParams);

            if (xmlWebService.isResponseXml()) {
                portalUser = processXmlResponse(username);
            } else {
                // still broken - give up
                logger.error(
                    "non-xml reply received from '" + uri + "': '"
                    + xmlWebService.getRawResponse() + "' - did you remember "
                    + "to login?");
            }
        }

        return portalUser;

    }

    private PortalUser processXmlResponse(String username) {
        PortalUser portalUser = null;
            // get the each record instance for this user
            Node user = xmlWebService.parseNode("//response/record");

            // if user doesn't exist, the xml document will just be an empty response element
            if (user == null) {
                logger.debug("unable to find requested user: '" + username + "'");
            } else {
                portalUser = new PortalUserImpl();
                portalUser.setUsername(username);
                portalUser.setLastName(xmlWebService.parseString(user, "surname/text()"));
                portalUser.setFirstName(xmlWebService.parseString(user, "name/text()"));
                portalUser.setAddress(xmlWebService.parseString(user, "address/text()"));
                portalUser.setState(xmlWebService.parseString(user, "state/text()"));
                portalUser.setCountry(xmlWebService.parseString(user, "country/text()"));
                portalUser.setZip(xmlWebService.parseString(user, "zip/text()"));
                portalUser.setEmail(xmlWebService.parseString(user, "email/text()"));
                portalUser.setOrganisation(xmlWebService.parseString(user, "organisation/text()"));
                portalUser.setType(profileType(xmlWebService.parseString(user, "profile/text()")));
            }
            xmlWebService.close();
            return portalUser;
    }


    public WebServiceSession getWebServiceSession() {
        return webServiceSession;
    }

    @Required
    public void setWebServiceSession(WebServiceSession webServiceSession) {
        this.webServiceSession = webServiceSession;
    }

    
}