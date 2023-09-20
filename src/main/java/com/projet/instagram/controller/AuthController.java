package com.projet.instagram.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.projet.instagram.exception.TokenRefreshException;
import com.projet.instagram.model.User;
import com.projet.instagram.model.security.ERole;
import com.projet.instagram.model.security.RefreshToken;
import com.projet.instagram.model.security.Role;
import com.projet.instagram.repository.UserRepository;
import com.projet.instagram.repository.security.RoleRepository;
import com.projet.instagram.schema.request.security.LoginRequest;
import com.projet.instagram.schema.request.security.RefreshTokenRequest;
import com.projet.instagram.schema.request.security.SignupRequest;
import com.projet.instagram.utils.security.JwtUtils;
import com.projet.instagram.schema.response.security.MessageResponse;
import com.projet.instagram.schema.response.security.SignupResponse;
import com.projet.instagram.schema.response.security.TokenRefreshResponse;
import com.projet.instagram.service.security.RefreshTokenService;
import com.projet.instagram.service.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("${api.baseURL}")
public class AuthController {

    AuthenticationManager authenticationManager;

    UserRepository userRepository;

    RoleRepository roleRepository;

    PasswordEncoder encoder;

    JwtUtils jwtUtils;

    RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder encoder,
                          JwtUtils jwtUtils,
                          RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        try {
            Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            String jwt = jwtUtils.generateJwtToken(userDetails);

            List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

            User user = null;
            if (userRepository.findById(userDetails.getId()).isPresent()) {
                user = userRepository.findById(userDetails.getId()).get();
            }

            assert user != null;
            return ResponseEntity.ok(new SignupResponse(jwt, refreshToken.getToken(), userDetails.getId(), userDetails.getEmail(), user.getFirstname(), user.getLastname(), roles));
        }
        catch(Exception e) {// see note 2
                return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: User not exist!"));
            }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        if (!signUpRequest.getPassword().equals(signUpRequest.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Passwords must be the same!"));
        }

        User user = new User(signUpRequest.getFirstname(), signUpRequest.getLastname(), signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin" -> {
                        Role adminRole = roleRepository.findByName(ERole.ADMIN)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                    }
                    case "super" -> {
                        Role superRole = roleRepository.findByName(ERole.SUPER)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(superRole);
                    }
                    default -> {
                        Role userRole = roleRepository.findByName(ERole.USER)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                    }
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromEmail(user.getEmail());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }
}
