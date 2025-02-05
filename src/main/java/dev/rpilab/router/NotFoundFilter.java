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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
class NotFoundFilter implements GatewayFilter {
    @Value("classpath:/static/error/404.html")
    private Resource notFoundResource;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.defer(() -> {
            if (HttpStatus.NOT_FOUND.equals(exchange.getResponse().getStatusCode())) {
                return serveNotFoundPage(exchange);
            }
            return Mono.empty();
        }));
    }

    private Mono<Void> serveNotFoundPage(ServerWebExchange exchange) {
        final var resp = exchange.getResponse();
        resp.setStatusCode(HttpStatus.NOT_FOUND);
        resp.getHeaders().setContentType(MediaType.TEXT_HTML);

        final var buf = DataBufferUtils.read(notFoundResource, resp.bufferFactory(), 4096);
        return resp.writeWith(buf);
    }
}
