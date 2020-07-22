package org.tat.fni.api.domain.services.Interfaces;

import java.util.List;

import org.tat.fni.api.domain.proposalTemp.LifeMedicalInsuredPerson;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalInsuredPersonBeneficiary;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalProposal;

public interface ILifeProductsProposalService {

	public <T> List<LifeMedicalProposal> createDtoToProposal(T proposalDto);

	public <T> List<LifeMedicalProposal> convertProposalDTOToProposal(T proposalDto);

	public <T> LifeMedicalInsuredPerson createInsuredPerson(T proposalInsuredPersonDTO);

	public <T> LifeMedicalInsuredPersonBeneficiary createInsuredPersonBeneficiareis(T insuredPersonBeneficiariesDto,
			LifeMedicalInsuredPerson insuredPerson);

}
