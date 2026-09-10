package com.hrms.employee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hrms.employee.enums.Status;
import lombok.Data;

@Data
public class EmployeeResponseDTO {
	
	
	private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String designation;
    private LocalDate dateOfJoining;
    private BigDecimal salary;
    private Status status;
    private Long departmentId;
    private String departmentName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
