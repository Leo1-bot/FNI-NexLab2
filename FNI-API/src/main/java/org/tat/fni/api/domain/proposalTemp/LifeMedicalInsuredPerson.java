package org.tat.fni.api.domain.proposalTemp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.tat.fni.api.common.IDInterceptor;
import org.tat.fni.api.common.Name;
import org.tat.fni.api.common.RegNoSorter;
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
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LIFEPROPOSALID", referencedColumnName = "ID")
	private LifeMedicalProposal lifeProposal;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUSTOMERID", referencedColumnName = "ID")
	private LifeMedicalCustomer customer;

	private String occupationId;
	private String productId;
	private String typesOfSportId;
	private String relationshipId;
	private int approvedUnit;
	private int weight;
	private int height;
	private double premiumRate;
	private String riskyOccupationId;
	private String phone;
	
	@Temporal(TemporalType.DATE)
	private Date parentDOB;
	
	@Column(name = "GRATEINFOID")
	private String gradeInfoId;
	private String parentName;
	private String parentIdNo;
	
	@Enumerated(value = EnumType.STRING)
	private IdType parentIdType;
	
	private String schoolId;
	
	@Transient
	private Boolean isPaidPremiumForPaidup = false;
	
	private double premium;
	private boolean sameCustomer;
	
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "GUARDIANID", referencedColumnName = "ID")
	private LifeMedicalGuardian guardian;
	
	private String medicalProposalId;
	private double sumInsured;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "proposalInsuredPerson", orphanRemoval = true)
	private List<LifeMedicalInsuredPersonBeneficiary> insuredPersonBeneficiariesList;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "MEDIPROPOSALINSUREDPERSONID", referencedColumnName = "ID")
	private List<LifeMedicalProposalInsuredPersonAddOn> insuredPersonAddOnList;
	
	public List<LifeMedicalInsuredPersonBeneficiary> getInsuredPersonBeneficiariesList() {
		if (this.insuredPersonBeneficiariesList == null) {
			this.insuredPersonBeneficiariesList = new ArrayList<LifeMedicalInsuredPersonBeneficiary>();
		}
		return this.insuredPersonBeneficiariesList;
	}
	
	/* sort by addOn Name */
	public List<LifeMedicalProposalInsuredPersonAddOn> getInsuredPersonAddOnList() {
		if (insuredPersonAddOnList == null) {
			insuredPersonAddOnList = new ArrayList<LifeMedicalProposalInsuredPersonAddOn>();
		}
		if (insuredPersonAddOnList.isEmpty()) {
			return insuredPersonAddOnList;
		} else {
			RegNoSorter<LifeMedicalProposalInsuredPersonAddOn> sorter = new RegNoSorter<LifeMedicalProposalInsuredPersonAddOn>(insuredPersonAddOnList);
			return sorter.getSortedList();
		}
	}
	
}
