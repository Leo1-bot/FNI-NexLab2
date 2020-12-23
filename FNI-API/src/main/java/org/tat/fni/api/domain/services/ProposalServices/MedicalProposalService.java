package org.tat.fni.api.domain.services.ProposalServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tat.fni.api.common.KeyFactor;
import org.tat.fni.api.common.Name;
import org.tat.fni.api.common.OfficeAddress;
import org.tat.fni.api.common.PermanentAddress;
import org.tat.fni.api.common.ResidentAddress;
import org.tat.fni.api.common.emumdata.ContentInfo;
import org.tat.fni.api.common.utils.Utils;
import org.tat.fni.api.domain.Customer;
import org.tat.fni.api.domain.IPremiumCalculatorService;
import org.tat.fni.api.domain.MedicalKeyFactorValue;
import org.tat.fni.api.domain.MedicalProposal;
import org.tat.fni.api.domain.MedicalProposalInsuredPerson;
import org.tat.fni.api.domain.MedicalProposalInsuredPersonAddOn;
import org.tat.fni.api.domain.PremiumCalData;
import org.tat.fni.api.domain.Township;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalCustomer;
import org.tat.fni.api.domain.proposalTemp.LifeMedicalCustomerFamily;
import org.tat.fni.api.domain.proposalTemp.repository.LifeMedicalCustomerRepository;
import org.tat.fni.api.domain.repository.CustomerRepository;
import org.tat.fni.api.domain.services.TownShipService;
import org.tat.fni.api.domain.services.Interfaces.IMedicalProposalService;
import org.tat.fni.api.dto.customerDTO.CustomerDto;
import org.tat.fni.api.exception.DAOException;
import org.tat.fni.api.exception.SystemException;

@Service
public class MedicalProposalService implements IMedicalProposalService {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource(name = "PremiumCalculatorService")
	private IPremiumCalculatorService premiumCalculatorService;
	
	@Autowired
	private LifeMedicalCustomerRepository customerTempRepository;

	@Autowired
	private TownShipService townshipService;

	@Autowired
	private CustomerRepository customerRepository;

	private int term = 0;
	private String paymentTypeID = "";
	
	private String referenceId = "";

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public <T> MedicalKeyFactorValue createKeyFactorValue(KeyFactor keyfactor,
			MedicalProposalInsuredPerson insuredPerson, T dto) {

		MedicalKeyFactorValue medicalKeyFactorValue = new MedicalKeyFactorValue();
		medicalKeyFactorValue.setKeyFactor(keyfactor);

		keyfactor = medicalKeyFactorValue.getKeyFactor();

		// TODO get values from keyfactor checker properties instead
		if (keyfactor.getValue().equals("Sum Insured")) {
//			medicalKeyFactorValue.setValue(insuredPerson.getApprovedSumInsured() + "");
		} else if (keyfactor.getValue().equals("Age")) {
			medicalKeyFactorValue.setValue(insuredPerson.getAge() + "");
		} else if (keyfactor.getValue().equals("Term")) {
			if (term >= 0) {
				medicalKeyFactorValue.setValue(term + "");
			}
		} else if (keyfactor.getValue().equals("PaymentType")) {
			medicalKeyFactorValue.setValue(paymentTypeID);
		} else if (keyfactor.getValue().equals("RiskyOccupation")) {
//			if(insuredPerson.getRiskyOccupation() == null) {
//				medicalKeyFactorValue.setValue(RiskyOccupation.NO + "");
//			}else {
//				medicalKeyFactorValue.setValue(RiskyOccupation.YES + "");
//			}
		} else if (keyfactor.getValue().equals("Pound")) {
//			medicalKeyFactorValue.setValue(insuredPerson.getWeight() + "");
		} else if (keyfactor.getValue().equals("Dangerous Occupation")) {
//			medicalKeyFactorValue.setValue(
//					insuredPerson.getRiskyOccupation() == null ? 
//							0 + "" : insuredPerson.getRiskyOccupation().getExtraRate() + "");
		}

		return medicalKeyFactorValue;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public MedicalProposal calculatePremium(MedicalProposal medicalProposal) {

		double premium, addOnPremium, riskSIperUnit;
		int paymentTerm = 0;
		int paymentType = medicalProposal.getPaymentType().getMonth();
		int periodOfMonth = medicalProposal.getPeriodMonth();
		/* if not lumpSum payment */
		if (paymentType > 0) {
			paymentTerm = periodOfMonth / paymentType;
			if ((periodOfMonth % paymentType) > 0) {
				paymentTerm = paymentTerm + 1;
			}
		} else {
			/* lumpSum payment */
			paymentTerm = 1;
		}
		medicalProposal.setPaymentTerm(paymentTerm);
		for (MedicalProposalInsuredPerson person : medicalProposal.getMedicalProposalInsuredPersonList()) {
			try {
				Map<KeyFactor, String> keyfatorValueMap = new HashMap<KeyFactor, String>();
				for (MedicalKeyFactorValue keyfactor : person.getKeyFactorValueList()) {
					keyfatorValueMap.put(keyfactor.getKeyFactor(), keyfactor.getValue());
				}
				premium = premiumCalculatorService.calculatePremium(keyfatorValueMap, person.getProduct(),
						new PremiumCalData(null, null, null, (double) person.getUnit()));
				person.setPremium(premium * paymentTerm);

				riskSIperUnit = person.getProduct().getSumInsuredPerUnit();
				person.setSumInsured(person.getUnit() * riskSIperUnit);

				for (MedicalProposalInsuredPersonAddOn insuredPersonAddOn : person.getInsuredPersonAddOnList()) {
					try {
						Map<KeyFactor, String> addOnKeyfatorValueMap = new HashMap<KeyFactor, String>();
						for (MedicalKeyFactorValue kfv : insuredPersonAddOn.getKeyFactorValueList()) {
							addOnKeyfatorValueMap.put(kfv.getKeyFactor(), kfv.getValue());
						}
						addOnPremium = premiumCalculatorService.calculatePremium(addOnKeyfatorValueMap,
								insuredPersonAddOn.getAddOn(), new PremiumCalData(null, null, person.getPremium(),
										(double) insuredPersonAddOn.getUnit()));
						insuredPersonAddOn.setPremium(addOnPremium * paymentTerm);
						riskSIperUnit = insuredPersonAddOn.getAddOn().getSumInsuredPerUnit();
						insuredPersonAddOn.setSumInsured(insuredPersonAddOn.getUnit() * riskSIperUnit);
					} catch (SystemException e) {
						if (e.getSource() == null)
							e.setSource("Insured Person - " + person.getFullName() + ", AddOn - "
									+ insuredPersonAddOn.getAddOn().getName());
						throw e;
					}
				}
			} catch (SystemException e) {
				if (e.getSource() == null)
					e.setSource("Insured Person - " + person.getFullName() + ", Product - "
							+ person.getProduct().getName());
				throw e;
			}
		}
		return medicalProposal;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void calculateTermPremium(MedicalProposal medicalProposal) {

		double premium = 0.0, addOnPremium = 0.0, termPremium = 0.0, addOnTermPremium = 0.0;
		int paymentTerm = medicalProposal.getPaymentTerm();
		for (MedicalProposalInsuredPerson person : medicalProposal.getMedicalProposalInsuredPersonList()) {
			premium = person.getPremium();
			addOnPremium = person.getAddOnPremium();

			termPremium = Utils.divide(premium, paymentTerm);
			addOnTermPremium = Utils.divide(addOnPremium, paymentTerm);
			person.setBasicTermPremium(termPremium);
			person.setAddOnTermPremium(addOnTermPremium);
		}

	}

	@Override
	public void setPeriodMonthForKeyFacterValue(int periodMonth, String paymentTypeId) {

		this.term = periodMonth;
		this.paymentTypeID = paymentTypeId;

	}

	@Override
	public <T> LifeMedicalCustomer createNewCustomer(T customerDto) {
		try {

			CustomerDto dto = (CustomerDto) customerDto;

//			Optional<Township> officeTownshipOptional = townshipService
//					.findById(dto.getOfficeAddress() == null ? null : dto.getOfficeAddress().getTownshipId());
			Optional<Township> permanentTownshipOptional = townshipService
					.findById(dto.getPermanentAddress() == null ? null : dto.getPermanentAddress().getTownshipId());
			Optional<Township> residentTownshipOptional = townshipService
					.findById(dto.getResidentAddress() == null ? null : dto.getResidentAddress().getTownshipId());

//			OfficeAddress officeAddress = new OfficeAddress();
//			officeAddress.setOfficeAddress(
//					dto.getOfficeAddress() == null ? null : dto.getOfficeAddress().getOfficeAddress());
//			officeAddress.setTownship(officeTownshipOptional.isPresent() ? officeTownshipOptional.get() : null);

			PermanentAddress permanentAddress = new PermanentAddress();
			permanentAddress.setPermanentAddress(
					dto.getPermanentAddress() == null ? null : dto.getPermanentAddress().getPermanentAddress());
			permanentAddress
					.setTownship(permanentTownshipOptional.isPresent() ? permanentTownshipOptional.get() : null);

			ResidentAddress residentAddress = new ResidentAddress();
			residentAddress.setResidentAddress(
					dto.getResidentAddress() == null ? null : dto.getResidentAddress().getResidentAddress());
			residentAddress.setTownship(residentTownshipOptional.isPresent() ? residentTownshipOptional.get() : null);

//			ContentInfo contentInfo = new ContentInfo();
//			contentInfo.setEmail(dto.getContentInfo() == null ? null : dto.getContentInfo().getEmail());
//			contentInfo.setFax(dto.getContentInfo() == null ? null : dto.getContentInfo().getFax());
//			contentInfo.setMobile(dto.getContentInfo() == null ? null : dto.getContentInfo().getMobile());
//			contentInfo.setPhone(dto.getContentInfo() == null ? null : dto.getContentInfo().getPhone());

			Name name = new Name();
			name.setFirstName(dto.getName() == null ? null : dto.getName().getFirstName());
			name.setMiddleName(dto.getName() == null ? null : dto.getName().getMiddleName());
			name.setLastName(dto.getName() == null ? null : dto.getName().getLastName());

//			List<LifeMedicalCustomerFamily> familyInfo = new ArrayList<LifeMedicalCustomerFamily>();

//			if (dto.getFamilyInfoList() != null) {
//				dto.getFamilyInfoList().forEach(familydto -> {
//
//					Name familyName = new Name();
//
//					familyName.setFirstName(familydto.getName().getFirstName());
//					familyName.setMiddleName(familydto.getName().getMiddleName());
//					familyName.setLastName(familydto.getName().getLastName());
//
//					LifeMedicalCustomerFamily family = new LifeMedicalCustomerFamily();
//					family.setInitialId(familydto.getInitialId());
//					family.setIdNo(familydto.getIdNo());
//					family.setDateOfBirth(familydto.getDateOfBirth());
//					family.setName(familyName);
//					family.setIdType(familydto.getIdType());
//					family.setRelationshipId(familydto.getRelationShipId());
//					family.setIndustryId(familydto.getIndustryId());
//					family.setOccupationId(familydto.getOccupationId());
//					
//					familyInfo.add(family);
//
//				});
//			}

			LifeMedicalCustomer customer = new LifeMedicalCustomer();
			customer.setInitialId(dto.getInitialId());
			customer.setFatherName(dto.getFatherName());
			customer.setDateOfBirth(dto.getDateOfBirth());
			customer.setLabourNo(null);
			customer.setGender(dto.getGender());
			customer.setFullIdNo(dto.getIdNo());
			customer.setIdType(dto.getIdType());
			if (StringUtils.isNotBlank(String.valueOf(dto.getMaritalStatus()))) {
				customer.setMaritalStatus(dto.getMaritalStatus());
			}
			customer.setOfficeAddress(null);
			customer.setPermanentAddress(permanentAddress);
			customer.setResidentAddress(residentAddress);
			customer.setContentInfo(null);
			customer.setName(name);
			customer.setFamilyInfo(null);
			customer.setQualificationId(null);
			customer.setReligionId(null);
			customer.setOccupationId(dto.getOccupationId());
			customer.setNationalityId(dto.getCountryId());

			boolean customerExistInMain = checkCustomerAvailability(dto);
					
			customer.setStatus(customerExistInMain ? true : false);
			customer.setReferenceId(customerExistInMain ? this.referenceId : null);

			customerTempRepository.save(customer);

			return customer;

		} catch (DAOException e) {
			throw new SystemException(e.getErrorCode(), e.getMessage());
		}
	}
	
	@Override
	public LifeMedicalCustomer checkCustomerAvailabilityTemp(CustomerDto dto) {

		try {

			String idNo = StringUtils.isBlank(dto.getIdNo()) ? "idNo" : dto.getIdNo();
			String idType = (dto.getIdType()).toString();

			LifeMedicalCustomer customer = customerTempRepository.findCustomerByIdNoAndIdType(idNo, idType);

			return customer;

		} catch (DAOException e) {
			throw new SystemException(e.getErrorCode(), e.getMessage());
		}
	}

	@Override
	public Boolean checkCustomerAvailability(CustomerDto dto) {
		try {

			String idNo = dto.getIdNo();
			String idType = (dto.getIdType()).toString();

			Customer customer = customerRepository.findCustomerByIdNoAndIdType(idNo, idType);

			if (customer == null) {
				return false;
			}else {
				this.referenceId = customer.getId();
				return true;
			}

		} catch (DAOException e) {
			throw new SystemException(e.getErrorCode(), e.getMessage());
		}
	}

}
