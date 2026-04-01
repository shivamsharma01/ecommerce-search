package com.mcart.search.config;

import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.spring.boot.autoconfigure.RestClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenSearch uses a pooled async HTTP client. After idle time, OpenSearch or kube-proxy may close
 * the TCP connection; the next request can then fail with "Connection closed by peer". Evicting
 * idle connections aggressively avoids handing out dead sockets.
 */
@Configuration
public class OpenSearchRestClientConfig {

	@Bean
	RestClientBuilderCustomizer searchOpenSearchHttpClientCustomizer() {
		return new RestClientBuilderCustomizer() {
			@Override
			public void customize(RestClientBuilder builder) {
				// HTTP tuning is applied via {@link #customize(HttpAsyncClientBuilder)}.
			}

			@Override
			public void customize(HttpAsyncClientBuilder builder) {
				builder.evictExpiredConnections();
				builder.evictIdleConnections(TimeValue.ofSeconds(30));
			}
		};
	}
}
