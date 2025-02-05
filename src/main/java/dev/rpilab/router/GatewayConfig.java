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

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.host;

@Configuration(proxyBeanMethods = false)
class GatewayConfig {
    @Bean
    AuthFilter authFilter(AppProps props) {
        return new AuthFilter(props);
    }

    @Bean
    NotFoundFilter notFoundFilter() {
        return new NotFoundFilter();
    }

    @Bean
    RobotsTxtFilter robotsTxtFilter() {
        return new RobotsTxtFilter();
    }

    @Bean
    TimeoutFilter timeoutFilter() {
        return new TimeoutFilter();
    }

    @Bean
    BeanFactoryPostProcessor routes(
            AuthFilter authFilter,
            NotFoundFilter notFoundFilter,
            RobotsTxtFilter robotsTxtFilter,
            TimeoutFilter timeoutFilter,
            AppProps props) {
        return beanFactory -> {
            // Setup downstream routes
            int i = 0;
            for (final var rd : props.routes()) {
                final var routeId = "route-" + (i++);
                final var r = route(routeId)
                        .route(host(rd.host()), http(rd.uri()))
                        .filter(robotsTxtFilter)
                        .filter(notFoundFilter)
                        .filter(timeoutFilter);
                if (rd.secured()) {
                    r.filter(authFilter);
                }
                beanFactory.registerSingleton(routeId, r.build());
            }
        };
    }
}
