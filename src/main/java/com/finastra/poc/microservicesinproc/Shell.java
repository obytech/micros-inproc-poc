package com.finastra.poc.microservicesinproc;

import com.finastra.poc.microservicesinproc.common.Consts;
import com.finastra.poc.microservicesinproc.services.CreateFileService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@Profile("CreateFile")
public class Shell {

    private static final String COUNT = "count";
    private static final String COUNT_AND_REVERSE_LINES = "countAndReverseLines";
    private static final String CREATION_TIME_REMOTE_FILE = "creationTimeRemoteFile";

    private AtomicInteger counter = new AtomicInteger();

    @Autowired
    private KafkaSender<Integer, String> kafkaSender;

    @Autowired
    private Map<String, String> flowMap;

    @GetMapping(COUNT)
    public String lineCount() {
        int jobId = counter.incrementAndGet();
//        String targetTopic = flowMap.get(COUNT);
        String targetTopic = CreateFileService.TOPIC; // Simulates first call via MQ.

        ProducerRecord<Integer, String> data = new ProducerRecord<>(targetTopic, jobId, "value");
        data.headers().add(Consts.FLOW_HEADER, COUNT.getBytes(StandardCharsets.UTF_8));
        SenderRecord<Integer, String, String> record = SenderRecord.create(data, "bla bla");

        kafkaSender.send(Mono.just(record))
                .subscribeOn(Schedulers.elastic())
                .subscribe();

        return "Activated flow: " + targetTopic;
    }

    /*@GetMapping(COUNT_AND_REVERSE_LINES)
    @GetMapping("creationTimeRemoteFile"))*/
}
