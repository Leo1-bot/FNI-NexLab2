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
import org.tat.fni.api.common.emumdata.Gender;
import org.tat.fni.api.common.emumdata.IdType;
import org.tat.fni.api.common.emumdata.ProposalType;
import org.tat.fni.api.common.emumdata.SaleChannelType;
import org.tat.fni.api.domain.Township;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalCustomer;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalInsuredPerson;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalInsuredPersonBeneficiary;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalProposal;
import org.tat.fni.api.domain.proposalTemp.repository.LifeMedicalProposalRepository;
import org.tat.fni.api.domain.services.BaseService;
import org.tat.fni.api.domain.services.TownShipService;
import org.tat.fni.api.domain.services.Interfaces.ICustomIdGenerator;
import org.tat.fni.api.domain.services.Interfaces.ILifeProductsProposalService;
import org.tat.fni.api.domain.services.Interfaces.ILifeProposalService;
import org.tat.fni.api.dto.studentLifeDTO.StudentLifeDTO;
import org.tat.fni.api.dto.studentLifeDTO.StudentLifeProposalInsuredPersonDTO;
import org.tat.fni.api.exception.DAOException;
import org.tat.fni.api.exception.SystemException;

@Service
public class StudentLifeProposalService extends BaseService implements ILifeProductsProposalService {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LifeMedicalProposalRepository lifeMedicalProposalRepo;

	@Autowired
	private TownShipService townShipService;

	@Autowired
	private ILifeProposalService lifeProposalService;

	@Value("${studentLifeProductId}")
	private String studentLifeProductId;
	
	@Value("${branchId}")
	private String branchId;

	@Value("${salespointId}")
	private String salespointId;

	@Autowired
	private ICustomIdGenerator customId;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public <T> List<LifeMedicalProposal> createDtoToProposal(T proposalDto) {
		try {

			StudentLifeDTO studentLifeProposalDTO = (StudentLifeDTO) proposalDto;

			List<LifeMedicalProposal> studentLifeProposalList = convertProposalDTOToProposal(studentLifeProposalDTO);
			lifeMedicalProposalRepo.saveAll(studentLifeProposalList);

			return studentLifeProposalList;

		} catch (DAOException e) {

			logger.error("JOEERROR:" + e.getMessage(), e);
			throw new SystemException(e.getErrorCode(), e.getMessage());
		}
	}

	@Override
	public <T> List<LifeMedicalProposal> convertProposalDTOToProposal(T proposalDto) {

		StudentLifeDTO studentLifeProposalDTO = (StudentLifeDTO) proposalDto;

		List<LifeMedicalProposal> lifeProposalList = new ArrayList<>();

		try {
			studentLifeProposalDTO.getProposalInsuredPersonList().forEach(insuredPerson -> {

				LifeMedicalProposal lifeProposal = new LifeMedicalProposal();

				LifeMedicalCustomer customer = lifeProposalService.checkCustomerAvailabilityTemp(studentLifeProposalDTO.getCustomer());

				if (customer == null) {
					lifeProposal.setCustomer(lifeProposalService.createNewCustomer(studentLifeProposalDTO.getCustomer()));
				} else {
					lifeProposal.setCustomer(customer);
				}

				lifeProposal.getProposalInsuredPersonList().add(createInsuredPerson(insuredPerson));

				lifeProposal.setComplete(false);
				lifeProposal.setStatus(false);
				lifeProposal.setProposalType(ProposalType.UNDERWRITING);
				lifeProposal.setSubmittedDate(studentLifeProposalDTO.getSubmittedDate());
				lifeProposal.setPeriodMonth(studentLifeProposalDTO.getPeriodMonth() / 12);
				lifeProposal.setSaleChannelType(SaleChannelType.AGENT);
				lifeProposal.setPaymentTypeId(studentLifeProposalDTO.getPaymentTypeId());
				lifeProposal.setAgentId(studentLifeProposalDTO.getAgentId());
				lifeProposal.setSalesPointsId(salespointId);
				lifeProposal.setBranchId(branchId);

				String proposalNo = customId.getNextId("STUDENT_LIFE_PROPOSAL_NO", null);
				lifeProposal.setStartDate(studentLifeProposalDTO.getStartDate());
				
				Calendar cal = Calendar.getInstance();
				cal.setTime(lifeProposal.getStartDate());
				cal.add(Calendar.YEAR, lifeProposal.getPeriodMonth());
				
				lifeProposal.setEndDate(cal.getTime());
				lifeProposal.setProposalNo(proposalNo);

				lifeProposalList.add(lifeProposal);

			});

		} catch (DAOException e) {
			throw new SystemException(e.getErrorCode(), e.getMessage());
		}

		return lifeProposalList;
	}

	@Override
	public <T> LifeMedicalInsuredPerson createInsuredPerson(T proposalInsuredPersonDTO) {
		try {

			StudentLifeProposalInsuredPersonDTO dto = (StudentLifeProposalInsuredPersonDTO) proposalInsuredPersonDTO;

			Optional<Township> townshipOptional = townShipService.findById(dto.getTownshipId());
			
			ResidentAddress residentAddress = new ResidentAddress();
			residentAddress.setResidentAddress(dto.getResidentAddress());
			residentAddress.setTownship(townshipOptional.get());

			Name name = new Name();
			name.setFirstName(dto.getFirstName());
			name.setMiddleName(dto.getMiddleName());
			name.setLastName(dto.getLastName());

			LifeMedicalInsuredPerson insuredPerson = new LifeMedicalInsuredPerson();
			insuredPerson.setInitialId(dto.getInitialId());
			insuredPerson.setProposedSumInsured(dto.getProposedSumInsured());
			insuredPerson.setIdType(null);
			insuredPerson.setIdNo(null);
			insuredPerson.setFatherName(dto.getFatherName());
			insuredPerson.setDateOfBirth(dto.getDateOfBirth());
			insuredPerson.setPhone(dto.getPhone());
			insuredPerson.setAge(dto.getAge());
			insuredPerson.setGender(Gender.valueOf(dto.getGender()));
			insuredPerson.setResidentAddress(residentAddress);
			insuredPerson.setName(name);
			insuredPerson.setParentName(dto.getParentName());
			insuredPerson.setParentIdType(IdType.valueOf(dto.getParentIdType()));
			insuredPerson.setParentIdNo(dto.getParentIdNo());
			insuredPerson.setParentDOB(dto.getParentDOB());
			insuredPerson.setProductId(studentLifeProductId);
			insuredPerson.setRelationshipId(dto.getRelationshipId());
			insuredPerson.setSchoolId(dto.getSchoolId());
			insuredPerson.setGradeInfoId(dto.getGrateInfoId());

			String insPersonCodeNo = customId.getNextId("LIFE_INSUREDPERSON_CODENO", null);
			insuredPerson.setInsPersonCodeNo(insPersonCodeNo);

			return insuredPerson;
			
		} catch (DAOException e) {
			throw new SystemException(e.getErrorCode(), e.getMessage());
		}
	}

	@Override
	public <T> LifeMedicalInsuredPersonBeneficiary createInsuredPersonBeneficiareis(T insuredPersonBeneficiariesDto,
			LifeMedicalInsuredPerson insuredPerson) {
		// TODO Auto-generated method stub
		return null;
	}

}
