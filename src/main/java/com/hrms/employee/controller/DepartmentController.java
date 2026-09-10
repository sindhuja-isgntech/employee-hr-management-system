package com.hrms.employee.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.employee.dto.DepartmentRequestDTO;
import com.hrms.employee.dto.DepartmentResponseDTO;
import com.hrms.employee.service.DepartmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

	private final DepartmentService departmentService;

	public DepartmentController(DepartmentService departmentService) {
		this.departmentService = departmentService;
	}

	@PostMapping
	public ResponseEntity<DepartmentResponseDTO> createDepartment(@Valid @RequestBody DepartmentRequestDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(dto));
	}

	@GetMapping
	public ResponseEntity<List<DepartmentResponseDTO>> getAllDepartments() {
		return ResponseEntity.ok(departmentService.getAllDepartments());
	}

	@GetMapping("/{id}")
	public ResponseEntity<DepartmentResponseDTO> getDepartmentById(@PathVariable Long id) {
		return ResponseEntity.ok(departmentService.getDepartmentById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<DepartmentResponseDTO> updateDepartment(@PathVariable Long id,
			@Valid @RequestBody DepartmentRequestDTO dto) {
		return ResponseEntity.ok(departmentService.updateDepartment(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
		departmentService.deleteDepartment(id);
		return ResponseEntity.noContent().build();
	}
}
