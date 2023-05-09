package com.example.jms_message_processing.configurations;

import com.example.jms_message_processing.errorHandling.CustomErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import javax.jms.ConnectionFactory;

@Configuration
public class MyCustomJmsConfiguration {
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory, CustomErrorHandler customErrorHandler) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setErrorHandler(customErrorHandler);
        factory.setSessionTransacted(true);
        return factory;
    }
}
