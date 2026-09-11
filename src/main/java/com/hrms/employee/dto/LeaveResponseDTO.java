package com.hrms.employee.dto;

import com.hrms.employee.enums.LeaveStatus;
import com.hrms.employee.enums.LeaveType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveResponseDTO {
	
	private Long id;
    private Long employeeId;
    private String employeeName;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;
    private String rejectionReason;
    private String approvedByName;
    private LocalDateTime createdAt;

}
