package com.disbursement.app.daraja.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Component
public class HttpService {

    @Autowired private ObjectMapper objectMapper;
    @Autowired private RestTemplate restTemplate;

    private HttpHeaders createHeaders() {
        return new HttpHeaders() {
            {
                set("Content-Type", MediaType.APPLICATION_JSON.toString());
                set("Accept", "application/json");
            }
        };
    }

    /**
     * Send a POST request
     *
     * @param url
     * @param request
     * @return ResponseEntity<String>
     * @throws Exception
     */
    public ResponseEntity<String> postRequest(String url, Object request ) throws Exception {
        /*Pack the URL for this request*/
        UriComponents uriComponents = fromHttpUrl( url ).build().encode();
        HttpHeaders httpHeaders = createHeaders();
        String jsonRequest = objectMapper.writeValueAsString(request);

        HttpEntity<?> httpEntity = new HttpEntity<>( jsonRequest, httpHeaders );
        return restTemplate.exchange(
                uriComponents.toString(),
                HttpMethod.POST,
                httpEntity,
                String.class
        );
    }
}
