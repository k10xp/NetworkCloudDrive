package com.cloud.NetworkCloudDrive.Configuration;

import com.cloud.NetworkCloudDrive.Utilities.SecurityUtility;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final SecurityUtility securityUtility;

    public SecurityConfig(SecurityUtility securityUtility) {
        this.securityUtility = securityUtility;
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        // give everyone access to these 2 endpoints
                        .requestMatchers("/api/user/login").permitAll()
                        .requestMatchers("/api/user/register").permitAll()
                        .requestMatchers("/api/user/info").hasRole("GUEST")
                        // but require authentication for any other endpoint
                        .anyRequest()
                        .authenticated()
                        // temporarily disable csrf protection
                ).csrf(AbstractHttpConfigurer::disable) // blocks POST and cross-platform attacks read more about it
                // give everyone access to log out
                .logout(LogoutConfigurer::permitAll);
        return http.build();
    }

    // default strength is 10 might bump it up to 16
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(securityUtility);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }
}
