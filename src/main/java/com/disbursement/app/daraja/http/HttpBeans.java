package com.disbursement.app.daraja.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class HttpBeans {

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Bean
    public RestTemplate restTemplate(){
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(
                new SimpleClientHttpRequestFactory()
        );

        RestTemplate t = restTemplateBuilder
                .errorHandler( new RestResponseErrorHandler() )
                .build();
        t.setRequestFactory( factory );
        t.setInterceptors(Collections.singletonList(new RestLogInterceptor()));
        return  t;
    }
}
