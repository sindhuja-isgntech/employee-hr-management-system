package com.hrms.employee.dto;

import java.util.Set;

public record UserProfileDTO(Long id, String name, String email, Set<String> roles) {
}