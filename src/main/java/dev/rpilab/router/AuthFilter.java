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

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;

class AuthFilter implements GatewayFilter {
    private final String username;
    private final String password;
    private final String realm;

    AuthFilter(String username, String password, String realm) {
        this.username = username;
        this.password = password;
        this.realm = realm;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        final var req = exchange.getRequest();
        final var authHeader = req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            final var token = authHeader.substring("Basic ".length());
            final var credentials = new String(Base64.getDecoder().decode(token));
            final var parts = credentials.split(":");
            if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                return chain.filter(exchange);
            }
        }
        final var resp = exchange.getResponse();
        resp.setStatusCode(HttpStatus.UNAUTHORIZED);
        resp.getHeaders().add(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"" + realm + "\"");
        return resp.setComplete();
    }
}
