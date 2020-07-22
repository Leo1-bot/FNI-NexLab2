package org.tat.fni.api.domain.services.ProposalServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tat.fni.api.common.Name;
import org.tat.fni.api.common.ResidentAddress;
import org.tat.fni.api.common.emumdata.IdType;
import org.tat.fni.api.common.emumdata.ProposalType;
import org.tat.fni.api.domain.MedicalProposalInsuredPersonAddOn;
import org.tat.fni.api.domain.Township;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalCustomer;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalInsuredPerson;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalInsuredPersonBeneficiary;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalProposal;
import org.tat.fni.api.domain.proposalTemp.repository.LifeMedicalProposalRepository;
import org.tat.fni.api.domain.services.TownShipService;
import org.tat.fni.api.domain.services.Interfaces.ICustomIdGenerator;
import org.tat.fni.api.domain.services.Interfaces.IMedicalProductsProposalService;
import org.tat.fni.api.domain.services.Interfaces.IMedicalProposalService;
import org.tat.fni.api.dto.InsuredPersonAddOnDTO;
import org.tat.fni.api.dto.customerDTO.CustomerDto;
import org.tat.fni.api.dto.customerDTO.ResidentAddressDto;
import org.tat.fni.api.dto.microHealthDTO.MicroHealthDTO;
import org.tat.fni.api.dto.microHealthDTO.MicroHealthProposalInsuredPersonBeneficiariesDTO;
import org.tat.fni.api.dto.microHealthDTO.MicroHealthProposalInsuredPersonDTO;
import org.tat.fni.api.dto.retrieveDTO.NameDto;
import org.tat.fni.api.exception.DAOException;
import org.tat.fni.api.exception.SystemException;

@Service
public class MicroHealthProposalService implements IMedicalProductsProposalService {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LifeMedicalProposalRepository lifeMedicalProposalRepo;

	@Autowired
	private TownShipService townShipService;

	@Autowired
	private IMedicalProposalService medicalProposalService;

	@Autowired
	private ICustomIdGenerator customIdRepo;

	@Value("${microHealthProductId}")
	private String microHealthProductId;

	@Value("${branchId}")
	private String branchId;

	@Value("${salespointId}")
	private String salespointId;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public <T> List<LifeMedicalProposal> createDtoToProposal(T proposalDto) {
		try {
			MicroHealthDTO microHealthInsuranceDTO = (MicroHealthDTO) proposalDto;

			// convert MicroHealthProposalDTO to lifeproposal
			List<LifeMedicalProposal> microHealthProposalList = convertIndividualProposalDTOToProposal(
					microHealthInsuranceDTO);
			lifeMedicalProposalRepo.saveAll(microHealthProposalList);

//			String id = DateUtils.formattedSqlDate(new Date()).concat(microHealthProposalList.get(0).getProposalNo());
//			String referenceNo = microHealthProposalList.get(0).getId();
//			String referenceType = "MICRO_HEALTH";
//			String createdDate = DateUtils.formattedSqlDate(new Date());
//			String workflowDate = DateUtils.formattedSqlDate(new Date());
//
//			lifeProposalRepo.saveToWorkflow(id, referenceNo, referenceType, createdDate);
//			lifeProposalRepo.saveToWorkflowHistory(id, referenceNo, referenceType, createdDate, workflowDate);

			return microHealthProposalList;
		} catch (Exception e) {
			logger.error("JOEERROR:" + e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public <T> List<LifeMedicalProposal> convertIndividualProposalDTOToProposal(T proposalDto) {

		List<LifeMedicalProposal> medicalProposalList = new ArrayList<>();
		MicroHealthDTO microHealthInsuranceDTO = (MicroHealthDTO) proposalDto;
		try {
			microHealthInsuranceDTO.getMicrohealthproposalInsuredPersonList().forEach(insuredPerson -> {
				
				LifeMedicalProposal medicalProposal = new LifeMedicalProposal();

				LifeMedicalCustomer customer = medicalProposalService
						.checkCustomerAvailabilityTemp(microHealthInsuranceDTO.getCustomer());

				if (customer == null) {
					medicalProposal.setCustomer(medicalProposalService.createNewCustomer(microHealthInsuranceDTO.getCustomer()));
				} else {
					medicalProposal.setCustomer(customer);
				}

//				medicalProposalService.setPeriodMonthForKeyFacterValue(microHealthInsuranceDTO.getPeriodMonth(),
//						microHealthInsuranceDTO.getPaymentTypeId());

				medicalProposal.getMedicalProposalInsuredPersonList()
						.add(createInsuredPerson(insuredPerson, microHealthInsuranceDTO));
				medicalProposal.setComplete(false);
				medicalProposal.setHealthType(microHealthInsuranceDTO.getHealthType());
				medicalProposal.setStatus(false);
				medicalProposal.setProposalType(ProposalType.UNDERWRITING);
				medicalProposal.setSubmittedDate(microHealthInsuranceDTO.getSubmittedDate());
				medicalProposal.setAgentId(microHealthInsuranceDTO.getAgentId());
				medicalProposal.setPaymentTypeId(microHealthInsuranceDTO.getPaymentTypeId());
				medicalProposal.setBranchId(branchId);
				medicalProposal.setSalesPointsId(salespointId);

				String proposalNo = customIdRepo.getNextId("HEALTH_PROPOSAL_NO", null);
				medicalProposal.setStartDate(microHealthInsuranceDTO.getStartDate());
				medicalProposal.setEndDate(microHealthInsuranceDTO.getEndDate());
				medicalProposal.setSaleChannelType(microHealthInsuranceDTO.getSaleChannelType());
				medicalProposal.setPeriodMonth(microHealthInsuranceDTO.getPeriodMonth());
				medicalProposal.setProposalNo(proposalNo);

//				medicalProposal = medicalProposalService.calculatePremium(medicalProposal);
//				medicalProposalService.calculateTermPremium(medicalProposal);

				medicalProposalList.add(medicalProposal);
			});
		} catch (DAOException e) {
			throw new SystemException(e.getErrorCode(), e.getMessage());
		}
		return medicalProposalList;
	}

	@Override
	public <T> List<LifeMedicalProposal> convertGroupProposalDTOToProposal(T proposalDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> LifeMedicalInsuredPerson createInsuredPerson(T proposalInsuredPersonDTO, T proposalDto) {

		try {
			MicroHealthProposalInsuredPersonDTO dto = (MicroHealthProposalInsuredPersonDTO) proposalInsuredPersonDTO;

			LifeMedicalInsuredPerson insuredPerson = new LifeMedicalInsuredPerson();
			insuredPerson.setAge(dto.getAge());
			insuredPerson.setProductId(microHealthProductId);
			insuredPerson.setUnit(dto.getUnit());
			insuredPerson.setNeedMedicalCheckup(dto.isNeedMedicalCheckup());
			insuredPerson.setGuardianId(dto.getGuardianId());
			insuredPerson.setProposedPremium(dto.getProposedPremium());
			insuredPerson.setProposedSumInsured(dto.getProposedSumInsured());
			insuredPerson.setRelationshipId(dto.getRelationshipId());
			

			String insPersonCodeNo = customIdRepo.getNextId("HEALTH_INSUPERSON_CODE_NO", null);
			insuredPerson.setInsPersonCodeNo(insPersonCodeNo);

			CustomerDto customerDto = getCustomerFromInsuredPerson(dto);

			LifeMedicalCustomer customer = medicalProposalService.checkCustomerAvailabilityTemp(customerDto);

			if (customer == null) {
				insuredPerson.setCustomer(medicalProposalService.createNewCustomer(customerDto));
			} else {
				insuredPerson.setCustomer(customer);
			}

//			insuredPerson.getProduct().getKeyFactorList().forEach(keyfactor -> {
//				insuredPerson.getKeyFactorValueList()
//						.add(medicalProposalService.createKeyFactorValue(keyfactor, insuredPerson, dto));
//			});
			dto.getInsuredPersonBeneficiariesList().forEach(beneficiary -> {
				insuredPerson.getInsuredPersonBeneficiariesList().add(createInsuredPersonBeneficiareis(beneficiary));
			});

			return insuredPerson;
		} catch (DAOException e) {
			throw new SystemException(e.getErrorCode(), e.getMessage());
		}
	}

	@Override
	public <T> LifeMedicalInsuredPersonBeneficiary createInsuredPersonBeneficiareis(
			T insuredPersonBeneficiariesDto) {
		try {
			MicroHealthProposalInsuredPersonBeneficiariesDTO dto = (MicroHealthProposalInsuredPersonBeneficiariesDTO) insuredPersonBeneficiariesDto;

			Optional<Township> townshipOptional = townShipService.findById(dto.getTownshipId());
			
			ResidentAddress residentAddress = new ResidentAddress();
			residentAddress.setResidentAddress(dto.getResidentAddress());
			residentAddress.setTownship(townshipOptional.get());
			
			Name name = new Name();
			name.setFirstName(dto.getFirstName());
			name.setMiddleName(dto.getMiddleName());
			name.setLastName(dto.getLastName());

			LifeMedicalInsuredPersonBeneficiary beneficiary = new LifeMedicalInsuredPersonBeneficiary();
			beneficiary.setInitialId(dto.getInitialId());
			beneficiary.setPercentage(dto.getPercentage());
			beneficiary.setIdType(IdType.valueOf(dto.getIdType()));
			beneficiary.setIdNo(dto.getIdNo());
			beneficiary.setResidentAddress(residentAddress);
			beneficiary.setName(name);
			beneficiary.setRelationshipId(dto.getRelationshipId());
			
			String beneficiaryNo = customIdRepo.getNextId("HEALTH_BENEFICIARY_NO", null);
			beneficiary.setBeneficiaryNo(beneficiaryNo);
			
			return beneficiary;
			
		} catch (DAOException e) {
			throw new SystemException(e.getErrorCode(), e.getMessage());
		}
	}

	@Override
	public MedicalProposalInsuredPersonAddOn createInsuredPersonAddon(InsuredPersonAddOnDTO addOnDTO,
			LifeMedicalInsuredPerson insuredPerson) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> CustomerDto getCustomerFromInsuredPerson(T proposalInsuredPersonDTO) {

		MicroHealthProposalInsuredPersonDTO dto = (MicroHealthProposalInsuredPersonDTO) proposalInsuredPersonDTO;

		NameDto name = NameDto.builder().firstName(dto.getFirstName()).middleName(dto.getMiddleName())
				.lastName(dto.getLastName()).build();

		ResidentAddressDto residentAddress = new ResidentAddressDto();
		residentAddress.setResidentAddress(dto.getResidentAddress());
		residentAddress.setTownshipId(dto.getTownshipId());

		CustomerDto customer = new CustomerDto();
		customer.setInitialId(dto.getInitialId());
		customer.setName(name);
		customer.setFatherName(dto.getFatherName());
		customer.setDateOfBirth(dto.getDateOfBirth());
		customer.setIdNo(dto.getIdNo());
		customer.setIdType(dto.getIdType());
		customer.setResidentAddress(residentAddress);
		customer.setOccupationId(dto.getOccupationID());
		customer.setGender(dto.getGender());

		return customer;
	}

}
