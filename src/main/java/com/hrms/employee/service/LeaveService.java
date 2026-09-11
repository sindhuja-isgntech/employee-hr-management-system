package com.hrms.employee.service;

import com.hrms.employee.dto.*;
import com.hrms.employee.entity.*;
import com.hrms.employee.enums.LeaveStatus;
import com.hrms.employee.exception.InvalidOperationException;
import com.hrms.employee.exception.ResourceNotFoundException;
import com.hrms.employee.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class LeaveService {
	
	private final LeaveRequestRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public LeaveResponseDTO applyLeave(LeaveRequestDTO dto) {
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new InvalidOperationException("End date cannot be before start date");
        }

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + dto.getEmployeeId()));

        if (leaveRepository.hasOverlappingLeave(dto.getEmployeeId(), dto.getStartDate(), dto.getEndDate())) {
            throw new InvalidOperationException("Employee already has an overlapping active/pending leave request for this period.");
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(dto.getLeaveType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        return mapToDTO(leaveRepository.save(leaveRequest));
    }

    public List<LeaveResponseDTO> getMyLeaves(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<LeaveResponseDTO> getAllLeaves() {
        return leaveRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public LeaveResponseDTO getLeaveById(Long id) {
        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));
        return mapToDTO(leave);
    }

    public LeaveResponseDTO approveLeave(Long id) {
        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidOperationException("Only PENDING leave requests can be approved.");
        }

        String currentAdminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(currentAdminEmail).orElse(null);

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedBy(admin);

        return mapToDTO(leaveRepository.save(leave));
    }

    public LeaveResponseDTO rejectLeave(Long id, LeaveActionDTO actionDTO) {
        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidOperationException("Only PENDING leave requests can be rejected.");
        }

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setRejectionReason(actionDTO.getRejectionReason());

        return mapToDTO(leaveRepository.save(leave));
    }

    public LeaveResponseDTO cancelLeave(Long id) {
        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidOperationException("Only PENDING leave requests can be cancelled.");
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        return mapToDTO(leaveRepository.save(leave));
    }

    private LeaveResponseDTO mapToDTO(LeaveRequest leave) {
        LeaveResponseDTO dto = new LeaveResponseDTO();
        dto.setId(leave.getId());
        dto.setEmployeeId(leave.getEmployee().getId());
        dto.setEmployeeName(leave.getEmployee().getFirstName() + " " + leave.getEmployee().getLastName());
        dto.setLeaveType(leave.getLeaveType());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus());
        dto.setRejectionReason(leave.getRejectionReason());
        if (leave.getApprovedBy() != null) {
            dto.setApprovedByName(leave.getApprovedBy().getName());
        }
        dto.setCreatedAt(leave.getCreatedAt());
        return dto;
    }

}
