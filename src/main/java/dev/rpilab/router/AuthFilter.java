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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Base64;

class AuthFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    private final AppProps props;

    AuthFilter(AppProps props) {
        this.props = props;
    }

    private boolean isUserValid(String username, String password) {
        for (final Credentials creds : props.credentials()) {
            if (username.equals(creds.username()) && password.equals(creds.password())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ServerResponse filter(ServerRequest req, HandlerFunction<ServerResponse> next) throws Exception {
        final var authHeader = req.headers().firstHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            final var token = authHeader.substring("Basic ".length());
            final var credentials = new String(Base64.getDecoder().decode(token));
            final var parts = credentials.split(":");
            if (parts.length == 2 && isUserValid(parts[0], parts[1])) {
                return next.handle(req);
            }
        }
        return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"rpilab\"")
                .body("Unauthorized");
    }
}
