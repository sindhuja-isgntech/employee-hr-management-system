package com.hrms.employee.service;

import com.hrms.employee.dto.*;
import com.hrms.employee.entity.*;
import com.hrms.employee.enums.RoleType;
import com.hrms.employee.enums.Status;
import com.hrms.employee.exception.DuplicateResourceException;
import com.hrms.employee.exception.ResourceNotFoundException;
import com.hrms.employee.repository.*;
import com.hrms.employee.security.JwtUtils;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


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

    public JwtResponseDTO authenticateUser(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        org.springframework.security.core.userdetails.User userDetails =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        Set<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toSet());

        return new JwtResponseDTO(jwt, userDetails.getUsername(), roles);
    }

    public String registerUser(RegisterRequestDTO registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateResourceException("Email is already taken!");
        }

        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
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
