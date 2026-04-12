package com.mcart.search.config;

import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.spring.boot.autoconfigure.RestClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
