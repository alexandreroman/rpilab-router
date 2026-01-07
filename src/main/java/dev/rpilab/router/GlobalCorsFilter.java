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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class GlobalCorsFilter extends OncePerRequestFilter {
    private final AppProps props;
    private final CorsProcessor processor = new DefaultCorsProcessor();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    GlobalCorsFilter(AppProps props) {
        this.props = props;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final var host = request.getHeader(HttpHeaders.HOST);
        CorsConfiguration config = null;

        if (host != null) {
            for (final var route : props.routes()) {
                if (pathMatcher.match(route.host(), host)) {
                    config = route.cors();
                    // We assume first matching route determines CORS config (or lack thereof)
                    // If matched route has no CORS, maybe we should continue matching?
                    // But standard Gateway logic routes to first match.
                    break;
                }
            }
        }

        if (config != null) {
            boolean isValid = processor.processRequest(config, request, response);
            if (!isValid || CorsUtils.isPreFlightRequest(request)) {
                // If the request is a CORS preflight request, we handle it directly here.
                // We ensure the response status is 200 OK and stop the filter chain
                // to prevent the request from being forwarded to the downstream service.
                if (isValid && CorsUtils.isPreFlightRequest(request)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                }
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
