package com.finastra.poc.microservicesinproc.services;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
@Profile("LineCount")
public class LineCountService {


    public long getLineCount(File file) {
        try (BufferedReader buf = new BufferedReader(new FileReader(file))) {
            return buf.lines() .count();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to count all lines", e);
        }
    }
}
