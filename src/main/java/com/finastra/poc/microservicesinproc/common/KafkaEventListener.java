package com.finastra.poc.microservicesinproc.common;

import com.finastra.poc.microservicesinproc.config.KafkaConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.SenderRecord;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

public interface KafkaEventListener {

    @PostConstruct
    default void init() {
        getKafkaConfig().<Integer, String>registerListener(getTopic())
                .doOnNext(this::handleIncomingEvent)
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }


    KafkaConfig getKafkaConfig();

    String getTopic();

    void handleIncomingEvent(ReceiverRecord<Integer, String> record);

    default String getFlow(ReceiverRecord<Integer, String> record) {
        Header flowHeader = record.headers().headers(Consts.FLOW_HEADER).iterator().next();
        return new String(flowHeader.value(), StandardCharsets.UTF_8);
    }

    default SenderRecord<Integer, String, String> createOutgoingMessage(String topic, String flow, int key, String payload) {
        ProducerRecord<Integer, String> data = new ProducerRecord<>(topic, key, payload);
        data.headers().add(Consts.FLOW_HEADER, flow.getBytes(StandardCharsets.UTF_8));
        return SenderRecord.create(data, "onnext-data");
    }
}
