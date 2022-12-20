package net.dahanne.nelligan.auto.renew;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Client;

import static net.dahanne.nelligan.auto.renew.HttpClientUtils.createNewHttpClient;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
@QuarkusTestResource(WireMockExtensions.class)
public class NelliganClientTest {

    private final String baseUrl;
    NelliganClient nelliganClient;

    CredentialsCollections.Credentials credentials;
    CredentialsCollections credentialsCollections;


    public NelliganClientTest(CredentialsCollections credentialsCollections,
                              NelliganClient nelliganClient,
                              @ConfigProperty(name = "quarkus.rest-client.url") String baseUrl) {
        this.credentialsCollections = credentialsCollections;
        this.nelliganClient = nelliganClient;
        this.baseUrl = baseUrl;
    }

    @BeforeEach
    void before() {
        nelliganClient.setBaseUrl(baseUrl);
        credentials = credentialsCollections.credentials().stream().toList().get(0);
    }

    @Test
    void cantRenewWhen3RenewedTimesTest() {
        Client client = createNewHttpClient();
        PatronInfo patronInfo = nelliganClient.authenticateAndPatronInfo(client, credentials.username(), credentials.password());
        // this item has been renewed 3 times already
        Item item = patronInfo.items().get(0);
        try {
            nelliganClient.renew(client, patronInfo.location(), item);
            fail("Did not throw expected exception");
        } catch (UnRenewableItemException e) {
            assertThat(e.getMessage(), equalTo("TOO MANY RENEWALS Renewed 3 times"));
        }
    }
}
