package com.hrms.employee.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckOutRequestDTO {
	
	@NotNull(message = "Employee ID is required")
    private Long employeeId;

}
