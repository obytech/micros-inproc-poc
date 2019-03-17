package com.finastra.poc.microservicesinproc.services;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Profile("CreateFile")
public class CreateFileService {

    private static final String ROOT_PATH = "/tmp/";
    private static final int ROWS_QTY = 1000;

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
                            String data = generateRow() + "\n";
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


    public static void main(String args[]) {
        System.out.println(new CreateFileService().createFile().getName());
    }
}
