package net.dahanne.nelligan.auto.renew;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


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
        credentials = credentialsCollections.credentials().stream().toList().getFirst();
    }

    @Test
    void authenticateAndRenewTest() {
        credentialsCollections.credentials().forEach(credentialsInstance -> {
            PatronInfo patronInfo = nelliganClient.authenticateAndRenew(credentialsInstance.username(), credentialsInstance.password(), 2);
            System.out.println(patronInfo.name());
            patronInfo.items().forEach(System.out::println);
        });
    }

//    @Test
//    void renewTest() {
//        Client client = createNewHttpClient();
//        PatronInfo patronInfo = nelliganClient.authenticateAndPatronInfo(client, credentials.username(), credentials.password());
//        Item item = patronInfo.items().getFirst();
//        Item renewedItem = nelliganClient.renew(patronInfo.location(), item);
//        System.out.println(renewedItem);
//    }

}
