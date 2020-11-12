package com.disbursement.app.daraja;

import com.disbursement.app.daraja.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class MpesaHttpService {

    @Autowired private ObjectMapper objectMapper;
    @Autowired private RestTemplate restTemplate;
    @Autowired private AuthService authService;

    @Value("${daraja.b2c.requestUrl}")
    private String requestUrl;

    /**
     * Package Safaricom API request headers
     *
     * @return HttpHeaders
     * @throws Exception
     */
    private HttpHeaders createHeaders() throws Exception {
        return new HttpHeaders() {
            {
                //Authorization
                String token = authService.fetchToken();
                String authHeader = String.format("Bearer %s", token);
                set("Authorization", authHeader);

                set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                set("Accept", "*/*");
            }
        };
    }

    /**
     * Send POST outbound request
     *
     * @param payload
     * @return ResponseEntity<String>
     * @throws Exception
     */
    public ResponseEntity<String> sendOutboundRequest(Object payload) throws Exception {
        //Generate template URI
        UriComponents uriComponents = UriComponentsBuilder
                .fromHttpUrl( requestUrl )
                .build()
                .encode();

        //Package the headers and body parameters
        HttpEntity<?> httpEntity = new HttpEntity<>(
                payload,
                createHeaders()
        );

        //Initiate a transaction request
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                uriComponents.toString(),
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        return responseEntity;
    }

    /**
     * Respond to client callback
     *
     * @param url
     * @param payload
     * @return ResponseEntity<String>
     */
    public ResponseEntity<String> respondToClientCallback(String url, String payload) {
        UriComponents uriComponents = UriComponentsBuilder
                .fromHttpUrl(url)
                .build()
                .encode();

        HttpHeaders httpHeaders = new HttpHeaders() {{
            set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            set("Accept", "*/*");
        }};

        HttpEntity<?> httpEntity = new HttpEntity<>(payload, httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                uriComponents.toString(),
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        return responseEntity;
    }

}
