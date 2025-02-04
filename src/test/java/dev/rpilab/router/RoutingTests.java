/*
 * Copyright (c) 2025 Broadcom, Inc. or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.rpilab.router;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
class RoutingTests {
    @Autowired
    private WebTestClient client;

    @Test
    void testRoutes() {
        stubFor(get("/hello").willReturn(ok("Hello")));
        client.get().uri("/hello").header(HttpHeaders.HOST, "hello.corp.com").exchange()
                .expectStatus().isOk();

        stubFor(get("/hola").willReturn(ok("Hola")));
        client.get().uri("/hola").header(HttpHeaders.HOST, "hola.corp.com").exchange()
                .expectStatus().isOk();
    }

    @Test
    void testSecuredRoutes() {
        stubFor(get("/secure").willReturn(ok("Secure")));
        client.get().uri("/secure").header(HttpHeaders.HOST, "secure.corp.com").exchange()
                .expectStatus().isUnauthorized();
        client.get().uri("/notfound").header(HttpHeaders.HOST, "secure.corp.com").exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testUnknownRoute() {
        client.get().uri("/notfound").header(HttpHeaders.HOST, "foo.corp.com").exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testHealthProbes() {
        client.get().uri("/livez").exchange().expectStatus().isOk();
        client.get().uri("/readyz").exchange().expectStatus().isOk();
    }
}
