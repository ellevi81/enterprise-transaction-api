package com.santiagolevi.notification.config;

import com.santiagolevi.notification.dto.TransactionEvent;
import com.santiagolevi.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION, containerFactory = "rabbitListenerContainerFactory")
    public void onTransactionEvent(TransactionEvent event) {
        log.debug("Received event for notification: type={}", event.getEventType());
        notificationService.notify(event);
    }
}
