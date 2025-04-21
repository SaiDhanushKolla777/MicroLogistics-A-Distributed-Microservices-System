package com.micrologistics.item.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.micrologistics.common.event.ItemRegisteredEvent;

/**
 * Configuration for Kafka producers and topics.
 */
@Configuration
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${kafka.topics.item-registered}")
    private String itemRegisteredTopic;
    
    /**
     * Configure the Kafka producer factory for ItemRegisteredEvents.
     * 
     * @return The producer factory
     */
    @Bean
    public ProducerFactory<String, ItemRegisteredEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    /**
     * Create a KafkaTemplate for ItemRegisteredEvents.
     * 
     * @return The Kafka template
     */
    @Bean
    public KafkaTemplate<String, ItemRegisteredEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    /**
     * Define the item-registered topic.
     * 
     * @return The topic configuration
     */
    @Bean
    public NewTopic itemRegisteredTopic() {
        return TopicBuilder.name(itemRegisteredTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
