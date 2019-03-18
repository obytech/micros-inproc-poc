package com.finastra.poc.microservicesinproc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Configuration
public class AppConfig {

    private static final String FLOW_FILE = "/flows.properties";

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Bean
    // <Flow name, Next topic>
    public Map<String, String> flowMap() throws IOException {
        Properties props = new Properties();

        props.load(new BufferedInputStream(getClass().getResourceAsStream(FLOW_FILE)));
        String servicePrefix = activeProfile + ".";
        return props.stringPropertyNames()
                .stream()
                .filter(prop -> prop.startsWith(servicePrefix))
                .collect(
                        Collectors.toMap(
                                prop -> prop.substring(activeProfile.length() + 1),
                                props::getProperty));
    }
}
