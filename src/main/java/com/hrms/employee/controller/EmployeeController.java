package com.hrms.employee.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.employee.dto.EmployeeRequestDTO;
import com.hrms.employee.dto.EmployeeResponseDTO;
import com.hrms.employee.enums.Status;
import com.hrms.employee.service.EmployeeService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

	private final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@PostMapping
	public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(dto));
	}

	@GetMapping
	public ResponseEntity<Page<EmployeeResponseDTO>> getEmployees(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) Long departmentId,
			@RequestParam(required = false) Status status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDir) {
		return ResponseEntity.ok(employeeService.getEmployees(search, departmentId, status, page, size, sortBy, sortDir));
	}

	@GetMapping("/{id}")
	public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
		return ResponseEntity.ok(employeeService.getEmployeeById(id));
	}

	@GetMapping("/department/{departmentId}")
	public ResponseEntity<List<EmployeeResponseDTO>> getEmployeesByDepartment(@PathVariable Long departmentId) {
		return ResponseEntity.ok(employeeService.getEmployeesByDepartment(departmentId));
	}

	@PutMapping("/{id}")
	public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable Long id,
			@Valid @RequestBody EmployeeRequestDTO dto) {
		return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
		employeeService.deleteEmployee(id);
		return ResponseEntity.noContent().build();
	}
}
