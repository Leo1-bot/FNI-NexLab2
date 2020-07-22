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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.tat.fni.api.common.IDInterceptor;
import org.tat.fni.api.common.TableName;
import org.tat.fni.api.common.UserRecorder;
import org.tat.fni.api.common.emumdata.ClassificationOfHealth;
import org.tat.fni.api.common.emumdata.ProposalStatus;
import org.tat.fni.api.common.emumdata.ProposalType;
import org.tat.fni.api.common.emumdata.SaleChannelType;
import org.tat.fni.api.domain.CustomerType;
import org.tat.fni.api.domain.HealthType;

import lombok.Data;

@Entity
@Table(name = TableName.PROPOSAL_LIFE_MEDICAL_TEMP)
@TableGenerator(name = "LIFEPROPOSAL_GEN", table = "ID_GEN", pkColumnName = "GEN_NAME", valueColumnName = "GEN_VAL", pkColumnValue = "LIFEPROPOSAL_GEN", allocationSize = 10)
@EntityListeners(IDInterceptor.class)
@Data
public class LifeMedicalProposal {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "LIFEPROPOSAL_GEN")
	private String id;

	private boolean complete;
	private boolean isnonfinancialendorse;
	private int paymentTerm;
	private String portalId;
	private String proposalNo;

	@Enumerated(EnumType.STRING)
	private ProposalType proposalType;

	@Enumerated(EnumType.STRING)
	private SaleChannelType saleChannelType;

	@Temporal(TemporalType.TIMESTAMP)
	private Date submittedDate;

	@Version
	private int version;

	@Embedded
	private UserRecorder recorder;

	private String agentId;
	private String branchId;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUSTOMERID", referencedColumnName = "ID")
	private LifeMedicalCustomer customer;
	
	private String lifePolicyId;
	private String organizationId;
	private String paymentTypeId;
	private String saleBankId;

	@Enumerated(EnumType.STRING)
	private ProposalStatus proposalStatus;

	private float specialDiscount;

	@Column(name = "PERIODOFMONTH")
	private int periodMonth;

	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	private double currencyRate;
	private String salesPointsId;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "CUSTOMERCLSOFHEALTH")
	private ClassificationOfHealth clsOfHealth;

	private boolean isSkipPaymentTlf;
	private boolean status;

	@Temporal(TemporalType.TIMESTAMP)
	private Date informDate;

	@Enumerated(EnumType.STRING)
	private CustomerType customerType;

	private String oldMedicalPolicyId;

	private String saleManId;

	@Enumerated(EnumType.STRING)
	private HealthType healthType;

	@Column(name = "RATE")
	private double rate;

	private double totalNcbAmount;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "LIFEPROPOSALID", referencedColumnName = "ID")
	private List<LifeMedicalInsuredPerson> proposalInsuredPersonList;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "MEDICALPROPOSALID", referencedColumnName = "ID")
	private List<LifeMedicalInsuredPerson> medicalProposalInsuredPersonList;
	
	public List<LifeMedicalInsuredPerson> getProposalInsuredPersonList() {
		if (this.proposalInsuredPersonList == null) {
			this.proposalInsuredPersonList = new ArrayList<LifeMedicalInsuredPerson>();
		}
		return this.proposalInsuredPersonList;
	}
	
	public List<LifeMedicalInsuredPerson> getMedicalProposalInsuredPersonList() {
		if (this.medicalProposalInsuredPersonList == null) {
			this.medicalProposalInsuredPersonList = new ArrayList<LifeMedicalInsuredPerson>();
		}
		return this.medicalProposalInsuredPersonList;
	}
	
	public double getProposedPremium() {
		double proposedPremium = 0.0;
		for (LifeMedicalInsuredPerson pi : proposalInsuredPersonList) {
			if (pi.getProposedPremium() > 0) {
				proposedPremium = proposedPremium + pi.getProposedPremium();
			}
		}
		return proposedPremium;
	}

}
