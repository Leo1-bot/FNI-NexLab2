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
import org.tat.fni.api.common.interfaces.ISorter;
import org.tat.fni.api.domain.addon.AddOn;

import lombok.Data;

@Entity
@Table(name = TableName.PROPOSAL_LIFE_MEDICAL_INSUREDPERSON_ADDON_TEMP)
@TableGenerator(name = "MEDINSUREDPERSONADDON_GEN", table = "ID_GEN", pkColumnName = "GEN_NAME", valueColumnName = "GEN_VAL", pkColumnValue = "MEDINSUREDPERSONADDON_GEN", allocationSize = 10)
@EntityListeners(IDInterceptor.class)
@Data
public class LifeMedicalProposalInsuredPersonAddOn implements ISorter {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "MEDINSUREDPERSONADDON_GEN")
	private String id;
	
	private double premium;
	private int unit;
	
	@Version
	private int version;
	
	@Embedded
	private UserRecorder recorder;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MEDICALPRODUCTADDONID", referencedColumnName = "ID")
	private AddOn addOn;
	
	private double sumInsured;

	@Override
	public String getRegistrationNo() {
		if (addOn != null)
			return addOn.getName();
		return "";
	}

}
