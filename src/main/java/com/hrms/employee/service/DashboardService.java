package com.hrms.employee.service;


import com.hrms.employee.dto.DashboardSummaryDTO;
import com.hrms.employee.enums.LeaveStatus;
import com.hrms.employee.enums.Status;
import com.hrms.employee.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {
	
	@Autowired
	private  EmployeeRepository employeeRepository;
	
	@Autowired
    private DepartmentRepository departmentRepository;
	
	@Autowired
    private  LeaveRequestRepository leaveRequestRepository;
	
	@Autowired
    private  AttendanceRepository attendanceRepository;

    public DashboardSummaryDTO getDashboardSummary() {
        LocalDate today = LocalDate.now();

        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.countByStatus(Status.ACTIVE);
        long totalDepartments = departmentRepository.count();
        long pendingLeaves = leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
        long employeesOnLeave = leaveRequestRepository.countEmployeesOnLeave(today);
        long presentToday = attendanceRepository.countPresentEmployeesByDate(today);

        return DashboardSummaryDTO.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .totalDepartments(totalDepartments)
                .pendingLeaves(pendingLeaves)
                .employeesOnLeave(employeesOnLeave)
                .presentToday(presentToday)
                .build();
    }

}
