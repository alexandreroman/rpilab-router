/*
 * Copyright (c) 2026 Alexandre Roman
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.SocketTimeoutException;

class TimeoutFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    private final Logger logger = LoggerFactory.getLogger(TimeoutFilter.class);

    @Override
    public ServerResponse filter(ServerRequest req, HandlerFunction<ServerResponse> next) throws Exception {
        try {
            return next.handle(req);
        } catch (ResourceAccessException e) {
            if (e.getMostSpecificCause() instanceof SocketTimeoutException) {
                logger.warn("Socket timed out", e);
                return ServerResponse.status(HttpStatus.GATEWAY_TIMEOUT).body("Connection timed out");
            }
            throw e;
        }
    }
}
