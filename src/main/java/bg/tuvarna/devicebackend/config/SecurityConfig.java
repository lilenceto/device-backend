package bg.tuvarna.devicebackend.config;

import bg.tuvarna.devicebackend.models.enums.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ApplicationConfig applicationConfig;
    private final AuthenticationFilter authenticationFilter;
    private final Environment environment;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(cors -> cors.configurationSource(SecurityConfig::getCorsConfiguration));

        http.authorizeHttpRequests(auth -> {

            // ✅ Разрешаваме достъп до паспортите без токен само при test профил
            if (Arrays.asList(environment.getActiveProfiles()).contains("test")) {
                auth.requestMatchers("/api/v1/passports/**").permitAll();
            }

            // --- Свободен достъп до login, registration и swagger ---
            auth.requestMatchers(
                    "/api/v1/users/login",
                    "/api/v1/users/registration",
                    "/swagger-ui",
                    "/swagger",
                    "/swagger-ui/**",
                    "/swagger/**",
                    "/login/**"
            ).permitAll();

            // --- Свободен достъп до проверки за съществуване ---
            auth.requestMatchers(
                    "/api/v1/devices/exists/**",
                    "/api/v1/devices/anonymousDevice",
                    "/api/v1/passports/getBySerialId/*"
            ).permitAll();

            // --- USER и ADMIN могат да достъпват user/device endpoints ---
            auth.requestMatchers(
                    "/api/v1/users/update",
                    "/api/v1/users/getUser",
                    "/api/v1/users/changePassword",
                    "/api/v1/devices/**"
            ).hasAnyAuthority(UserRole.USER.toString(), UserRole.ADMIN.toString());

            // --- Само ADMIN за passports, renovations и user admin endpoints ---
            auth.requestMatchers(
                    "/api/v1/passports/**",
                    "/api/v1/users",
                    "/api/v1/users/*",
                    "/api/v1/renovations/**"
            ).hasAuthority(UserRole.ADMIN.toString());

            // --- Всичко останало изисква автентикация ---
            auth.anyRequest().authenticated();
        });

        // --- Настройки за Authentication и JWT филтри ---
        http.authenticationManager(applicationConfig.authenticationManager());
        http.addFilterAt(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // --- При липса на токен: 401 ---
        http.exceptionHandling(e ->
                e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        );

        return http.build();
    }

    // --- CORS конфигурация ---
    private static CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));
        configuration.setAllowedOriginPatterns(List.of("*"));
        return configuration;
    }
}
