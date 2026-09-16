package com.fund_transfer.backend.controller;

import com.fund_transfer.backend.dto.Request.CreateBeneficiaryRequest;
import com.fund_transfer.backend.dto.Request.SendBeneficiaryOtpRequest;
import com.fund_transfer.backend.dto.Request.UpdateBeneficiaryRequest;
import com.fund_transfer.backend.dto.Response.BeneficiaryResponse;
import com.fund_transfer.backend.dto.Response.OtpSendResponse;
import com.fund_transfer.backend.exception.BeneficiaryNotFoundException;
import com.fund_transfer.backend.exception.DuplicateBeneficiaryException;
import com.fund_transfer.backend.security.AuthenticatedUser;
import com.fund_transfer.backend.security.AuthenticatedUserService;
import com.fund_transfer.backend.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/beneficiaries")
public class BeneficiaryController {

    public static final String CIF_HEADER = "X-CIF";

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    // ---------------------------------------------------------------
    // Step 1 of add-beneficiary: send OTP to the customer's mobile.
    // Frontend calls this, shows an OTP screen, then calls create()
    // below with the returned otp_reference + whatever code was typed.
    // ---------------------------------------------------------------
    @PostMapping("/otp/send")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'beneficiary:create')")
    public ResponseEntity<OtpSendResponse.OtpSendData> sendOtp(
            @RequestHeader(CIF_HEADER) String cif,
            @Valid @RequestBody SendBeneficiaryOtpRequest request) {
        OtpSendResponse.OtpSendData data = beneficiaryService.sendAddBeneficiaryOtp(cif, request.mobileNumber());
        return ResponseEntity.ok(data);
    }

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, 'beneficiary:create')")

    public ResponseEntity<BeneficiaryResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(CIF_HEADER) String cif,
            @Valid @RequestBody CreateBeneficiaryRequest request) {

        String ownerCif = jwt.getClaimAsString("cif");
        String ownerKeycloakUserId = jwt.getSubject();

        BeneficiaryResponse response =
                beneficiaryService.create(
                        "CIF100001",
                        ownerKeycloakUserId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, 'beneficiary:view')")
    public ResponseEntity<List<BeneficiaryResponse>> list(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                beneficiaryService.listForCustomer(
                        jwt.getClaimAsString("cif")
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'beneficiary:view')")
    public ResponseEntity<BeneficiaryResponse> getOne(
            @RequestHeader(CIF_HEADER) String cif,
            @PathVariable Long id) {

        return ResponseEntity.ok(
                beneficiaryService.getOne(
                        jwt.getClaimAsString("cif"),
                        id
                )
        );
    }

     @PatchMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'beneficiary:create')")
    public ResponseEntity<BeneficiaryResponse> rename(
            @RequestHeader(CIF_HEADER) String cif,
            @PathVariable Long id,
            @Valid @RequestBody UpdateBeneficiaryRequest request) {
        return ResponseEntity.ok(beneficiaryService.rename(cif, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'beneficiary:create')")
    public ResponseEntity<Void> delete(
            @RequestHeader(CIF_HEADER) String cif,
            @PathVariable Long id) {

        beneficiaryService.delete(
                jwt.getClaimAsString("cif"),
                id
        );

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(BeneficiaryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(BeneficiaryNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("BENEFICIARY_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(DuplicateBeneficiaryException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateBeneficiaryException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("BENEFICIARY_ALREADY_EXISTS", e.getMessage()));
    }

    public record ErrorResponse(String code, String message) {
    }
}