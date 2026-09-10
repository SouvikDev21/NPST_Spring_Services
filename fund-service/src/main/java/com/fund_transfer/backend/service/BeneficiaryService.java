package com.fund_transfer.backend.service;

import com.fund_transfer.backend.dto.Mapper.BeneficiaryMapper;
import com.fund_transfer.backend.dto.Request.CreateBeneficiaryRequest;
import com.fund_transfer.backend.dto.Request.UpdateBeneficiaryRequest;
import com.fund_transfer.backend.dto.Response.BeneficiaryResponse;
import com.fund_transfer.backend.entity.Beneficiary;
import com.fund_transfer.backend.enums.BeneficiaryStatus;
import com.fund_transfer.backend.exception.BeneficiaryNotFoundException;
import com.fund_transfer.backend.exception.DuplicateBeneficiaryException;
import com.fund_transfer.backend.ifsc.IfscDetailsResponse;
import com.fund_transfer.backend.ifsc.IfscLookupService;
import com.fund_transfer.backend.repository.BeneficiaryRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepo beneficiaryRepo;
    private final IfscLookupService ifscLookupService;
    private final BeneficiaryMapper beneficiaryMapper;

    public BeneficiaryService(BeneficiaryRepo beneficiaryRepo,
                              IfscLookupService ifscLookupService,
                              BeneficiaryMapper beneficiaryMapper) {
        this.beneficiaryRepo = beneficiaryRepo;
        this.ifscLookupService = ifscLookupService;
        this.beneficiaryMapper = beneficiaryMapper;
    }

    /**
     * ownerCif and ownerKeycloakUserId are passed in explicitly by the controller,
     * resolved from the authenticated security principal — never taken from the
     * request body (see CreateBeneficiaryRequest's IDOR-fix comment).
     */
    @Transactional
    public BeneficiaryResponse create(String ownerCif, String ownerKeycloakUserId, CreateBeneficiaryRequest request) {
        if (beneficiaryRepo.existsByOwnerCifAndBeneficiaryAccountNumberAndBeneficiaryIfscCode(
                ownerCif, request.beneficiaryAccountNumber(), request.beneficiaryIfscCode())) {
            throw new DuplicateBeneficiaryException(request.beneficiaryAccountNumber(), request.beneficiaryIfscCode());
        }

        // Server resolves bank name from IFSC — never trust a client-submitted bank name.
        // beneficiaryBankName is nullable on the entity, so a lookup failure here doesn't
        // need to hard-fail beneficiary creation if the product decision is to allow it;
        // currently this propagates the IfscLookupUnavailableException/IfscNotFoundException
        // up to the controller, which is the stricter (fail-closed) choice — revisit if the
        // product wants "save now, resolve bank name later" instead.
        IfscDetailsResponse ifscDetails = ifscLookupService.lookup(request.beneficiaryIfscCode());

        Beneficiary beneficiary = Beneficiary.builder()
                .ownerCif(ownerCif)
                .ownerKeycloakUserId(ownerKeycloakUserId)
                .beneficiaryName(request.beneficiaryName())
                .beneficiaryAccountNumber(request.beneficiaryAccountNumber())
                .beneficiaryIfscCode(ifscDetails.getIfsc())
                .beneficiaryBankName(ifscDetails.getBankName())
                .nickname(request.nickname())
                .transferMode(request.transferMode())
                .type(request.type())
                .status(BeneficiaryStatus.PENDING_COOLING_PERIOD)
                // coolingPeriodEndsAt intentionally left null here — set by the OTP
                // confirmation step (next build step), once the bank-configurable
                // cooling-off duration is applied.
                .build();

        beneficiary = beneficiaryRepo.save(beneficiary);
        return beneficiaryMapper.toResponse(beneficiary);
    }

    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> listForCustomer(String ownerCif) {
        return beneficiaryRepo.findByOwnerCifAndStatusNot(ownerCif, BeneficiaryStatus.DELETED).stream()
                .map(beneficiaryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BeneficiaryResponse getOne(String ownerCif, Long beneficiaryId) {
        return beneficiaryMapper.toResponse(requireOwned(ownerCif, beneficiaryId));
    }

    @Transactional
    public BeneficiaryResponse rename(String ownerCif, Long beneficiaryId, UpdateBeneficiaryRequest request) {
        Beneficiary beneficiary = requireOwned(ownerCif, beneficiaryId);
        beneficiary.setNickname(request.nickname());
        return beneficiaryMapper.toResponse(beneficiary);
    }

    // Soft delete — DELETED is a status value on this entity, not a row removal.
    @Transactional
    public void delete(String ownerCif, Long beneficiaryId) {
        Beneficiary beneficiary = requireOwned(ownerCif, beneficiaryId);
        beneficiary.setStatus(BeneficiaryStatus.DELETED);
    }

    private Beneficiary requireOwned(String ownerCif, Long beneficiaryId) {
        return beneficiaryRepo.findByIdAndOwnerCif(beneficiaryId, ownerCif)
                .orElseThrow(() -> new BeneficiaryNotFoundException(beneficiaryId));
    }
}
