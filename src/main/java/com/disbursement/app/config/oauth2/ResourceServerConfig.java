//package com.disbursement.app.config.oauth2;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
//import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
//import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
//import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
//
//@Configuration
//@EnableResourceServer
//public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
//
//    @Autowired
//    private DefaultTokenServices tokenServices;
//
//    private static final String[] WHITELIST_RESOURCES = {
//            // -- swagger ui
//            "/swagger-resources/**",
//            "/swagger-ui.html",
//            "/configuration/**",
//            "/v2/api-docs",
//            "/webjars/**",
//    };
//
//    @Override
//    public void configure(final HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests()
//                    .antMatchers( WHITELIST_RESOURCES ).permitAll()
//                    .anyRequest().authenticated()
//                .and()
//                    .sessionManagement()
//                    .sessionCreationPolicy( SessionCreationPolicy.STATELESS )
//                ;
//    }
//
//    @Override
//    public void configure(ResourceServerSecurityConfigurer config) {
//        config.tokenServices( tokenServices );
//    }
//
//}
