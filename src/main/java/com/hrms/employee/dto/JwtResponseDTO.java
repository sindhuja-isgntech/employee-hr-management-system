package com.hrms.employee.dto;

import java.util.Set;

import lombok.Data;

@Data
public class JwtResponseDTO {
	
	private String token;
    private String type = "Bearer";
    private String email;
    private Set<String> roles;
    
    public JwtResponseDTO(String token, String email, Set<String> roles) {
        this.token = token;
        this.email = email;
        this.roles = roles;
    }

}
