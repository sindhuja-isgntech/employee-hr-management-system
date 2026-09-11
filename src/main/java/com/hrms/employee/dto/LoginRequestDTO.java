package com.hrms.employee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.Set;

@Data
public class LoginRequestDTO {
	
	@NotBlank @Email private String email;
    @NotBlank private String password;

}
