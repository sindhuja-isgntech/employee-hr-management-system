package com.hrms.employee.dto;

import lombok.Data;


import jakarta.validation.constraints.*;


import java.math.BigDecimal;
import java.time.LocalDate;

import com.hrms.employee.enums.Status;

@Data
public class EmployeeRequestDTO {

	@NotBlank(message = "Employee code is required")
    private String employeeCode;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotNull(message = "Date of joining is required")
    @PastOrPresent(message = "Date of joining cannot be in the future")
    private LocalDate dateOfJoining;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than zero")
    private BigDecimal salary;

    private Status status = Status.ACTIVE;

    @NotNull(message = "Department ID is required")
    private Long departmentId;
}
