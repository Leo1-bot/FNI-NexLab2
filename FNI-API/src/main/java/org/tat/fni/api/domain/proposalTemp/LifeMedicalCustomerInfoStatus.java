package org.tat.fni.api.domain.proposalTemp;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.tat.fni.api.common.IDInterceptor;
import org.tat.fni.api.common.TableName;
import org.tat.fni.api.common.UserRecorder;
import org.tat.fni.api.common.emumdata.CustomerStatus;

import lombok.Data;

@Entity
@Table(name = TableName.PROPOSAL_LIFE_MEDICAL_CUSTOMERINFOSTATUS_TEMP)
@TableGenerator(name = "CUSTOMERSTATUS_GEN", table = "ID_GEN", pkColumnName = "GEN_NAME", valueColumnName = "GEN_VAL", pkColumnValue = "CUSTOMERSTATUS_GEN", allocationSize = 10)
@Access(value = AccessType.FIELD)
@EntityListeners(IDInterceptor.class)
@Data
public class LifeMedicalCustomerInfoStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "CUSTOMERSTATUS_GEN")
	private String id;
	
	@Enumerated(EnumType.STRING)
	private CustomerStatus statusName;

	@Version
	private int version;
	
	@Embedded
	private UserRecorder recorder;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUSTOMERID", referencedColumnName = "ID")
	private LifeMedicalCustomer customer;
	
}
