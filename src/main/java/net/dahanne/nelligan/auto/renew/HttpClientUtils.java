package net.dahanne.nelligan.auto.renew;

import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;

import javax.ws.rs.client.Client;

public class HttpClientUtils {

    /*
    Not exactly something ideal here...
    More sensible things were tried, such as:
    Client client = new ResteasyClientBuilderImpl().setFollowRedirects(true).build();
    But the behavior changed a lot...
    Since the calls are rare (once a day) we can live with the churn of Clients though...
     */
    public static Client createNewHttpClient() {
        ApacheHttpClient43Engine engine = new ApacheHttpClient43Engine();
        engine.setFollowRedirects(true);
        return new ResteasyClientBuilderImpl().httpEngine(engine).build();
    }
}
