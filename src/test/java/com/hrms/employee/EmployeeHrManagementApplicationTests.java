package com.hrms.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.hrms.employee.service.EmployeeService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class EmployeeHrManagementApplicationTests {
	@Autowired
	private EmployeeService employeeService;

	@Test
	void contextLoads() {
	}

	@Test
	void getEmployeesWithoutSearchTerm() {
		assertDoesNotThrow(() -> employeeService.getEmployees(null, null, null, 0, 10, "id", "asc"));
	}

}
