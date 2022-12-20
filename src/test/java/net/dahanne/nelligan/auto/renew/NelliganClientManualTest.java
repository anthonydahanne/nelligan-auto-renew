package net.dahanne.nelligan.auto.renew;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Client;

import static net.dahanne.nelligan.auto.renew.HttpClientUtils.createNewHttpClient;

@QuarkusTest
@Disabled
public class NelliganClientManualTest {

    CredentialsCollections.Credentials credentials;
    CredentialsCollections credentialsCollections;
    NelliganClient nelliganClient;

    public NelliganClientManualTest(CredentialsCollections credentialsCollections, NelliganClient nelliganClient) {
        this.credentialsCollections = credentialsCollections;
        this.nelliganClient = nelliganClient;
    }

    @BeforeEach
    void before() {
        credentials = credentialsCollections.credentials().stream().toList().get(0);
    }

    @Test
    void authenticateAndPatronInfoTest() {
        credentialsCollections.credentials().forEach(credentialsInstance -> {
            Client client = createNewHttpClient();
            PatronInfo patronInfo = nelliganClient.authenticateAndPatronInfo(client, credentialsInstance.username(), credentialsInstance.password());
            System.out.println(patronInfo.name());
            patronInfo.items().forEach(System.out::println);
        });
    }

    @Test
    void renewTest() {
        Client client = createNewHttpClient();
        PatronInfo patronInfo = nelliganClient.authenticateAndPatronInfo(client, credentials.username(), credentials.password());
        Item item = patronInfo.items().get(0);
        Item renewedItem = nelliganClient.renew(client, patronInfo.location(), item);
        System.out.println(renewedItem);
    }

}
