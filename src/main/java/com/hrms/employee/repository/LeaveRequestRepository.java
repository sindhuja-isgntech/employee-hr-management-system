package com.hrms.employee.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hrms.employee.entity.LeaveRequest;
import com.hrms.employee.enums.LeaveStatus;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>{
	
	List<LeaveRequest> findByEmployeeId(Long employeeId);

    @Query("SELECT COUNT(l) > 0 FROM LeaveRequest l WHERE l.employee.id = :employeeId " +
           "AND l.status IN ('PENDING', 'APPROVED') " +
           "AND (:startDate <= l.endDate AND :endDate >= l.startDate)")
    boolean hasOverlappingLeave(@Param("employeeId") Long employeeId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);
    
    // Add these methods to LeaveRequestRepository.java
    long countByStatus(LeaveStatus status);

    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.status = 'APPROVED' " +
           "AND :today BETWEEN l.startDate AND l.endDate")
    long countEmployeesOnLeave(@Param("today") LocalDate today);

}
