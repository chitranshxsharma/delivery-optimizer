package com.delivery.service;

import com.delivery.dto.JwtResponse;
import com.delivery.dto.LoginRequest;
import com.delivery.dto.RegisterRequest;
import com.delivery.exception.BadRequestException;
import com.delivery.model.User;
import com.delivery.repository.UserRepository;
import com.delivery.security.JwtUtils;
import com.delivery.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired private AuthenticationManager authManager;
    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JwtUtils jwtUtils;

    public JwtResponse login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwt = jwtUtils.generateToken(auth);
        UserDetailsImpl u = (UserDetailsImpl) auth.getPrincipal();
        String role = u.getAuthorities().iterator().next().getAuthority();
        return new JwtResponse(jwt, u.getId(), u.getName(), u.getUsername(), role);
    }

    public void register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail()))
            throw new BadRequestException("Email already registered: " + req.getEmail());

        User.UserRole role = User.UserRole.ROLE_DRIVER;
        if (req.getRole() != null) {
            try { role = User.UserRole.valueOf("ROLE_" + req.getRole().toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        userRepo.save(User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .role(role)
                .build());
    }
}
