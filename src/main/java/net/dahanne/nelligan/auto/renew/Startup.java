package net.dahanne.nelligan.auto.renew;

import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

@io.quarkus.runtime.Startup
@ApplicationScoped
public class Startup {

    private static final Logger LOG = Logger.getLogger(Startup.class);

    Startup(CredentialsCollections credentialsCollections) {
        credentialsCollections
                .credentials()
                .forEach(credentials ->
                        LOG.debugf("Starting with username %s, password seems to be set, it's %d characters long.",
                                credentials.username(),
                                credentials.password().length()
                        )
                );
    }

}