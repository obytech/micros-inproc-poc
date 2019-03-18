package com.finastra.poc.microservicesinproc.services;

import com.finastra.poc.microservicesinproc.common.KafkaEventListener;
import com.finastra.poc.microservicesinproc.config.KafkaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;

import java.util.Map;

@Service
@Profile(FileCreationTimeService.TOPIC)
public class FileCreationTimeService implements KafkaEventListener {

    public static final String TOPIC = "FileCreationTime";

    @Autowired
    private Map<String, String> flowMap;

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private KafkaSender<Integer, String> kafkaSender;

    @Override
    public KafkaConfig getKafkaConfig() {
        return kafkaConfig;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }

    @Override
    public void handleIncomingEvent(ReceiverRecord<Integer, String> record) {

    }
}
