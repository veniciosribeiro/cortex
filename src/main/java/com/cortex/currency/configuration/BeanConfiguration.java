package com.cortex.currency.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfiguration {

    @Value("${api.bc.conexao.timeout}")
    private Integer apiBcTimeout;

    @Bean
    public RestTemplate restTemplate() {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(apiBcTimeout * 1000);
        factory.setReadTimeout(apiBcTimeout * 1000);

        return new RestTemplate(factory);
    }

}
