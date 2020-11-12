package com.disbursement.app.daraja.services;


import com.disbursement.app.daraja.entities.AuthData;
import com.disbursement.app.daraja.repository.AuthDataRepository;
import com.disbursement.app.daraja.vm.OauthTokenResponse;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class AuthService {

    private final Logger LOG = LoggerFactory.getLogger( getClass() );

    @Autowired
    private AuthDataRepository authDataRepository;

    //Token expires after 1 hour
    private final long TOKEN_INVALIDITY_WINDOW = 240;

    @Value("${daraja.access_token_url}")
    private String accessTokenUrl;

    @Value("${daraja.b2c.consumer_key}")
    private String b2cConsumerKey;

    @Value("${daraja.b2c.consumer_secret}")
    private String b2cConsumerSecret;

    @Autowired
    private RestTemplate restTemplate;

    public String fetchToken() throws Exception {
        String accessToken = "";
        List<AuthData> oAuthorizationData = (List<AuthData>)authDataRepository.findAll();
        if ( oAuthorizationData.size() > 0 ) {
            AuthData authorizationData = oAuthorizationData.get( 0 );

            Long duration = new Date().getTime() - authorizationData.getLastUpdated().getTime();
            long diffInMinutes = (TimeUnit.MILLISECONDS.toSeconds(duration) ) + TOKEN_INVALIDITY_WINDOW;
            boolean isValid =  diffInMinutes < authorizationData.getSafaricomTimeout();

            if( isValid ) {
                accessToken = authorizationData.getOauthAccessToken();
            }
        }

        //When to fetch a new token
        if( StringUtils.isEmpty( accessToken ) ){
            accessToken = resetToken();
        }

        return accessToken;
    }

//    @Override
    private String resetToken() throws Exception{
        String accessToken = "";
        String appKeySecret = b2cConsumerKey + ":" + b2cConsumerSecret;

        byte[] bytes = appKeySecret.getBytes("ISO-8859-1");
        String auth = new String( Base64.encodeBase64( bytes ));

        UriComponents uriComponents = UriComponentsBuilder
                .fromHttpUrl( accessTokenUrl )
                .build()
                .encode();

        HttpHeaders httpHeaders = new HttpHeaders(){{
            set("authorization", "Basic " + auth);
            set("cache-control", "no-cache");
        }};

        HttpEntity<?> httpEntity = new HttpEntity<>( null, httpHeaders );
        ResponseEntity<OauthTokenResponse> responseEntity = restTemplate.exchange(
                uriComponents.toString(),
                HttpMethod.GET,
                httpEntity,
                OauthTokenResponse.class
        );

        //Sample - {"access_token":"SGWcJPtNtYNPGm6uSYR9yPYrAI3Bm","expires_in":"3599"}
        OauthTokenResponse authResponse = responseEntity.getBody();
        LOG.info("OAUTH ACCESS RESPONSE:  " + authResponse);

        accessToken = authResponse.getAccessToken();
        long expiresIn = authResponse.getExpiresIn();

        List<AuthData> oAuthorizationData = (List<AuthData>)authDataRepository.findAll();
        if ( oAuthorizationData.size() > 0 ) {
            AuthData entity = new AuthData();
            entity.setOauthAccessToken(accessToken);
            entity.setSafaricomTimeout(expiresIn);
            entity.setLastUpdated(new Date());
            authDataRepository.save(entity);
        } else {
            AuthData entity = new AuthData();
            entity.setOauthAccessToken(accessToken);
            entity.setSafaricomTimeout(expiresIn);
            entity.setLastUpdated(new Date());
            authDataRepository.save(entity);
        }

        return accessToken;
    }

}
