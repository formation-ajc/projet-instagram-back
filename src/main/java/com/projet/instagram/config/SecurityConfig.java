package com.projet.instagram.config;

import com.projet.instagram.service.security.UserDetailsServiceImpl;
import com.projet.instagram.utils.security.AuthenticationTokenFilter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    final
    UserDetailsServiceImpl userDetailsService;

    @Value("${api.baseURL}")
    private String baseURL;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthenticationTokenFilter authenticationJwtTokenFilter() {
        return new AuthenticationTokenFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        String hierarchy = "SUPER > ADMIN > USER";
        roleHierarchy.setHierarchy(hierarchy);
        return roleHierarchy;
    }

    @Bean
    public DefaultWebSecurityExpressionHandler webSecurityExpressionHandler() {
        DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy());
        return expressionHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            //désactivation de la gestion des en-têtes CORS au sein de Spring Security
            .cors(request -> new CorsConfiguration().applyPermitDefaultValues())

            //désactivation du CSRF (Cross-Site Request Forgery)
            .csrf(AbstractHttpConfigurer::disable)

            //Configuration des règles d'autorisations concernant les requêtes HTTP
            .authorizeHttpRequests(auth -> auth

                //Toutes les requêtes HTTP listées sont autorisées pour tout le monde
                .requestMatchers(baseURL + "/login", baseURL + "/register", baseURL + "/refresh-token").permitAll()

                //on gere les droits sur les routes ici (voir la hierarchie dans la fonction d'avant)
                .requestMatchers(baseURL + "/test/user").hasAnyAuthority("USER")
                .requestMatchers(baseURL + "/test/admin").hasAnyAuthority("ADMIN")
                .requestMatchers(baseURL + "/test/super").hasAnyAuthority("SUPER")

                //Toutes les autres requêtes HTTP nécessitent une authentification
                .anyRequest().authenticated()

            )

            // Configuration de la session Spring Security : AUCUNE session ne sera créée coté serveur
            // Moins coûteux et inutile lorsque nous sommes dans une configuration RESTful
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Ajout d'un filtre personnalisé qui s'exécutera avant le filtre UsernamePasswordAuthenticationFilter
            // Filtre pour gérer l'authentification basée sur le JWT reçu dans les en-têtes de requêtes
            // Le filtre UsernamePasswordAuthenticationFilter est un filtre de base de Spring Security
            // Il est exécuté pour gérer l'authentification par username et mot de passe
            .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/proxy/**");
    }


    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
            .info(new Info().title("INSTAGRAM API")
                .description("Some custom description of API.")
                .version("1.0").contact(new Contact().name("Robin FOUTEL")
                    .email( "robin.foutel@gmail.com").url("http://localhost:8080/swagger-ui/index.html"))
                .license(new License().name("License of API")
                    .url("API license URL")));
    }
}