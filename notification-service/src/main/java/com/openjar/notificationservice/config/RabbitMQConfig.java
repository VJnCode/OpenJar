package com.openjar.notificationservice.config;

import org.springframework.amqp.core.*;
// Notice the new import below (No '2' in the name!)
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//public class RabbitMQConfig {
//
//    public static final String QUEUE = "notification_queue";
//    public static final String EXCHANGE = "openjar_exchange"; // Updated to match User Service
//    public static final String ROUTING_KEY = "notification_routing_key"; // Updated to match User Service
//
//    @Bean
//    public Queue queue() {
//        return new Queue(QUEUE, true);
//    }
//
//    @Bean
//    public TopicExchange exchange() {
//        return new TopicExchange(EXCHANGE);
//    }
//
//    @Bean
//    public Binding binding(Queue queue, TopicExchange exchange) {
//        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
//    }
//
//    @Bean
//    public MessageConverter converter() {
//        return new Jackson2JsonMessageConverter();
//    }
//}

public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "openjar_exchange";

    public static final String N_QUEUE = "notification_queue";
    public static final String N_ROUTING_KEY = "notification_routing_key";

    //  EMAIL
    public static final String EMAIL_QUEUE = "email_queue";
    public static final String EMAIL_ROUTING_KEY = "email.notification";

//notification when  a user posts a recipe
    public static final String RECIPE_QUEUE = "recipe_queue";
    public static final String RECIPE_ROUTING_KEY = "recipe.notification";

//recipe-like queue
    public static final String LIKE_Recipe_QUEUE = "recipe_like_queue";
    public static final String LIKE_Recipe_ROUTING_KEY = "recipe.like.event";

    // -------- QUEUES --------
    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true);
    }

    // -------- QUEUES --------
    @Bean
    public Queue NOTIFICATIONQueue() {
        return new Queue(N_QUEUE, true);
    }

    @Bean
    public Queue recipeQueue() {
        return new Queue( RECIPE_QUEUE, true);
    }


    @Bean
    public Queue likeRecipeQueue() {
        return new Queue(LIKE_Recipe_QUEUE, true);
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

    @Bean
    public Binding notificationBinding(TopicExchange exchange) {
        return BindingBuilder
                .bind(NOTIFICATIONQueue())
                .to(exchange)
                .with( N_ROUTING_KEY);
    }



    @Bean
    public Binding recipeBinding(TopicExchange exchange) {
        return BindingBuilder
                .bind(recipeQueue())
                .to(exchange)
                .with(RECIPE_ROUTING_KEY);
    }



    @Bean
    public Binding likeRecipeBinding(TopicExchange exchange) {
        return BindingBuilder
                .bind(likeRecipeQueue())
                .to(exchange)
                .with(LIKE_Recipe_ROUTING_KEY);
    }

    // -------- CONVERTER --------
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}