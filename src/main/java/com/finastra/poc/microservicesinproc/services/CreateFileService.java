package com.finastra.poc.microservicesinproc.services;

import com.finastra.poc.microservicesinproc.common.KafkaEventListener;
import com.finastra.poc.microservicesinproc.config.KafkaConfig;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Profile(CreateFileService.TOPIC)
public class CreateFileService implements KafkaEventListener {
    public static final String TOPIC = "CreateFile";

    private static final String ROOT_PATH = "/tmp/";
    private static final int ROWS_QTY = 1000;

    @Autowired
    private Map<String, String> flowMap;

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private KafkaSender<Integer, String> kafkaSender;

    public File createFile() {
        File file;
        do {
            file = new File(ROOT_PATH, UUID.randomUUID().toString().replaceAll("-", ""));
        } while (file.exists());

        populateData(file);
        return file;
    }

    private void populateData(File file) {
        try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
            IntStream.range(0, ROWS_QTY)
                    .forEach( i -> {
                        try {
                            String data = generateRow() + (i +1 == ROWS_QTY ? "" : "\n");
                            stream.write(data.getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateRow() {
        Random random = new SecureRandom();
        List<String> words = Arrays.asList(UUID.randomUUID().toString().split("-"));
        Map<Integer, Integer> vals = IntStream.range(0, words.size())
                .mapToObj(i -> i)
                .collect(Collectors.toMap(i -> random.nextInt(), Function.identity()));

        List<Integer> shuffle = vals.keySet()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        String shuffled = "";
        for (Integer shuf : shuffle) {
            int idx = vals.get(shuf);
            shuffled += words.get(idx) + " ";
        }

        return shuffled.trim();
    }

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
        String flow = getFlow(record);
        String nextTopic = flowMap.get(flow);
//        String payload = record.value();
        File file = createFile();

        String outgoingPayload = getFileAsString(file);
        SenderRecord<Integer, String, String> outgoingMessage = createOutgoingMessage(nextTopic, flow, record.key(), outgoingPayload);

        kafkaSender.send(Mono.just(outgoingMessage))
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    private String getFileAsString(File file) {

        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(file))) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
