package com.hrms.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hrms.employee.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{
	
	Optional<User> findByEmail(String email);
	Boolean existsByEmail(String email);

}
