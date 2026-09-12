package com.hrms.employee.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckInRequestDTO {
	
	@NotNull(message = "Employee ID is required")
    private Long employeeId;

}
