package org.tat.fni.api.domain.proposalTemp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalCustomer;

public interface LifeMedicalCustomerRepository extends JpaRepository<LifeMedicalCustomer, String> {
	
	@Query(value = "SELECT * FROM PROPOSAL_LIFE_MEDICAL_CUSTOMER_TEMP WHERE FULLIDNO LIKE %?1 AND IDTYPE = ?2", nativeQuery = true)
	LifeMedicalCustomer findCustomerByIdNoAndIdType(String idNo, String idType);

}
