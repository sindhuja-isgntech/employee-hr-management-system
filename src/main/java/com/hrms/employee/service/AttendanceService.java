package com.hrms.employee.service;


import com.hrms.employee.dto.*;
import com.hrms.employee.entity.*;
import com.hrms.employee.enums.AttendanceStatus;
import com.hrms.employee.exception.InvalidOperationException;
import com.hrms.employee.exception.ResourceNotFoundException;
import com.hrms.employee.repository.AttendanceRepository;
import com.hrms.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {
	
	private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceResponseDTO checkIn(CheckInRequestDTO dto) {
        LocalDate today = LocalDate.now();

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + dto.getEmployeeId()));

        if (attendanceRepository.existsByEmployeeIdAndAttendanceDate(dto.getEmployeeId(), today)) {
            throw new InvalidOperationException("Employee has already checked in for today.");
        }

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .attendanceDate(today)
                .checkIn(LocalDateTime.now())
                .status(AttendanceStatus.PRESENT)
                .build();

        return mapToDTO(attendanceRepository.save(attendance));
    }

    public AttendanceResponseDTO checkOut(CheckOutRequestDTO dto) {
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(dto.getEmployeeId(), today)
                .orElseThrow(() -> new InvalidOperationException("Cannot check out. No check-in record found for today."));

        if (attendance.getCheckOut() != null) {
            throw new InvalidOperationException("Employee has already checked out for today.");
        }

        LocalDateTime checkOutTime = LocalDateTime.now();
        attendance.setCheckOut(checkOutTime);

        // Calculate working hours
        long minutesWorked = Duration.between(attendance.getCheckIn(), checkOutTime).toMinutes();
        BigDecimal hoursWorked = BigDecimal.valueOf(minutesWorked)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        
        attendance.setWorkingHours(hoursWorked);

        // Adjust status if working hours are below threshold
        if (hoursWorked.compareTo(BigDecimal.valueOf(4)) < 0) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        }

        return mapToDTO(attendanceRepository.save(attendance));
    }

    public List<AttendanceResponseDTO> getMyAttendance(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponseDTO> getAllAttendance(LocalDate date) {
        List<Attendance> list = (date != null) ? 
                attendanceRepository.findByAttendanceDate(date) : attendanceRepository.findAll();

        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<AttendanceResponseDTO> getAttendanceByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AttendanceResponseDTO mapToDTO(Attendance attendance) {
        AttendanceResponseDTO dto = new AttendanceResponseDTO();
        dto.setId(attendance.getId());
        dto.setEmployeeId(attendance.getEmployee().getId());
        dto.setEmployeeName(attendance.getEmployee().getFirstName() + " " + attendance.getEmployee().getLastName());
        dto.setAttendanceDate(attendance.getAttendanceDate());
        dto.setCheckIn(attendance.getCheckIn());
        dto.setCheckOut(attendance.getCheckOut());
        dto.setWorkingHours(attendance.getWorkingHours());
        dto.setStatus(attendance.getStatus());
        return dto;
    }
	
	
	

}
