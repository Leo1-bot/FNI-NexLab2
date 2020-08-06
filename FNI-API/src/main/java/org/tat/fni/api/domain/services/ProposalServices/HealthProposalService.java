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
import org.tat.fni.api.domain.Township;
import org.tat.fni.api.domain.addon.AddOn;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalCustomer;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalInsuredPerson;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalInsuredPersonBeneficiary;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalProposal;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalProposalInsuredPersonAddOn;
import org.tat.fni.api.domain.proposalTemp.repository.LifeMedicalProposalRepository;
import org.tat.fni.api.domain.services.AddOnService;
import org.tat.fni.api.domain.services.TownShipService;
import org.tat.fni.api.domain.services.Interfaces.ICustomIdGenerator;
import org.tat.fni.api.domain.services.Interfaces.IMedicalProductsProposalService;
import org.tat.fni.api.domain.services.Interfaces.IMedicalProposalService;
import org.tat.fni.api.dto.InsuredPersonAddOnDTO;
import org.tat.fni.api.dto.customerDTO.CustomerDto;
import org.tat.fni.api.dto.customerDTO.ResidentAddressDto;
import org.tat.fni.api.dto.healthInsuranceDTO.GroupHealthInsuranceDTO;
import org.tat.fni.api.dto.healthInsuranceDTO.HealthProposalInsuredPersonBeneficiariesDTO;
import org.tat.fni.api.dto.healthInsuranceDTO.HealthProposalInsuredPersonDTO;
import org.tat.fni.api.dto.healthInsuranceDTO.IndividualHealthInsuranceDTO;
import org.tat.fni.api.dto.retrieveDTO.NameDto;
import org.tat.fni.api.exception.DAOException;
import org.tat.fni.api.exception.SystemException;

@Service
public class HealthProposalService implements IMedicalProductsProposalService {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LifeMedicalProposalRepository lifeMedicalProposalRepo;

	@Autowired
	private TownShipService townShipService;

	@Autowired
	private AddOnService addOnService;

	@Autowired
	private IMedicalProposalService medicalProposalService;

	@Autowired
	private ICustomIdGenerator customIdRepo;

	@Value("${individualHealthProductId}")
	private String individualHealthProductId;

	@Value("${groupHealthProductId}")
	private String groupHealthProductId;

	@Value("${branchId}")
	private String branchId;

	@Value("${salespointId}")
	private String salespointId;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public <T> List<LifeMedicalProposal> createDtoToProposal(T proposalDto) {
		try {
			// convert IndividualHealthProposalDTO to lifeproposal
			List<LifeMedicalProposal> healthProposalList = proposalDto instanceof IndividualHealthInsuranceDTO
					? convertIndividualProposalDTOToProposal((IndividualHealthInsuranceDTO) proposalDto)
					: convertGroupProposalDTOToProposal((GroupHealthInsuranceDTO) proposalDto);

			lifeMedicalProposalRepo.saveAll(healthProposalList);

//			String id = DateUtils.formattedSqlDate(new Date()).concat(healthProposalList.get(0).getProposalNo());
//			String referenceNo = healthProposalList.get(0).getId();
//			String referenceType = "HEALTH";
//			String createdDate = DateUtils.formattedSqlDate(new Date());
//			String workflowDate = DateUtils.formattedSqlDate(new Date());
//
//			lifeProposalRepo.saveToWorkflow(id, referenceNo, referenceType, createdDate);
//			lifeProposalRepo.saveToWorkflowHistory(id, referenceNo, referenceType, createdDate, workflowDate);

			return healthProposalList;

		} catch (Exception e) {
			logger.error("JOEERROR:" + e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public <T> List<LifeMedicalProposal> convertIndividualProposalDTOToProposal(T proposalDto) {

		List<LifeMedicalProposal> medicalProposalList = new ArrayList<>();
		IndividualHealthInsuranceDTO individualHealthInsuranceDTO = (IndividualHealthInsuranceDTO) proposalDto;

		try {
			individualHealthInsuranceDTO.getProposalInsuredPersonList().forEach(insuredPerson -> {
				LifeMedicalProposal medicalProposal = new LifeMedicalProposal();

				LifeMedicalCustomer customer = medicalProposalService
						.checkCustomerAvailabilityTemp(individualHealthInsuranceDTO.getCustomer());

				if (customer == null) {
					medicalProposal.setCustomer(
							medicalProposalService.createNewCustomer(individualHealthInsuranceDTO.getCustomer()));
				} else {
					medicalProposal.setCustomer(customer);
				}

//				medicalProposalService.setPeriodMonthForKeyFacterValue(individualHealthInsuranceDTO.getPeriodMonth(),
//						individualHealthInsuranceDTO.getPaymentTypeId());

				medicalProposal.getMedicalProposalInsuredPersonList()
						.add(createInsuredPerson(insuredPerson, individualHealthInsuranceDTO));
				medicalProposal.setComplete(false);
				medicalProposal.setHealthType(individualHealthInsuranceDTO.getHealthType());
				medicalProposal.setCustomerType(individualHealthInsuranceDTO.getCustomerType());
				medicalProposal.setStatus(false);
				medicalProposal.setProposalType(ProposalType.UNDERWRITING);
				medicalProposal.setSubmittedDate(individualHealthInsuranceDTO.getSubmittedDate());
				medicalProposal.setAgentId(individualHealthInsuranceDTO.getAgentId());
				medicalProposal.setPaymentTypeId(individualHealthInsuranceDTO.getPaymentTypeId());
				medicalProposal.setBranchId(branchId);
				medicalProposal.setSalesPointsId(salespointId);

				String proposalNo = customIdRepo.getNextId("HEALTH_PROPOSAL_NO", null);
				medicalProposal.setStartDate(individualHealthInsuranceDTO.getStartDate());
				medicalProposal.setEndDate(individualHealthInsuranceDTO.getEndDate());
				medicalProposal.setSaleChannelType(individualHealthInsuranceDTO.getSaleChannelType());
				medicalProposal.setPeriodMonth(individualHealthInsuranceDTO.getPeriodMonth());
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

		List<LifeMedicalProposal> medicalProposalList = new ArrayList<>();
		GroupHealthInsuranceDTO groupHealthInsuranceDTO = (GroupHealthInsuranceDTO) proposalDto;

		try {
			groupHealthInsuranceDTO.getProposalInsuredPersonList().forEach(insuredPerson -> {
				LifeMedicalProposal medicalProposal = new LifeMedicalProposal();

				LifeMedicalCustomer customer = medicalProposalService
						.checkCustomerAvailabilityTemp(groupHealthInsuranceDTO.getCustomer());

				if (customer == null) {
					medicalProposal.setCustomer(
							medicalProposalService.createNewCustomer(groupHealthInsuranceDTO.getCustomer()));
				} else {
					medicalProposal.setCustomer(customer);
				}

//				medicalProposalService.setPeriodMonthForKeyFacterValue(individualHealthInsuranceDTO.getPeriodMonth(),
//						individualHealthInsuranceDTO.getPaymentTypeId());

				medicalProposal.getMedicalProposalInsuredPersonList()
						.add(createInsuredPerson(insuredPerson, groupHealthInsuranceDTO));
				medicalProposal.setComplete(false);
				medicalProposal.setHealthType(groupHealthInsuranceDTO.getHealthType());
				medicalProposal.setCustomerType(groupHealthInsuranceDTO.getCustomerType());
				medicalProposal.setProposalType(ProposalType.UNDERWRITING);
				medicalProposal.setSubmittedDate(groupHealthInsuranceDTO.getSubmittedDate());
				medicalProposal.setAgentId(groupHealthInsuranceDTO.getAgentId());
				medicalProposal.setPaymentTypeId(groupHealthInsuranceDTO.getPaymentTypeId());
				medicalProposal.setBranchId(branchId);
				medicalProposal.setSalesPointsId(salespointId);
				medicalProposal.setOrganizationId(groupHealthInsuranceDTO.getOrganizationId());

				String proposalNo = customIdRepo.getNextId("HEALTH_PROPOSAL_NO", null);
				medicalProposal.setStartDate(groupHealthInsuranceDTO.getStartDate());
				medicalProposal.setEndDate(groupHealthInsuranceDTO.getEndDate());
				medicalProposal.setSaleChannelType(groupHealthInsuranceDTO.getSaleChannelType());
				medicalProposal.setPeriodMonth(groupHealthInsuranceDTO.getPeriodMonth());
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
	public <T> LifeMedicalInsuredPerson createInsuredPerson(T proposalInsuredPersonDTO, T proposalDto) {

		HealthProposalInsuredPersonDTO dto = (HealthProposalInsuredPersonDTO) proposalInsuredPersonDTO;
		try {
			LifeMedicalInsuredPerson insuredPerson = new LifeMedicalInsuredPerson();

			insuredPerson.setAge(dto.getAge());
			insuredPerson.setProductId(proposalDto instanceof IndividualHealthInsuranceDTO ? individualHealthProductId
					: groupHealthProductId);
			insuredPerson.setUnit(dto.getUnit());
			insuredPerson.setNeedMedicalCheckup(dto.isNeedMedicalCheckup());
			insuredPerson.setRelationshipId(dto.getRelationshipId());
			insuredPerson.setProposedPremium(dto.getProposedPremium());
			insuredPerson.setProposedSumInsured(dto.getProposedSumInsured());
			
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
			
			dto.getInsuredPersonAddonOnList().forEach(addon -> {
				insuredPerson.getInsuredPersonAddOnList().add(createInsuredPersonAddon(addon, insuredPerson));
			});

			dto.getInsuredPersonBeneficiariesList().forEach(beneficiary -> {
				insuredPerson.getInsuredPersonBeneficiariesList().add(createInsuredPersonBeneficiareis(beneficiary, insuredPerson));
			});

			return insuredPerson;

		} catch (DAOException e) {
			throw new SystemException(e.getErrorCode(), e.getMessage());
		}
	}

	@Override
	public <T> LifeMedicalInsuredPersonBeneficiary createInsuredPersonBeneficiareis(
			T insuredPersonBeneficiariesDto, LifeMedicalInsuredPerson insuredPerson) {
		try {
			HealthProposalInsuredPersonBeneficiariesDTO dto = (HealthProposalInsuredPersonBeneficiariesDTO) insuredPersonBeneficiariesDto;

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
			beneficiary.setDateOfBirth(dto.getDateOfBirth());
			beneficiary.setPercentage(dto.getPercentage());
			beneficiary.setFatherName(dto.getFatherName());
			beneficiary.setIdType(IdType.valueOf(dto.getIdType()));
			beneficiary.setIdNo(dto.getIdNo());
			beneficiary.setResidentAddress(residentAddress);
			beneficiary.setName(name);
			beneficiary.setRelationshipId(dto.getRelationshipId());
			beneficiary.setProposalInsuredPerson(insuredPerson);

			String beneficiaryNo = customIdRepo.getNextId("HEALTH_BENEFICIARY_NO", null);
			beneficiary.setBeneficiaryNo(beneficiaryNo);
			
			return beneficiary;
			
		} catch (DAOException e) {
			throw new SystemException(e.getErrorCode(), e.getMessage());
		}
	}

	@Override
	public LifeMedicalProposalInsuredPersonAddOn createInsuredPersonAddon(InsuredPersonAddOnDTO addOnDTO,
			LifeMedicalInsuredPerson insuredPerson) {

		try {

			AddOn addOn = addOnService.findAddOnById(addOnDTO.getMedicalProductAddOnId());

			LifeMedicalProposalInsuredPersonAddOn addon = new LifeMedicalProposalInsuredPersonAddOn();
			addon.setUnit(addOnDTO.getUnit());
			addon.setSumInsured(insuredPerson.getSumInsured());
			addon.setPremium(addOnDTO.getPremium());
			addon.setAddOn(addOn);

			return addon;

		} catch (DAOException e) {
			throw new SystemException(e.getErrorCode(), e.getMessage());
		}

	}

	@Override
	public <T> CustomerDto getCustomerFromInsuredPerson(T proposalInsuredPersonDTO) {

		HealthProposalInsuredPersonDTO dto = (HealthProposalInsuredPersonDTO) proposalInsuredPersonDTO;

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
