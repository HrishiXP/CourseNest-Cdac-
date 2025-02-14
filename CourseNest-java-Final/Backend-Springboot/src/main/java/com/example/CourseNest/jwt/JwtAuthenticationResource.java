package com.example.CourseNest.jwt;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.CourseNest.model.AuthRequest;

@RestController
public class JwtAuthenticationResource {

    record JwtResponse(String token, String role, String name, Long id) {
    }

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @PostMapping("/authenticate")
    public JwtResponse authenticate(@RequestBody AuthRequest authRequest) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(authRequest.getEmail());

        if (userDetails != null && userDetails.getPassword().equals("{noop}" + authRequest.getPassword())) {
            // Generate token
            String token = createdToken(userDetails);

            // Extract role and name
            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("User");

            String name = userDetails.getUsername();

            Long id = ((CustomUserDetails) userDetails).getId();

            return new JwtResponse(token, role, name, id);
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

    private String createdToken(UserDetails userDetails) {
        return jwtEncoder.encode(JwtEncoderParameters.from(JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60 * 15))
                .subject(userDetails.getUsername())
                .claim("scope", createdScope(userDetails))
                .build())).getTokenValue();
    }

    private String createdScope(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
    }
}
