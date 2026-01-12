package gr.hua.dit.my.city.gov.core.repository;

import gr.hua.dit.my.city.gov.core.model.EmployeeDecision;
import gr.hua.dit.my.city.gov.core.model.Request;
import gr.hua.dit.my.city.gov.core.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
	List<Request> findByPersonId(Long personId);
	List<Request> findByCitizenId(Long personId);

	List<Request> findByRequestType_ServiceUnit_IdOrderByCreatedAtDesc(Long serviceUnitId);
	long countByRequestType_ServiceUnit_Id(Long serviceUnitId);
	List<Request> findByRequestType_ServiceUnit_IdOrderByIdDesc(Long serviceUnitId);

	List<Request> findByRequestType_ServiceUnit_IdAndAssignedEmployee_IdOrderByCreatedAtDesc(Long serviceUnitId, Long employeeId);
	List<Request> findByRequestType_ServiceUnit_IdAndAssignedEmployeeIsNullOrderByCreatedAtDesc(Long serviceUnitId);

	// Atomic claim: παίρνω το αίτημα μόνο αν ΔΕΝ έχει assignee
	@Modifying
	@Query("""
        update Request r
           set r.assignedEmployee.id = :employeeId,
               r.assignedAt = :assignedAt
         where r.id = :requestId
           and r.assignedEmployee is null
    """)
	int claimIfUnassigned(@Param("requestId") Long requestId,
						  @Param("employeeId") Long employeeId,
						  @Param("assignedAt") LocalDateTime assignedAt);

	// Unclaim: μόνο ο ίδιος που το έχει μπορεί να το αφήσει
	@Modifying
	@Query("""
        update Request r
           set r.assignedEmployee = null,
               r.assignedAt = null
         where r.id = :requestId
           and r.assignedEmployee.id = :employeeId
    """)
	int unclaimIfOwned(@Param("requestId") Long requestId,
					   @Param("employeeId") Long employeeId);

	@Modifying
	@Query("""
    update Request r
       set r.status = :status,
           r.completedAt = :completedAt
     where r.id = :id
  """)
	int updateStatusOnly(@Param("id") Long id,
						 @Param("status") RequestStatus status,
						 @Param("completedAt") LocalDateTime completedAt);

	@Modifying
	@Query("""
    update Request r
       set r.employeeDecision = :decision,
           r.employeeDecisionReason = :reason,
           r.employeeDecidedAt = :decidedAt
     where r.id = :id
  """)
	int updateDecision(@Param("id") Long id,
					   @Param("decision") EmployeeDecision decision,
					   @Param("reason") String reason,
					   @Param("decidedAt") LocalDateTime decidedAt);
}

