package net.dahanne.nelligan.auto.renew;

import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ApplicationScoped
public class NelliganClient {

    private static final Logger LOG = Logger.getLogger(NelliganClient.class);
    public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36";

// Not needed for now - but could be necessary in the future
//    private final String NELLIGAN_DECOUVERTE_BASE_URL = "https://nelligandecouverte.ville.montreal.qc.ca";
    private final String NELLIGAN_BASE_URL = "https://nelligan.ville.montreal.qc.ca";


    public PatronInfo authenticateAndPatronInfo(Client client, String username, String password) {
        WebTarget target = client.target(NELLIGAN_BASE_URL + "/patroninfo");
        Response response = target
                .request()
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .post(Entity.entity("code=" + username + "&pin=" + password, MediaType.APPLICATION_FORM_URLENCODED));

        String location = NELLIGAN_BASE_URL + response.getHeaderString("Location");
        target = client.target(location);
        response = target
                .request()
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .get();

        String patronInfoAsString = response.readEntity(String.class);
        return ParseUtils.parsePatronInfoPage(patronInfoAsString, location);
    }

    public Item renew(Client client, String location, Item item) {
        WebTarget target = client.target(location);
        Response response = target
                .request()
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .post(Entity.entity("value=" + item.rValue(), MediaType.APPLICATION_FORM_URLENCODED));
        String renewPageAsString = response.readEntity(String.class);
        PatronInfo patronInfo = ParseUtils.parsePatronInfoPage(renewPageAsString, location);
        Item renewedItem = patronInfo
                .items()
                .stream()
                .filter(itemToFilter -> itemToFilter.rValue().equals(item.rValue())).findFirst().orElseThrow();
        if(!renewedItem.error().isBlank()) {
            throw new UnRenewableItemException(renewedItem.error());
        }
        return renewedItem;
    }


}
