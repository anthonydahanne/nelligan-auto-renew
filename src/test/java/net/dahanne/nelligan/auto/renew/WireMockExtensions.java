package net.dahanne.nelligan.auto.renew;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WireMockExtensions implements QuarkusTestResourceLifecycleManager {

    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();

        String patronInfoRenewed3Times = readFile("/renewed3Times.html");

        wireMockServer.stubFor(post(urlEqualTo("/patroninfo"))
                .willReturn(aResponse()
                        .withHeader("Location", "/patroninfo~S58/2548616/items")));

        wireMockServer.stubFor(get(urlEqualTo("/patroninfo~S58/2548616/items"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/html")
                        .withBody(patronInfoRenewed3Times)));

        ContentPattern<String> stringContentPattern = new ContainsPattern("value=i9999999");
        wireMockServer.stubFor(post(urlEqualTo("/patroninfo~S58/2548616/items")).withRequestBody(stringContentPattern)
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/html")
                        .withBody(patronInfoRenewed3Times)));


        return Collections.singletonMap("quarkus.rest-client.url", wireMockServer.baseUrl());
    }

    private String readFile(String pathName) {
        Path path;
        try {
            path = Paths.get(getClass().getClassLoader()
                    .getResource(pathName).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String data;
        try (Stream<String> lines = Files.lines(path)) {
            data = lines.collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public void stop() {
        if (null != wireMockServer) {
            wireMockServer.stop();
        }
    }
}