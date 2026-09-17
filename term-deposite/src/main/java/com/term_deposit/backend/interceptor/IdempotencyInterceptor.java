package com.term_deposit.backend.interceptor;

import com.term_deposit.backend.entity.IdempotencyRecord;
import com.term_deposit.backend.repository.IdempotencyRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {

    private final IdempotencyRepository idempotencyRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!request.getMethod().equalsIgnoreCase("POST")) {
            return true;
        }

        String idempotencyKey = request.getHeader("Idempotency-Key");

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {  //400 Bad request
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing Idempotency-Key header");
            return false;
        }

        if (idempotencyRepository.existsByIdempotencyKey(idempotencyKey)) { //409 Conflict error
            response.sendError(HttpServletResponse.SC_CONFLICT, "Duplicate request detected for Idempotency-Key");
            return false;
        }

        // Save the fresh key to block future duplicates
        IdempotencyRecord record = new IdempotencyRecord();
        record.setIdempotencyKey(idempotencyKey);
        record.setRequestHash(request.getRequestURI());
        record.setCreatedAt(Instant.now());

        idempotencyRepository.save(record);

        return true; // request handed over to controller
    }
}