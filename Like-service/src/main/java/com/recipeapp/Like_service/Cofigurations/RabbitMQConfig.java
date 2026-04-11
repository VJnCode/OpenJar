package com.recipeapp.Like_service.Cofigurations;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;





@Configuration
public class RabbitMQConfig {

    public static final String LIKE_QUEUE = "like_queue";
    public static final String EXCHANGE = "openjar_exchange";
    public static final String LIKE_ROUTING_KEY = "like_routing_key";

//like-recipe

    public static final String LIKE_Recipe_QUEUE = "recipe_like_queue";
    public static final String LIKE_Recipe_ROUTING_KEY = "recipe.like.event";


    // Like queue bean
    @Bean
    public Queue likeQueue() {
        return new Queue(LIKE_QUEUE, true);
    }


    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // Like binding
    @Bean
    public Binding likeBinding(Queue likeQueue, TopicExchange exchange) {
        return BindingBuilder.bind(likeQueue).to(exchange).with(LIKE_ROUTING_KEY);
    }

    @Bean
    public Queue likeRecipeQueue() {
        return new  Queue(LIKE_Recipe_QUEUE, true);
    }

    @Bean
    public Binding likeRecipeBinding(TopicExchange exchange) {
        return BindingBuilder
                .bind(likeRecipeQueue())
                .to(exchange)
                .with(LIKE_Recipe_ROUTING_KEY);
    }


    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}