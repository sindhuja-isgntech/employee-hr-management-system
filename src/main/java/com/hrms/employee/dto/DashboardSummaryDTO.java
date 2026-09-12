package com.hrms.employee.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardSummaryDTO {
	
	private long totalEmployees;
    private long activeEmployees;
    private long totalDepartments;
    private long pendingLeaves;
    private long employeesOnLeave;
    private long presentToday;

}
