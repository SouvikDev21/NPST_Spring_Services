package com.fund_transfer.backend.service;

import com.fund_transfer.backend.Otp.OtpClient;
import com.fund_transfer.backend.dto.Mapper.BeneficiaryMapper;
import com.fund_transfer.backend.dto.Request.CreateBeneficiaryRequest;
//import com.fund_transfer.backend.dto.Request.UpdateBeneficiaryRequest;
import com.fund_transfer.backend.dto.Request.UpdateBeneficiaryRequest;
import com.fund_transfer.backend.dto.Response.BeneficiaryResponse;
import com.fund_transfer.backend.dto.Response.OtpSendResponse;
import com.fund_transfer.backend.entity.Beneficiary;
import com.fund_transfer.backend.enums.BeneficiaryStatus;
import com.fund_transfer.backend.exception.BeneficiaryNotFoundException;
import com.fund_transfer.backend.exception.DuplicateBeneficiaryException;
import com.fund_transfer.backend.ifsc.IfscDetailsResponse;
import com.fund_transfer.backend.ifsc.IfscLookupService;
import com.fund_transfer.backend.repository.BeneficiaryRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BeneficiaryService {

    // 24-hour cooling period from the moment a beneficiary is added, per
    // product requirement — the beneficiary cannot be used for a transfer
    // until coolingPeriodEndsAt has passed.
    private static final long COOLING_PERIOD_HOURS = 24;

    private final BeneficiaryRepo beneficiaryRepo;
    private final IfscLookupService ifscLookupService;
    private final BeneficiaryMapper beneficiaryMapper;
    private final OtpClient otpClient;
    private final String addBeneficiaryOtpPurpose;

    public BeneficiaryService(BeneficiaryRepo beneficiaryRepo,
                              IfscLookupService ifscLookupService,
                              BeneficiaryMapper beneficiaryMapper,
                              OtpClient otpClient,
                              @Value("${otp.add-beneficiary-purpose:ADD_BENEFICIARY}") String addBeneficiaryOtpPurpose) {
        this.beneficiaryRepo = beneficiaryRepo;
        this.ifscLookupService = ifscLookupService;
        this.beneficiaryMapper = beneficiaryMapper;
        this.otpClient = otpClient;
        this.addBeneficiaryOtpPurpose = addBeneficiaryOtpPurpose;
    }

    /**
     * Step 1 of the add-beneficiary flow: send an OTP to the customer's
     * registered mobile number. The frontend calls this first, shows an
     * OTP-entry screen, then calls create() with the returned otp_reference
     * plus whatever code the customer typed in.
     *
     * amount is intentionally not passed through here — it's only meaningful
     * for money-movement OTP purposes (e.g. LOGIN/TRANSFER in the sample),
     * not for adding a beneficiary.
     */
    public OtpSendResponse.OtpSendData sendAddBeneficiaryOtp(String ownerCif, String mobileNumber) {
        return otpClient.sendOtp(ownerCif, mobileNumber, addBeneficiaryOtpPurpose, null);
    }

    /**
     * ownerCif and ownerKeycloakUserId are passed in explicitly by the controller
     * (ownerCif from the X-CIF header, ownerKeycloakUserId from the validated
     * Keycloak token's subject claim) — never taken from the request body.
     */
    @Transactional
    public BeneficiaryResponse create(String ownerCif, String ownerKeycloakUserId, CreateBeneficiaryRequest request) {

        // OTP must be verified BEFORE any duplicate check / IFSC lookup / persistence —
        // fail fast on an unverified customer rather than doing other work first.
        // otpClient.verifyOtp throws OtpVerificationException (mapped to a 4xx by
        // GlobalExceptionHandler) on a wrong code, expired reference, or mismatched cif.
        otpClient.verifyOtp(request.otpReference(), request.otpCode(), ownerCif);

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

        Instant now = Instant.now();

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
                // Cooling period starts now, at creation time (OTP has already been
                // verified above) — 24 hours per product requirement. A beneficiary
                // stays PENDING_COOLING_PERIOD until this instant passes; see
                // requireOwned()/listForCustomer() below, which flip the status to
                // ACTIVE lazily once it's read after expiry.
                .coolingPeriodEndsAt(now.plus(COOLING_PERIOD_HOURS, ChronoUnit.HOURS))
                .build();

        beneficiary = beneficiaryRepo.save(beneficiary);
        return beneficiaryMapper.toResponse(beneficiary);
    }

    @Transactional
    public List<BeneficiaryResponse> listForCustomer(String ownerCif) {
        List<Beneficiary> beneficiaries = beneficiaryRepo.findByOwnerCifAndStatusNot(ownerCif, BeneficiaryStatus.DELETED);
        beneficiaries.forEach(this::promoteIfCoolingPeriodElapsed);
        return beneficiaries.stream()
                .map(beneficiaryMapper::toResponse)
                .toList();
    }

    @Transactional
    public BeneficiaryResponse getOne(String ownerCif, Long beneficiaryId) {
        Beneficiary beneficiary = requireOwned(ownerCif, beneficiaryId);
        promoteIfCoolingPeriodElapsed(beneficiary);
        return beneficiaryMapper.toResponse(beneficiary);
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

    // Lazily flips PENDING_COOLING_PERIOD -> ACTIVE once coolingPeriodEndsAt has
    // passed. Called from read paths within an already-@Transactional method, so
    // the change is persisted via Hibernate dirty checking — no explicit save()
    // needed. BLOCKED/DELETED/ACTIVE beneficiaries are left untouched.
    private void promoteIfCoolingPeriodElapsed(Beneficiary beneficiary) {
        if (beneficiary.getStatus() == BeneficiaryStatus.PENDING_COOLING_PERIOD
                && beneficiary.getCoolingPeriodEndsAt() != null
                && !beneficiary.getCoolingPeriodEndsAt().isAfter(Instant.now())) {
            beneficiary.setStatus(BeneficiaryStatus.ACTIVE);
        }
    }
}