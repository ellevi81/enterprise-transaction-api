package com.santiagolevi.transaction.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "transaction.events";
    public static final String DLX = "transaction.events.dlx";

    public static final String QUEUE_AUDIT = "transaction.audit";
    public static final String QUEUE_NOTIFICATION = "transaction.notification";
    public static final String DLQ_AUDIT = "transaction.audit.dlq";
    public static final String DLQ_NOTIFICATION = "transaction.notification.dlq";

    @Bean
    TopicExchange transactionExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    TopicExchange deadLetterExchange() {
        return ExchangeBuilder.topicExchange(DLX).durable(true).build();
    }

    @Bean
    Queue auditQueue() {
        return QueueBuilder.durable(QUEUE_AUDIT)
            .withArgument("x-dead-letter-exchange", DLX)
            .withArgument("x-dead-letter-routing-key", DLQ_AUDIT)
            .build();
    }

    @Bean
    Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION)
            .withArgument("x-dead-letter-exchange", DLX)
            .withArgument("x-dead-letter-routing-key", DLQ_NOTIFICATION)
            .build();
    }

    @Bean
    Queue auditDlq() {
        return QueueBuilder.durable(DLQ_AUDIT).build();
    }

    @Bean
    Queue notificationDlq() {
        return QueueBuilder.durable(DLQ_NOTIFICATION).build();
    }

    @Bean
    Binding auditBinding(Queue auditQueue, TopicExchange transactionExchange) {
        return BindingBuilder.bind(auditQueue).to(transactionExchange).with("transaction.#");
    }

    @Bean
    Binding notificationBinding(Queue notificationQueue, TopicExchange transactionExchange) {
        return BindingBuilder.bind(notificationQueue).to(transactionExchange).with("transaction.#");
    }

    @Bean
    Binding auditDlqBinding(Queue auditDlq, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(auditDlq).to(deadLetterExchange).with(DLQ_AUDIT);
    }

    @Bean
    Binding notificationDlqBinding(Queue notificationDlq, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(notificationDlq).to(deadLetterExchange).with(DLQ_NOTIFICATION);
    }

    @Bean
    MessageConverter messageConverter() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(om);
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
