package com.santiagolevi.audit.config;

import com.santiagolevi.audit.dto.TransactionEvent;
import com.santiagolevi.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final AuditService auditService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_AUDIT, containerFactory = "rabbitListenerContainerFactory")
    public void onTransactionEvent(TransactionEvent event) {
        log.debug("Received transaction event: type={} transactionId={}", event.getEventType(), event.getTransactionId());
        auditService.record(event);
    }
}
