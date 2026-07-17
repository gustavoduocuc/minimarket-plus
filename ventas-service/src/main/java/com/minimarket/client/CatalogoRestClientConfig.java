package com.minimarket.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class CatalogoRestClientConfig {

    @Bean
    public RestClient catalogoRestClient(
            @Value("${catalogo.base-url}") String catalogoBaseUrl,
            @Value("${catalogo.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${catalogo.read-timeout-ms:3000}") int readTimeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return RestClient.builder()
                .baseUrl(catalogoBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
