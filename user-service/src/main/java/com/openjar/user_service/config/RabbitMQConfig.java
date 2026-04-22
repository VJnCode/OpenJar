package com.openjar.user_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//public class RabbitMQConfig {
//
//    public static final String QUEUE_NAME = "notification_queue";
//    public static final String EXCHANGE_NAME = "openjar_exchange";
//    public static final String ROUTING_KEY = "notification_routing_key";
//
//    @Bean
//    public Queue queue() {
//        return new Queue(QUEUE_NAME, true); // true = durable (survives restarts)
//    }
//
//    @Bean
//    public TopicExchange exchange() {
//        return new TopicExchange(EXCHANGE_NAME);
//    }
//
//    @Bean
//    public Binding binding(Queue queue, TopicExchange exchange) {
//        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
//    }
//
////    @Bean
////    public MessageConverter jsonMessageConverter() {
////        return new JacksonJsonMessageConverter();
////    }
////chnaged to newer version since old got deprecated
//    @Bean
//    public MessageConverter jsonMessageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate template = new RabbitTemplate(connectionFactory);
//        template.setMessageConverter(jsonMessageConverter());
//        return template;
//    }
//}
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "openjar_exchange";

    // ✅ EMAIL
    public static final String EMAIL_QUEUE = "email_queue";
    public static final String EMAIL_ROUTING_KEY = "email.notification";



    // -------- QUEUES --------
    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true);
    }

    // -------- EXCHANGE --------
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // -------- BINDINGS --------
    @Bean
    public Binding emailBinding(TopicExchange exchange) {
        return BindingBuilder
                .bind(emailQueue())
                .to(exchange)
                .with(EMAIL_ROUTING_KEY);
    }


    // -------- CONVERTER --------
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}