package com.finastra.poc.microservicesinproc.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${kafka.bootstrap.server:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public Map<String, Object> producerProps() {
        Map<String, Object> props = new HashMap<>(10);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "acc-lookup.encrich-data.producer");
//        props.put(ProducerConfig.ACKS_CONFIG, "all");// TODO re-config
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return props;
    }

    @Bean
    public KafkaSender<Integer, String> kafkaSender() {
        SenderOptions<Integer, String> senderOptions = SenderOptions.create(producerProps());
        return KafkaSender.create(senderOptions);
    }

    @Bean
    public Map<String, Object> consumerProps() {
        Map<String, Object> props = new HashMap<>(10);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "acc-lookup.enriched-data.consumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "acc-lookup.enriched-data.consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.finastra.poc.commons.entities");

        return props;
    }

    public <K, V> Flux<ReceiverRecord<K, V>> registerListener(String topic) {
        ReceiverOptions<K, V> receiverOptions = ReceiverOptions.create(consumerProps());
        receiverOptions.subscription(Collections.singletonList(topic));
        return KafkaReceiver.create(receiverOptions).receive();
    }
}