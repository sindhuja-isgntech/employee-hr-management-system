package com.hrms.employee.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.hrms.employee.dto.EmployeeRequestDTO;
import com.hrms.employee.dto.EmployeeResponseDTO;
import com.hrms.employee.entity.Department;
import com.hrms.employee.entity.Employee;
import com.hrms.employee.enums.Status;
import com.hrms.employee.exception.DuplicateResourceException;
import com.hrms.employee.exception.ResourceNotFoundException;
import com.hrms.employee.repository.DepartmentRepository;
import com.hrms.employee.repository.EmployeeRepository;

@Service
public class EmployeeService {
	
	private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Employee with email " + dto.getEmail() + " already exists");
        }
        if (employeeRepository.existsByEmployeeCode(dto.getEmployeeCode())) {
            throw new DuplicateResourceException("Employee code " + dto.getEmployeeCode() + " already exists");
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + dto.getDepartmentId()));

        Employee employee = Employee.builder()
                .employeeCode(dto.getEmployeeCode())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .designation(dto.getDesignation())
                .dateOfJoining(dto.getDateOfJoining())
                .salary(dto.getSalary())
                .status(dto.getStatus())
                .department(department)
                .build();

        return mapToDTO(employeeRepository.save(employee));
    }

    public Page<EmployeeResponseDTO> getEmployees(String search, Long departmentId, Status status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Employee> employeePage = employeeRepository.searchAndFilterEmployees(search, departmentId, status, pageable);
        return employeePage.map(this::mapToDTO);
    }

    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
        return mapToDTO(employee);
    }

    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + dto.getDepartmentId()));

        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setPhone(dto.getPhone());
        employee.setDesignation(dto.getDesignation());
        employee.setDateOfJoining(dto.getDateOfJoining());
        employee.setSalary(dto.getSalary());
        employee.setStatus(dto.getStatus());
        employee.setDepartment(department);

        return mapToDTO(employeeRepository.save(employee));
    }

    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + id);
        }
        employeeRepository.deleteById(id);
    }

    public List<EmployeeResponseDTO> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private EmployeeResponseDTO mapToDTO(Employee emp) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(emp.getId());
        dto.setEmployeeCode(emp.getEmployeeCode());
        dto.setFirstName(emp.getFirstName());
        dto.setLastName(emp.getLastName());
        dto.setEmail(emp.getEmail());
        dto.setPhone(emp.getPhone());
        dto.setDesignation(emp.getDesignation());
        dto.setDateOfJoining(emp.getDateOfJoining());
        dto.setSalary(emp.getSalary());
        dto.setStatus(emp.getStatus());
        if (emp.getDepartment() != null) {
            dto.setDepartmentId(emp.getDepartment().getId());
            dto.setDepartmentName(emp.getDepartment().getName());
        }
        dto.setCreatedAt(emp.getCreatedAt());
        dto.setUpdatedAt(emp.getUpdatedAt());
        return dto;
    }

}
