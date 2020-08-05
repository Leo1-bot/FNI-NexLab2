package org.tat.fni.api.domain.proposalTemp;

import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.tat.fni.api.common.IDInterceptor;
import org.tat.fni.api.common.Name;
import org.tat.fni.api.common.OfficeAddress;
import org.tat.fni.api.common.PermanentAddress;
import org.tat.fni.api.common.ResidentAddress;
import org.tat.fni.api.common.TableName;
import org.tat.fni.api.common.UserRecorder;
import org.tat.fni.api.common.emumdata.ContentInfo;
import org.tat.fni.api.common.emumdata.Gender;
import org.tat.fni.api.common.emumdata.IdType;
import org.tat.fni.api.common.emumdata.MaritalStatus;
import org.tat.fni.api.common.emumdata.PassportType;
import org.tat.fni.api.domain.Industry;

import lombok.Data;

@Entity
@Table(name = TableName.PROPOSAL_LIFE_MEDICAL_CUSTOMER_TEMP)
@TableGenerator(name = "CUSTOMER_GEN", table = "ID_GEN", pkColumnName = "GEN_NAME", valueColumnName = "GEN_VAL", pkColumnValue = "CUSTOMER_GEN", allocationSize = 10)
@EntityListeners(IDInterceptor.class)
@Data
public class LifeMedicalCustomer {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "CUSTOMER_GEN")
	private String id;
	
	private int activePolicy;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date activedDate;

	private String bankAccountNo;
	private String birthMark;

	private int closedPolicy;

	@Temporal(TemporalType.DATE)
	private Date dateOfBirth;

	private String fatherName;
	private String fullIdNo;
	
	@Enumerated(value = EnumType.STRING)
	private Gender gender;
	
	private double height;
	
	@Enumerated(value = EnumType.STRING)
	private IdType idType;
	
	private String initialId;
	private String labourNo;
	
	@Enumerated(value = EnumType.STRING)
	private MaritalStatus maritalStatus;

	@Enumerated(value = EnumType.STRING)
	private PassportType passportType;

	private String placeOfBirth;
	private String salary;
	
	@Version
	private int version;
	
	private double weight;
	
	@Embedded
	private ContentInfo contentInfo;

	@Embedded
	private Name name;
	
	@Embedded
	private OfficeAddress officeAddress;

	@Embedded
	private PermanentAddress permanentAddress;

	@Embedded
	private UserRecorder recorder;

	@Embedded
	private ResidentAddress residentAddress;
	
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "PROPOSAL_LIFE_MEDICAL_CUSTOMERFAMILY_TEMP", joinColumns = @JoinColumn(name = "CUSTOMERID", referencedColumnName = "ID"))
	private List<LifeMedicalCustomerFamily> familyInfo;
	
	//@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "customer", orphanRemoval = true)
	//private List<LifeMedicalCustomerInfoStatus> customerStatusList;

	private String bankBranchId;
	private String branchId;
	private String nationalityId;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "INDURSTRYID", referencedColumnName = "ID")
	private Industry industry;
	
	private String occupationId;
	private String qualificationId;
	private String religionId;
	private boolean isExisting;
	private boolean status;
	private String referenceId;
	
}
