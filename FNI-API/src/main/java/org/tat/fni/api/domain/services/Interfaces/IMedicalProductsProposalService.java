package org.tat.fni.api.domain.services.Interfaces;

import java.util.List;

import org.tat.fni.api.domain.proposalTemp.LifeMedicalInsuredPerson;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalInsuredPersonBeneficiary;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalProposal;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalProposalInsuredPersonAddOn;
import org.tat.fni.api.dto.InsuredPersonAddOnDTO;
import org.tat.fni.api.dto.customerDTO.CustomerDto;
import org.tat.fni.api.dto.customerDTO.GuardianDto;

public interface IMedicalProductsProposalService {

	public <T> List<LifeMedicalProposal> createDtoToProposal(T proposalDto);

	public <T> List<LifeMedicalProposal> convertIndividualProposalDTOToProposal(T proposalDto);

	public <T> List<LifeMedicalProposal> convertGroupProposalDTOToProposal(T proposalDto);

	public <T> LifeMedicalInsuredPerson createInsuredPerson(T proposalInsuredPersonDTO, T proposalDto);

	public <T> LifeMedicalInsuredPersonBeneficiary createInsuredPersonBeneficiareis(
			T insuredPersonBeneficiariesDto, LifeMedicalInsuredPerson insuredPerson);

	public LifeMedicalProposalInsuredPersonAddOn createInsuredPersonAddon(InsuredPersonAddOnDTO addOnDTO,
			LifeMedicalInsuredPerson insuredPerson);
	
	public <T> CustomerDto getCustomerFromInsuredPerson(T proposalInsuredPersonDTO);

}
