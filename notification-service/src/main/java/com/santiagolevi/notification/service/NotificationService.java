package com.santiagolevi.notification.service;

import com.santiagolevi.notification.dto.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    public void notify(TransactionEvent event) {
        switch (event.getEventType()) {
            case "TRANSACTION_CREATED" -> log.info(
                "[NOTIFY] Transaction {} created by {} — {} {} {}",
                event.getTransactionId(), event.getActor(),
                event.getTransactionType(), event.getAmount(), event.getCurrency()
            );
            case "TRANSACTION_UPDATED" -> log.info(
                "[NOTIFY] Transaction {} updated to status {} by {}",
                event.getTransactionId(), event.getStatus(), event.getActor()
            );
            case "TRANSACTION_DELETED" -> log.info(
                "[NOTIFY] Transaction {} deleted by {}",
                event.getTransactionId(), event.getActor()
            );
            default -> log.debug("[NOTIFY] Unhandled event type: {}", event.getEventType());
        }
    }
}
