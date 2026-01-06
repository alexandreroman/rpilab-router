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

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

class RobotsTxtFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    @Override
    public ServerResponse filter(ServerRequest req, HandlerFunction<ServerResponse> next) throws Exception {
        if (req.path().equals("/robots.txt")) {
            final var res = new ClassPathResource("/static/robots.txt");
            return ServerResponse
                    .status(HttpStatus.OK)
                    .contentType(MediaType.TEXT_PLAIN)
                    .cacheControl(CacheControl.noStore())
                    .lastModified(Instant.now())
                    .body(res.getContentAsString(StandardCharsets.UTF_8));
        }
        return next.handle(req);
    }
}
