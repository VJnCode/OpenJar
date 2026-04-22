package com.openjar.socialservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfig {
//
//    public static final String EXCHANGE = "notification_exchange";
//    public static final String ROUTING_KEY = "notification_routing_key";
//
//
//    @Bean
//    public TopicExchange exchange() {
//        return new TopicExchange(EXCHANGE);
//    }
//
//    @Bean
//    public MessageConverter converter() {
//        return new JacksonJsonMessageConverter();
//    }
//
//    @Bean
//    public AmqpTemplate template(ConnectionFactory connectionFactory) {
//        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setMessageConverter(converter());
//        return rabbitTemplate;
//    }
//}

@Configuration
public class RabbitMQConfig {

    // 🔹 Exchanges
    public static final String NOTIFICATION_EXCHANGE = "notification_exchange";
    public static final String OPENJAR_EXCHANGE = "openjar_exchange";

    // 🔹 Routing keys
    public static final String NOTIFICATION_ROUTING_KEY = "notification_routing_key";
    public static final String LIKE_ROUTING_KEY = "like_routing_key";
    public static final String LIKE_Recipe_ROUTING_KEY = "recipe.like.event";

    // 🔹 Queues
    public static final String LIKE_QUEUE = "like_queue";
    public static final String LIKE_RECIPE_QUEUE = "recipe_like_queue";

    // ================= EXCHANGES =================

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public TopicExchange openjarExchange() {
        return new TopicExchange(OPENJAR_EXCHANGE);
    }

    // ================= QUEUES =================

    @Bean
    public Queue likeQueue() {
        return new Queue(LIKE_QUEUE, true);
    }

    @Bean
    public Queue likeRecipeQueue() {
        return new Queue(LIKE_RECIPE_QUEUE, true);
    }

    // ================= BINDINGS =================

    @Bean
    public Binding likeBinding() {
        return BindingBuilder
                .bind(likeQueue())
                .to(openjarExchange())
                .with(LIKE_ROUTING_KEY);
    }

    @Bean
    public Binding likeRecipeBinding() {
        return BindingBuilder
                .bind(likeRecipeQueue())
                .to(openjarExchange())
                .with(LIKE_Recipe_ROUTING_KEY);
    }

    // ================= CONVERTER =================

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // ================= TEMPLATE =================

//    @Bean
//    public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate template = new RabbitTemplate(connectionFactory);
//        template.setMessageConverter(messageConverter());
//        return template;
//    }
}