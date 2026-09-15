package com.term_deposit.backend.dto.Request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ApproveRequestDto(
        @NotNull(message = "Checker User ID is required")
        UUID checkerUserId
) {}