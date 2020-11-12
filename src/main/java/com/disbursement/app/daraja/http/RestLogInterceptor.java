package com.disbursement.app.daraja.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;

public class RestLogInterceptor implements ClientHttpRequestInterceptor {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = clientHttpRequestExecution.execute(request, body);
        logResponse(response);
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) throws IOException {
//        if (log.isinfoEnabled()) {
            log.info("\n\n");
            log.info("===========================Request begin================================================");
            log.info("URI         : {}", request.getURI());
            log.info("Method      : {}", request.getMethod());
            log.info("Headers     : {}", request.getHeaders());
            log.info("Request body: \n{}", new String(body, "UTF-8"));
            log.info("==========================Request end================================================");
//        }
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
//        if (log.isinfoEnabled()) {
            log.info("============================Response begin==========================================");
            log.info("Status code  : {}", response.getStatusCode());
            log.info("Status text  : {}", response.getStatusText());
            log.info("Headers      : {}", response.getHeaders());
            log.info("Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
            log.info("============================Response end=============================================");
//        }
    }
}
