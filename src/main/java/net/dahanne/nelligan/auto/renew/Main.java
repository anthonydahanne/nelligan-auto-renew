package net.dahanne.nelligan.auto.renew;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Startup
@ApplicationScoped
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class);

    Integer daysUntilRenewing;
    NelliganClient nelliganClient;
    CredentialsCollections credentialsCollections;

    Main(CredentialsCollections credentialsCollections, NelliganClient nelliganClient, @ConfigProperty(name = "renew.trigger.days") Integer daysUntilRenewing) {
        this.daysUntilRenewing = daysUntilRenewing;
        this.nelliganClient = nelliganClient;
        this.credentialsCollections = credentialsCollections;
        credentialsCollections
                .credentials()
                .forEach(credentials ->
                        LOG.debugf("Starting with username ending with %s, password seems to be set, it's %d characters long.",
                                credentials.username().substring(credentials.username().length() - 4),
                                credentials.password().length()
                        )
                );
        LOG.debugf("Configured with renew documents due before %d days", daysUntilRenewing);

    }

    @PostConstruct
    void onApplicationStart() {
        AtomicBoolean errors = new AtomicBoolean(false);

        credentialsCollections.credentials().forEach(credentialsInstance -> {
            PatronInfo patronInfo = nelliganClient.authenticateAndRenew(credentialsInstance.username(), credentialsInstance.password(), daysUntilRenewing);
            LOG.infof("Successfully connected with patron named %s; with %s documents on file", patronInfo.name(), patronInfo.items().size());
            if (patronInfo.items().stream().anyMatch(item -> item.error() != null)) {
                errors.set(true);
                var errorMessage = patronInfo.items().stream()
                        .filter(item -> item.error() != null)
                        .map(item -> String.format("%s: %s", item.title(), item.error()))
                        .collect(Collectors.joining("\n"));
                LOG.warnf("⚠️One or more items could not be renewed, Here are the problems:\n%s", errorMessage);
            }
        });
        if (errors.get()) {
            Quarkus.asyncExit(1);
        }
        Quarkus.asyncExit(0);
    }


}