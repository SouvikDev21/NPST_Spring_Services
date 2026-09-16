package com.fund_transfer.backend.controller;

import com.fund_transfer.backend.dto.Request.CreateBeneficiaryRequest;
import com.fund_transfer.backend.dto.Request.UpdateBeneficiaryRequest;
import com.fund_transfer.backend.dto.Response.BeneficiaryResponse;
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

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    // NOTE: assumes Spring Security's resource-server JWT support with Keycloak,
    // and that the access token carries CIF as a custom claim (e.g. "cif").
    // Adjust claim names / principal type to match your actual Auth/Identity
    // Service token shape — this is the wiring point to confirm with Pod C.

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, 'beneficiary:create')")

    public ResponseEntity<BeneficiaryResponse> create(
            @AuthenticationPrincipal Jwt jwt,
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
            @AuthenticationPrincipal Jwt jwt,
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
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody UpdateBeneficiaryRequest request) {
        return ResponseEntity.ok(beneficiaryService.rename(jwt.getClaimAsString("cif"), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'beneficiary:create')")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
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
