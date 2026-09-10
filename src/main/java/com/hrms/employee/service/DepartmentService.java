package com.hrms.employee.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hrms.employee.dto.DepartmentRequestDTO;
import com.hrms.employee.dto.DepartmentResponseDTO;
import com.hrms.employee.entity.Department;
import com.hrms.employee.exception.DuplicateResourceException;
import com.hrms.employee.exception.ResourceNotFoundException;
import com.hrms.employee.repository.DepartmentRepository;

@Service
public class DepartmentService {
	
	private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO dto) {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Department with name '" + dto.getName() + "' already exists");
        }
        Department department = Department.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .build();
        return mapToResponseDTO(departmentRepository.save(department));
    }

    public List<DepartmentResponseDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public DepartmentResponseDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
        return mapToResponseDTO(department);
    }

    public DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO dto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        if (!department.getName().equals(dto.getName()) && departmentRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Department name '" + dto.getName() + "' is already in use");
        }

        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        department.setStatus(dto.getStatus());

        return mapToResponseDTO(departmentRepository.save(department));
    }

    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with ID: " + id);
        }
        departmentRepository.deleteById(id);
    }

    private DepartmentResponseDTO mapToResponseDTO(Department dept) {
        DepartmentResponseDTO dto = new DepartmentResponseDTO();
        dto.setId(dept.getId());
        dto.setName(dept.getName());
        dto.setDescription(dept.getDescription());
        dto.setStatus(dept.getStatus());
        dto.setCreatedAt(dept.getCreatedAt());
        dto.setUpdatedAt(dept.getUpdatedAt());
        return dto;
    }

}
