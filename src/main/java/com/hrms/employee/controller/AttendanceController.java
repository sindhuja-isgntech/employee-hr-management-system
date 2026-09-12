package com.hrms.employee.controller;


import com.hrms.employee.dto.*;
import com.hrms.employee.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

	private final AttendanceService attendanceService;

    // Employee APIs
    @PostMapping("/check-in")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AttendanceResponseDTO> checkIn(@Valid @RequestBody CheckInRequestDTO dto) {
        return new ResponseEntity<>(attendanceService.checkIn(dto), HttpStatus.CREATED);
    }

    @PutMapping("/check-out")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AttendanceResponseDTO> checkOut(@Valid @RequestBody CheckOutRequestDTO dto) {
        return ResponseEntity.ok(attendanceService.checkOut(dto));
    }

    @GetMapping("/my/{employeeId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<AttendanceResponseDTO>> getMyAttendance(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getMyAttendance(employeeId));
    }

    // HR / Admin APIs
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<List<AttendanceResponseDTO>> getAllAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAllAttendance(date));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<List<AttendanceResponseDTO>> getAttendanceByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByEmployee(employeeId));
    }
}
