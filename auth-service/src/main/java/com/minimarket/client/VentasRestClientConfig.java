package com.minimarket.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class VentasRestClientConfig {

    @Bean
    public RestClient ventasRestClient(
            @Value("${ventas.base-url:http://localhost:8080}") String ventasBaseUrl,
            @Value("${ventas.connect-timeout-ms:1000}") int connectTimeoutMs,
            @Value("${ventas.read-timeout-ms:1500}") int readTimeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return RestClient.builder()
                .baseUrl(ventasBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
