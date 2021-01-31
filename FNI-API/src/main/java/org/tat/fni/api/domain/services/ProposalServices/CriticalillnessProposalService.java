package org.tat.fni.api.domain.services.ProposalServices;

import java.util.ArrayList;
import java.util.Calendar;
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
import org.tat.fni.api.domain.CustomerType;
import org.tat.fni.api.domain.HealthType;
import org.tat.fni.api.domain.Township;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalCustomer;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalInsuredPerson;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalInsuredPersonBeneficiary;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalProposal;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalProposalInsuredPersonAddOn;
import org.tat.fni.api.domain.proposalTemp.repository.LifeMedicalProposalRepository;
import org.tat.fni.api.domain.services.TownShipService;
import org.tat.fni.api.domain.services.Interfaces.ICustomIdGenerator;
import org.tat.fni.api.domain.services.Interfaces.IMedicalProductsProposalService;
import org.tat.fni.api.domain.services.Interfaces.IMedicalProposalService;
import org.tat.fni.api.dto.InsuredPersonAddOnDTO;
import org.tat.fni.api.dto.criticalIllnessDTO.CriticalillnessProposalInsuredPersonBeneficiariesDTO;
import org.tat.fni.api.dto.criticalIllnessDTO.CriticalillnessProposalInsuredPersonDTO;
import org.tat.fni.api.dto.criticalIllnessDTO.GroupCriticalIllnessDTO;
import org.tat.fni.api.dto.criticalIllnessDTO.IndividualCriticalIllnessDTO;
import org.tat.fni.api.dto.customerDTO.CustomerDto;
import org.tat.fni.api.dto.customerDTO.GuardianDto;
import org.tat.fni.api.dto.customerDTO.ResidentAddressDto;
import org.tat.fni.api.dto.retrieveDTO.NameDto;
import org.tat.fni.api.exception.DAOException;
import org.tat.fni.api.exception.SystemException;

@Service
public class CriticalillnessProposalService implements IMedicalProductsProposalService {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LifeMedicalProposalRepository lifeMedicalProposalRepo;

	@Autowired
	private TownShipService townShipService;

	@Autowired
	private IMedicalProposalService medicalProposalService;

	@Autowired
	private ICustomIdGenerator customIdRepo;

	@Value("${individualCriticalillnessProductID}")
	private String individualCriticalillnessProductID;

	@Value("${groupCriticalillnessProductID}")
	private String groupCriticalillnessProductID;

	@Value("${branchId}")
	private String branchId;

	@Value("${salespointId}")
	private String salespointId;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public <T> List<LifeMedicalProposal> createDtoToProposal(T proposalDto) {

		try {
			// convert CriticalProposalDTO to lifeproposal
			List<LifeMedicalProposal> criticalillnessProposalList = proposalDto instanceof IndividualCriticalIllnessDTO
					? convertIndividualProposalDTOToProposal((IndividualCriticalIllnessDTO) proposalDto)
					: convertGroupProposalDTOToProposal((GroupCriticalIllnessDTO) proposalDto);

			lifeMedicalProposalRepo.saveAll(criticalillnessProposalList);

			return criticalillnessProposalList;

		} catch (Exception e) {
			logger.error("JOEERROR:" + e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public <T> List<LifeMedicalProposal> convertIndividualProposalDTOToProposal(T proposalDto) {

		List<LifeMedicalProposal> medicalProposalList = new ArrayList<>();
		IndividualCriticalIllnessDTO criticalIllnessDTO = (IndividualCriticalIllnessDTO) proposalDto;
		LifeMedicalProposal medicalProposal = new LifeMedicalProposal();

		try {

				LifeMedicalCustomer customer = medicalProposalService
						.checkCustomerAvailabilityTemp(criticalIllnessDTO.getCustomer());

				if (customer == null) {
					medicalProposal
							.setCustomer(medicalProposalService.createNewCustomer(criticalIllnessDTO.getCustomer()));
				} else {
					medicalProposal.setCustomer(customer);
				}

				criticalIllnessDTO.getProposalInsuredPersonList().forEach(insuredPerson -> {
					medicalProposal.getMedicalProposalInsuredPersonList()
							.add(createInsuredPerson(insuredPerson, criticalIllnessDTO));
				});
				
				medicalProposal.setComplete(false);
				medicalProposal.setHealthType(HealthType.CRITICALILLNESS);
				medicalProposal.setCustomerType(CustomerType.INDIVIDUALCUSTOMER);
				medicalProposal.setStatus(false);
				medicalProposal.setPeriodMonth(criticalIllnessDTO.getPeriodMonth() / 12);
				medicalProposal.setProposalType(ProposalType.UNDERWRITING);
				medicalProposal.setSubmittedDate(criticalIllnessDTO.getSubmittedDate());
				medicalProposal.setAgentId(criticalIllnessDTO.getAgentId());
				medicalProposal.setPaymentTypeId(criticalIllnessDTO.getPaymentTypeId());
				medicalProposal.setBranchId(branchId);
				medicalProposal.setSalesPointsId(salespointId);

				String proposalNo = customIdRepo.getNextId("HEALTH_PROPOSAL_NO", null);
				medicalProposal.setPeriodMonth(criticalIllnessDTO.getPeriodMonth() / 12);
				medicalProposal.setStartDate(criticalIllnessDTO.getStartDate());
				
				Calendar cal = Calendar.getInstance();
				cal.setTime(medicalProposal.getStartDate());
				cal.add(Calendar.YEAR, medicalProposal.getPeriodMonth());
				
				medicalProposal.setEndDate(cal.getTime());
				medicalProposal.setSaleChannelType(null);
				medicalProposal.setProposalNo(proposalNo);

				medicalProposalList.add(medicalProposal);
			
		} catch (DAOException e) {
			throw new SystemException(e.getErrorCode(), e.getMessage());
		}
		return medicalProposalList;
	}

	@Override
	public <T> List<LifeMedicalProposal> convertGroupProposalDTOToProposal(T proposalDto) {

		List<LifeMedicalProposal> medicalProposalList = new ArrayList<>();
		GroupCriticalIllnessDTO criticalIllnessDTO = (GroupCriticalIllnessDTO) proposalDto;
		LifeMedicalProposal medicalProposal = new LifeMedicalProposal();

		try {
			
				LifeMedicalCustomer customer = medicalProposalService
						.checkCustomerAvailabilityTemp(criticalIllnessDTO.getCustomer());

				if (customer == null) {
					medicalProposal
							.setCustomer(medicalProposalService.createNewCustomer(criticalIllnessDTO.getCustomer()));
				} else {
					medicalProposal.setCustomer(customer);
				}
				
				criticalIllnessDTO.getProposalInsuredPersonList().forEach(insuredPerson -> {
					medicalProposal.getMedicalProposalInsuredPersonList()
							.add(createInsuredPerson(insuredPerson, criticalIllnessDTO));
				});
				
				medicalProposal.setComplete(false);
				medicalProposal.setHealthType(criticalIllnessDTO.getHealthType());
				medicalProposal.setCustomerType(criticalIllnessDTO.getCustomerType());
				medicalProposal.setPeriodMonth(criticalIllnessDTO.getPeriodMonth());
				medicalProposal.setProposalType(ProposalType.UNDERWRITING);
				medicalProposal.setSubmittedDate(criticalIllnessDTO.getSubmittedDate());
				medicalProposal.setAgentId(criticalIllnessDTO.getAgentId());
				medicalProposal.setPaymentTypeId(criticalIllnessDTO.getPaymentTypeId());
				medicalProposal.setBranchId(branchId);
				medicalProposal.setSalesPointsId(salespointId);
				medicalProposal.setOrganizationId(criticalIllnessDTO.getOrganizationId());

				String proposalNo = customIdRepo.getNextId("HEALTH_PROPOSAL_NO", null);
				medicalProposal.setPeriodMonth(criticalIllnessDTO.getPeriodMonth() / 12);
				medicalProposal.setStartDate(criticalIllnessDTO.getStartDate());
				
				Calendar cal = Calendar.getInstance();
				cal.setTime(medicalProposal.getStartDate());
				cal.add(Calendar.YEAR, medicalProposal.getPeriodMonth());
				
				medicalProposal.setEndDate(cal.getTime());
				medicalProposal.setSaleChannelType(criticalIllnessDTO.getSaleChannelType());
				
				medicalProposal.setProposalNo(proposalNo);

				medicalProposalList.add(medicalProposal);
			
		} catch (DAOException e) {
			throw new SystemException(e.getErrorCode(), e.getMessage());
		}
		return medicalProposalList;
	}

	@Override
	public <T> LifeMedicalInsuredPerson createInsuredPerson(T proposalInsuredPersonDTO, T proposalDto) {

		try {
			CriticalillnessProposalInsuredPersonDTO dto = (CriticalillnessProposalInsuredPersonDTO) proposalInsuredPersonDTO;

			LifeMedicalInsuredPerson insuredPerson = new LifeMedicalInsuredPerson();

			insuredPerson.setAge(dto.getAge());
			insuredPerson.setProductId(
					proposalDto instanceof IndividualCriticalIllnessDTO ? individualCriticalillnessProductID
							: groupCriticalillnessProductID);
			insuredPerson.setUnit(dto.getUnit());
			insuredPerson.setNeedMedicalCheckup(false);
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
			CriticalillnessProposalInsuredPersonBeneficiariesDTO dto = (CriticalillnessProposalInsuredPersonBeneficiariesDTO) insuredPersonBeneficiariesDto;

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
			beneficiary.setPhone(dto.getPhone());

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> CustomerDto getCustomerFromInsuredPerson(T proposalInsuredPersonDTO) {

		CriticalillnessProposalInsuredPersonDTO dto = (CriticalillnessProposalInsuredPersonDTO) proposalInsuredPersonDTO;

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
//		customer.setOccupationId(dto.getOccupationID());
		customer.setGender(dto.getGender());

		return customer;
	}

}
