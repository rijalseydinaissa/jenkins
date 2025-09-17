package com.example.Jenkins;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest

class JenkinsApplicationTests {

    @LocalServerPort  /*Récupère le port utilisé*/
    private int port;

    @Test
    void testHomePage() throws Exception {
        String url = "http://localhost:" + port + "/";
        TestRestTemplate restTemplate = null;
        String response = restTemplate.getForObject(url, String.class);
        assertThat(response).contains("Hello Jenkins Demo!");  /*Vérifie la réponse*/
    }

}
