package org.tat.fni.api.domain.proposalTemp;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.tat.fni.api.common.IDInterceptor;
import org.tat.fni.api.common.TableName;
import org.tat.fni.api.common.UserRecorder;

import lombok.Data;

@Entity
@Table(name = TableName.PROPOSAL_LIFE_MEDICAL_GUARDIAN_TEMP)
@TableGenerator(name = "GUARDIAN_GEN", table = "ID_GEN", pkColumnName = "GEN_NAME", valueColumnName = "GEN_VAL", pkColumnValue = "GUARDIAN_GEN", allocationSize = 10)
@EntityListeners(IDInterceptor.class)
@Data
public class LifeMedicalGuardian {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "GUARDIAN_GEN")
	private String id;
	
	private String guardianNo;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUSTOMERID", referencedColumnName = "ID")
	private LifeMedicalCustomer customer;
	
	private String relationshipId;
	
	@Version
	private int version;
	
	@Embedded
	private UserRecorder recorder;

}
