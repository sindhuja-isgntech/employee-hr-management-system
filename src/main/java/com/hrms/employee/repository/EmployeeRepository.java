package com.hrms.employee.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hrms.employee.entity.Department;
import com.hrms.employee.entity.Employee;
import com.hrms.employee.enums.Status;

public interface EmployeeRepository extends JpaRepository<Employee, Long>{

	boolean existsByEmail(String email);

	boolean existsByEmployeeCode(String employeeCode);

	@Query("""
			SELECT e FROM Employee e
			WHERE (:search IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
			   OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
			   OR LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))
			   OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')))
			AND (:departmentId IS NULL OR e.department.id = :departmentId)
			AND (:status IS NULL OR e.status = :status)
			""")
	Page<Employee> searchAndFilterEmployees(@Param("search") String search,
			@Param("departmentId") Long departmentId,
			@Param("status") Status status,
			Pageable pageable);

	List<Employee> findByDepartmentId(Long departmentId);
	
	long countByStatus(Status status);

}
