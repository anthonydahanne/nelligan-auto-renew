package net.dahanne.nelligan.auto.renew;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.dahanne.nelligan.auto.renew.HttpClientUtils.createNewHttpClient;

@ApplicationScoped
public class ScheduledOperation {

    private static final Logger LOG = Logger.getLogger(ScheduledOperation.class);

    @ConfigProperty(name = "renew.trigger.days")
    Integer daysUntilRenewing;

    @ConfigProperty(name = "renew.email.destination")
    String emailDestination;

    @Inject
    Mailer mailer;

    private final CredentialsCollections credentialsCollections;
    private final NelliganClient nelliganClient;

    public ScheduledOperation(CredentialsCollections credentialsCollections, NelliganClient nelliganClient) {
        this.credentialsCollections = credentialsCollections;
        this.nelliganClient = nelliganClient;
    }

    @Scheduled(every = "{renew.frequency}", delayed = "${renew.delay:0m}")
    public void runSchedule() {
        LocalDate now = LocalDate.now();
        StringBuilder emailBuilder = new StringBuilder();
        AtomicBoolean issuesRenewing = new AtomicBoolean(false);
        credentialsCollections.credentials().forEach(credentialsInstance -> {
            Client client = createNewHttpClient();
            PatronInfo patronInfo = nelliganClient.authenticateAndPatronInfo(client, credentialsInstance.username(), credentialsInstance.password());
            LOG.infof("Working with patron named %s; with %s documents on file, will renew documents due before %d days", patronInfo.name(), patronInfo.items().size(), daysUntilRenewing);
            patronInfo.items().forEach(item -> {
                if (now.until(item.dueDate()).getDays() < daysUntilRenewing) {
                    try {
                        Item renewedItem = nelliganClient.renew(client, patronInfo.location(), item);
                        String formattedRenewMessage = String.format("Successfully renewed item: %s, due date is now %s, it has been renewed %d times", renewedItem.title(), renewedItem.dueDate(), renewedItem.renewed());
                        LOG.info(formattedRenewMessage);
                        emailBuilder.append(formattedRenewMessage).append("\n");
                    } catch (UnRenewableItemException e) {
                        issuesRenewing.set(true);
                        String formattedNotRenewedMessage = String.format("Impossible to renew the item %s, bound to patron named %s, due date is still %s, it has been renewed %d times, error is : %s", item.title(), patronInfo.name(), item.dueDate(), item.renewed(), e.getMessage());
                        LOG.warn(formattedNotRenewedMessage);
                        emailBuilder.append(formattedNotRenewedMessage).append("\n");
                    }
                }
            });
            client.close();
        });
        if (!emailBuilder.toString().isBlank()) {
            mailer.send(
                    Mail.withText(emailDestination,
                            "Nelligan Auto Renew message - " + (issuesRenewing.get() ? "problem(s) encountered renewing" : "all good"),
                            emailBuilder.toString()
                    )
            );
        }
    }
}
