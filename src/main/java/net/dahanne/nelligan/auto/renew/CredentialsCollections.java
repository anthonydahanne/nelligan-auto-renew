package net.dahanne.nelligan.auto.renew;

import io.smallrye.config.ConfigMapping;

import java.util.Set;

@ConfigMapping(prefix = "nelligan")
public interface CredentialsCollections {
    Set<Credentials> credentials();

    interface Credentials {
        String username();
        String password();
    }
}