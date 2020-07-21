package org.tat.fni.api.domain.proposalTemp;

import java.util.Date;

import javax.persistence.Column;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.tat.fni.api.common.IDInterceptor;
import org.tat.fni.api.common.Name;
import org.tat.fni.api.common.ResidentAddress;
import org.tat.fni.api.common.TableName;
import org.tat.fni.api.common.UserRecorder;
import org.tat.fni.api.common.emumdata.ClassificationOfHealth;
import org.tat.fni.api.common.emumdata.EndorsementStatus;
import org.tat.fni.api.common.emumdata.Gender;
import org.tat.fni.api.common.emumdata.IdType;

import lombok.Data;

@Entity
@Table(name = TableName.PROPOSAL_LIFE_MEDICAL_INSUREDPERSON_TEMP)
@TableGenerator(name = "PRLINSURPERSON_GEN", table = "ID_GEN", pkColumnName = "GEN_NAME", valueColumnName = "GEN_VAL", pkColumnValue = "PRLINSURPERSON_GEN", allocationSize = 10)
@EntityListeners(IDInterceptor.class)
@Data
public class LifeMedicalInsuredPerson {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "PRLINSURPERSON_GEN")
	private String id;
	
	private double addOnTermPremium;
	
	@Column(name = "AGE")
	private int age;
	
	private boolean approved;
	private double approvedSumInsured;
	private double basicTermPremium;
	
	@Enumerated(value = EnumType.STRING)
	private ClassificationOfHealth clsOfHealth;
	
	@Temporal(TemporalType.DATE)
	private Date dateOfBirth;
	
	private double endorsementNetBasicPremium;
	private double endorsementNetAddonPremium;
	
	@Enumerated(EnumType.STRING)
	private EndorsementStatus endorsementStatus;

	private String fatherName;
	
	@Enumerated(value = EnumType.STRING)
	private Gender gender;
	
	private String idNo;
	
	@Enumerated(value = EnumType.STRING)
	private IdType idType;
	
	@Column(name = "INPERSONGROUPCODENO")
	private String inPersonGroupCodeNo;
	
	private String initialId;
	
	@Column(name = "INPERSONCODENO")
	private String insPersonCodeNo;
	
	private double interest;
	private boolean needMedicalCheckup;
	private double proposedPremium;
	private double proposedSumInsured;
	private String rejectReason;
	private int unit;
	
	@Version
	private int version;
	
	@Embedded
	private Name name;
	
	@Embedded
	private UserRecorder recorder;
	
	@Embedded
	private ResidentAddress residentAddress;

	private String residentTownshipId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LIFEPROPOSALID", referencedColumnName = "ID")
	private LifeMedicalProposal lifeMedicalProposal;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUSTOMERID", referencedColumnName = "ID")
	private LifeMedicalCustomer customer;

	private String occupationId;
	private String productId;
	private String typeOfSportId;
	private String relationshipId;
	private int approvedUnit;
	private int weight;
	private int height;
	private double premiumRate;
	private String riskyOccupationId;
	private String phone;
	
	@Temporal(TemporalType.DATE)
	private Date parentDOB;
	
	private String gradeInfo;
	private String parentName;
	private String parentIdNo;
	
	@Enumerated(value = EnumType.STRING)
	private IdType parentIdType;
	
	private String schoolId;
	
	@Transient
	private Boolean isPaidPremiumForPaidup = false;
	
	private double premium;
	private boolean sameCustomer;
	private String guardianId;
	private String medicalProposalId;
	private double sumInsured;
	
}
