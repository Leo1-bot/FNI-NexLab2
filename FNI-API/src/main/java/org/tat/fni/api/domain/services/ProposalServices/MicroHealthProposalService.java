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
import org.tat.fni.api.domain.HealthType;
import org.tat.fni.api.domain.Township;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalCustomer;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalGuardian;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalInsuredPerson;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalInsuredPersonBeneficiary;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalProposal;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalProposalInsuredPersonAddOn;
import org.tat.fni.api.domain.proposalTemp.repository.LifeMedicalGuardianRepository;
import org.tat.fni.api.domain.proposalTemp.repository.LifeMedicalProposalRepository;
import org.tat.fni.api.domain.repository.GuardianRepository;
import org.tat.fni.api.domain.services.TownShipService;
import org.tat.fni.api.domain.services.Interfaces.ICustomIdGenerator;
import org.tat.fni.api.domain.services.Interfaces.IMedicalProductsProposalService;
import org.tat.fni.api.domain.services.Interfaces.IMedicalProposalService;
import org.tat.fni.api.dto.InsuredPersonAddOnDTO;
import org.tat.fni.api.dto.customerDTO.CustomerDto;
import org.tat.fni.api.dto.customerDTO.GuardianDto;
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
	private LifeMedicalGuardianRepository guardianTempRepo;

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

				medicalProposal.getMedicalProposalInsuredPersonList()
						.add(createInsuredPerson(insuredPerson, microHealthInsuranceDTO));
				medicalProposal.setComplete(false);
				medicalProposal.setHealthType(HealthType.MICROHEALTH);
				medicalProposal.setStatus(false);
				medicalProposal.setProposalType(ProposalType.UNDERWRITING);
				medicalProposal.setSubmittedDate(microHealthInsuranceDTO.getSubmittedDate());
				medicalProposal.setAgentId(microHealthInsuranceDTO.getAgentId());
				medicalProposal.setPaymentTypeId(microHealthInsuranceDTO.getPaymentTypeId());
				medicalProposal.setBranchId(branchId);
				medicalProposal.setSalesPointsId(salespointId);

				String proposalNo = customIdRepo.getNextId("HEALTH_PROPOSAL_NO", null);
				medicalProposal.setPeriodMonth(microHealthInsuranceDTO.getPeriodMonth() / 12);
				medicalProposal.setStartDate(microHealthInsuranceDTO.getStartDate());
				
				Calendar cal = Calendar.getInstance();
				cal.setTime(medicalProposal.getStartDate());
				cal.add(Calendar.YEAR, medicalProposal.getPeriodMonth());
				
				medicalProposal.setEndDate(cal.getTime());
				medicalProposal.setSaleChannelType(null);
				medicalProposal.setPeriodMonth(microHealthInsuranceDTO.getPeriodMonth() / 12);
				medicalProposal.setProposalNo(proposalNo);

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
			insuredPerson.setNeedMedicalCheckup(false);
			insuredPerson.setRelationshipId(dto.getRelationshipId());
			
			// setting guardian
			if (insuredPerson.getAge() > 18) {
				insuredPerson.setGuardian(null);
			} else {
				LifeMedicalGuardian guardian = new LifeMedicalGuardian();
				
				GuardianDto guardianDto = dto.getGuardian();
				CustomerDto customerDto = new CustomerDto(guardianDto);
				
				LifeMedicalCustomer customer = medicalProposalService.checkCustomerAvailabilityTemp(customerDto);

				if (customer == null) {
					guardian.setCustomer(medicalProposalService.createNewCustomer(customerDto));
				} else {
					guardian.setCustomer(customer);
				}
				guardian.setRelationshipId(insuredPerson.getRelationshipId());
				
				String guardianNo = customIdRepo.getNextId("GUARDIAN_NO", null);
				guardian.setGuardianNo(guardianNo);
				
				guardianTempRepo.save(guardian);
				
				insuredPerson.setGuardian(guardian);
			}

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
			beneficiary.setFatherName(dto.getFatherName());
			beneficiary.setDateOfBirth(dto.getDateOfBirth());
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
//		customer.setOccupationId(dto.getOccupationID());
		customer.setGender(dto.getGender());

		return customer;
	}

}
