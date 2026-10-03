package com.hrms.employee.service;

import com.hrms.employee.dto.*;
import com.hrms.employee.entity.*;
import com.hrms.employee.enums.RoleType;
import com.hrms.employee.enums.Status;
import com.hrms.employee.exception.DuplicateResourceException;
import com.hrms.employee.exception.InvalidOperationException;
import com.hrms.employee.exception.ResourceNotFoundException;
import com.hrms.employee.repository.*;
import com.hrms.employee.security.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
	
	@Autowired
	private  AuthenticationManager authenticationManager;
	
	@Autowired
    private  UserRepository userRepository;
	
	@Autowired
    private  RoleRepository roleRepository;
	
	@Autowired
    private  PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.password-reset-expiration-minutes:30}")
    private long resetExpirationMinutes;

    @Value("${app.mail-from:no-reply@hrms.local}")
    private String mailFrom;

    @Value("${spring.mail.host:}")
    private String mailHost;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public JwtResponseDTO authenticateUser(LoginRequestDTO loginRequest) {
        
    	try {
    	Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail().trim(), loginRequest.getPassword()));
    	log.info("User login successful for email: {}", loginRequest.getEmail());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        org.springframework.security.core.userdetails.User userDetails =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        Set<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toSet());

        return new JwtResponseDTO(jwt, userDetails.getUsername(), roles);
    	}catch (BadCredentialsException e) {
            log.warn("Failed login attempt for email: {}", loginRequest.getEmail());
            throw e;
        }
    }

        public UserProfileDTO getCurrentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
            .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found."));

        Set<String> roles = user.getRoles().stream()
            .map(role -> "ROLE_" + role.getName().name())
            .collect(Collectors.toSet());

        return new UserProfileDTO(user.getId(), user.getName(), user.getEmail(), roles);
        }

    public void changePassword(String email, ChangePasswordRequestDTO request) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
            .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found."));

        if (!encoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidOperationException("Current password is incorrect.");
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        if (mailHost == null || mailHost.isBlank()) {
            log.warn("Password reset requested while MAIL_HOST is not configured.");
            return;
        }

        userRepository.findByEmailIgnoreCase(email.trim()).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUser(user);

            byte[] randomToken = new byte[32];
            SECURE_RANDOM.nextBytes(randomToken);
            String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomToken);
            PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hashResetToken(rawToken))
                .expiresAt(Instant.now().plus(Duration.ofMinutes(resetExpirationMinutes)))
                .build();
            passwordResetTokenRepository.save(resetToken);

            String resetLink = frontendUrl.replaceAll("/+$", "") + "/reset-password?token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(user.getEmail());
            message.setSubject("Reset your HRMS password");
            message.setText("Use the link below to reset your password. This link expires in "
                + resetExpirationMinutes + " minutes.\n\n" + resetLink
                + "\n\nIf you did not request this change, you can ignore this email.");

            try {
                mailSender.send(message);
            } catch (MailException exception) {
                passwordResetTokenRepository.delete(resetToken);
                log.error("Unable to send password reset email for user id {}.", user.getId(), exception);
            }
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(hashResetToken(request.getToken()))
            .orElseThrow(() -> new InvalidOperationException("This password reset link is invalid or expired."));

        if (!resetToken.getExpiresAt().isAfter(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidOperationException("This password reset link is invalid or expired.");
        }

        User user = resetToken.getUser();
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
    }

    private String hashResetToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }

    public String registerUser(RegisterRequestDTO registerRequest) {
        String email = registerRequest.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email is already taken!");
        }

        User user = User.builder()
                .name(registerRequest.getName())
                .email(email)
                .password(encoder.encode(registerRequest.getPassword()))
                .status(Status.ACTIVE)
                .build();

        Set<String> strRoles = registerRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(RoleType.EMPLOYEE)
                    .orElseThrow(() -> new ResourceNotFoundException("Error: Role EMPLOYEE is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toUpperCase()) {
                    case "ADMIN":
                        Role adminRole = roleRepository.findByName(RoleType.ADMIN)
                                .orElseThrow(() -> new ResourceNotFoundException("Error: Role ADMIN is not found."));
                        roles.add(adminRole);
                        break;
                    case "HR":
                        Role hrRole = roleRepository.findByName(RoleType.HR)
                                .orElseThrow(() -> new ResourceNotFoundException("Error: Role HR is not found."));
                        roles.add(hrRole);
                        break;
                    default:
                        Role empRole = roleRepository.findByName(RoleType.EMPLOYEE)
                                .orElseThrow(() -> new ResourceNotFoundException("Error: Role EMPLOYEE is not found."));
                        roles.add(empRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);
        return "User registered successfully!";
    }
	
	

}
