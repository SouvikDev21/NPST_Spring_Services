package com.term_deposit.backend.integration.outbox;

import com.term_deposit.backend.entity.OutboxEvent;
import com.term_deposit.backend.enums.OutboxStatus;
import com.term_deposit.backend.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayWorker {

    private final OutboxEventRepository outboxEventRepository;

    // Wakes up every 10 seconds (10000 milliseconds)
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void processPendingOutboxEvents() {
        // 1. Fetch all pending events
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatus(OutboxStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return; // Go back to sleep if there's no work
        }

        log.info("Outbox Worker woke up! Found {} pending events to process.", pendingEvents.size());

        // 2. Process each event
        for (OutboxEvent event : pendingEvents) {
            try {
                // In the future, this is where you push to Kafka or RabbitMQ.
                // For now, we simulate a successful publish to the Notification Service.
                log.info("Publishing event type [{}] for reference [{}]: {}",
                        event.getEventType(), event.getAggregateId(), event.getPayload());

                // 3. Mark as COMPLETED so it doesn't get processed again
                event.setStatus(OutboxStatus.SENT);
                event.setProcessedAt(Instant.now());

                outboxEventRepository.save(event);

                log.info("Successfully processed and marked OutboxEvent {} as COMPLETED.", event.getId());

            } catch (Exception e) {
                // If the message broker is down, we log the error and leave it as PENDING to retry next time
                log.error("Failed to process OutboxEvent {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}