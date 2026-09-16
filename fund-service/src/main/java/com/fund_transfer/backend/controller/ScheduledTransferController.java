package com.fund_transfer.backend.controller;

import com.fund_transfer.backend.dto.Request.CreateScheduledTransferRequest;
import com.fund_transfer.backend.dto.Request.UpdateScheduledTransferRequest;
import com.fund_transfer.backend.dto.Response.ScheduledTransferResponse;
import com.fund_transfer.backend.enums.ScheduleStatus;
import com.fund_transfer.backend.security.AuthenticatedUser;
import com.fund_transfer.backend.security.AuthenticatedUserService;
import com.fund_transfer.backend.service.ScheduledTransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scheduled-transfers")
public class ScheduledTransferController {

    private final ScheduledTransferService scheduledTransferService;
    private final AuthenticatedUserService authenticatedUserService;

    public ScheduledTransferController(
            ScheduledTransferService scheduledTransferService,
            AuthenticatedUserService authenticatedUserService) {

        this.scheduledTransferService =
                scheduledTransferService;

        this.authenticatedUserService =
                authenticatedUserService;
    }
    @PostMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, 'schedule:create')")
    public ResponseEntity<ScheduledTransferResponse> create(
            Authentication authentication,
            @Valid @RequestBody
            CreateScheduledTransferRequest request) {

        AuthenticatedUser user =
                authenticatedUserService
                        .getCurrentUser(authentication);

        return ResponseEntity.ok(
                scheduledTransferService.create(
                        user,
                        request
                )
        );
    }

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, 'schedule:view')")
    public ResponseEntity<List<ScheduledTransferResponse>> getAll(
            Authentication authentication) {

        AuthenticatedUser user =
                authenticatedUserService
                        .getCurrentUser(authentication);

        return ResponseEntity.ok(
                scheduledTransferService.getAll(
                        user.cif()
                )
        );
    }
    @GetMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'schedule:view')")
    public ResponseEntity<ScheduledTransferResponse> getById(
            @PathVariable Long id,
            Authentication authentication) {

        AuthenticatedUser user =
                authenticatedUserService
                        .getCurrentUser(authentication);

        return ResponseEntity.ok(
                scheduledTransferService.getById(
                        id,
                        user.cif()
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'schedule:create')")
    public ResponseEntity<ScheduledTransferResponse> update(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody
            UpdateScheduledTransferRequest request) {

        AuthenticatedUser user =
                authenticatedUserService
                        .getCurrentUser(authentication);

        return ResponseEntity.ok(
                scheduledTransferService.update(
                        id,
                        user.cif(),
                        request
                )
        );
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'schedule:create')")
    public ResponseEntity<ScheduledTransferResponse> cancel(
            @PathVariable Long id,
            Authentication authentication) {

        AuthenticatedUser user =
                authenticatedUserService
                        .getCurrentUser(authentication);

        return ResponseEntity.ok(
                scheduledTransferService.cancel(
                        id,
                        user.cif()
                )
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'schedule:create')")
    public ResponseEntity<ScheduledTransferResponse> changeStatus(
            @PathVariable Long id,
            @RequestParam ScheduleStatus status,
            Authentication authentication) {

        AuthenticatedUser user =
                authenticatedUserService
                        .getCurrentUser(authentication);

        return ResponseEntity.ok(
                scheduledTransferService.changeStatus(
                        id,
                        user.cif(),
                        status
                )
        );
    }
}