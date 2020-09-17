package com.cinlogic.jwtexampleserver.configuration;

import com.cinlogic.jwtexampleserver.security.JwtAuthenticationFilter;
import com.cinlogic.jwtexampleserver.security.JwtAuthenticationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.*;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String ACTUATOR_ADMIN_ROLE = "ActuatorAdmin";

    private static final RequestMatcher ANONYMOUS_ENDPOINTS = new OrRequestMatcher(
            new AntPathRequestMatcher("/accounts", "POST"),
            new AntPathRequestMatcher("/actuator/health", "GET"),
            new AntPathRequestMatcher("/actuator/info", "GET"),
            new AntPathRequestMatcher("/auth/token", "POST"),
            new AntPathRequestMatcher("/auth/token/refresh", "POST")
    );

    private static final RequestMatcher ACTUATOR_ENDPOINTS = new AndRequestMatcher(
            new AntPathRequestMatcher("/actuator/**"),
            new NegatedRequestMatcher(ANONYMOUS_ENDPOINTS)
    );

    private static final RequestMatcher JWT_AUTH_ENDPOINTS = new AndRequestMatcher(
            new AntPathRequestMatcher("/**"),
            new NegatedRequestMatcher(ANONYMOUS_ENDPOINTS),
            new NegatedRequestMatcher(ACTUATOR_ENDPOINTS)
    );

    @Value("${actuator.user}")
    String actuatorUser;

    @Value("${actuator.password}")
    String actuatorPassword;

    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public SecurityConfiguration(final JwtAuthenticationProvider jwtAuthenticationProvider, final BCryptPasswordEncoder bCryptPasswordEncoder) {
        super();
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(jwtAuthenticationProvider);
        auth.inMemoryAuthentication()
                .withUser(actuatorUser)
                .password(bCryptPasswordEncoder.encode(actuatorPassword))
                .roles(ACTUATOR_ADMIN_ROLE);
    }

    @Override
    public void configure(final WebSecurity webSecurity) {
        webSecurity.ignoring().requestMatchers(ANONYMOUS_ENDPOINTS);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.cors()
            .and().exceptionHandling()
            .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterAfter(jwtAuthenticationFilter(), BasicAuthenticationFilter.class)
            .authenticationProvider(jwtAuthenticationProvider);

        http.authorizeRequests()
            .requestMatchers(JWT_AUTH_ENDPOINTS)
            .authenticated();

        http.authorizeRequests()
            .requestMatchers(ACTUATOR_ENDPOINTS)
            .hasRole(ACTUATOR_ADMIN_ROLE)
            .and().httpBasic();

        http.authorizeRequests()
            .anyRequest()
            .anonymous();
    }

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        return new JwtAuthenticationFilter(JWT_AUTH_ENDPOINTS) {{
            setAuthenticationManager(authenticationManager());
        }};
    }
}