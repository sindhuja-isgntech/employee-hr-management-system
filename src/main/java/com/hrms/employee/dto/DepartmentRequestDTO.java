package com.hrms.employee.dto;

import java.time.LocalDateTime;

import com.hrms.employee.enums.Status;
import lombok.Data;

@Data
public class DepartmentRequestDTO {
	
	private Long id;
    private String name;
    private String description;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
