package com.account_service.backend.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "dotenvProperties";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envPath = locateEnvFile();
        if (envPath == null || !Files.exists(envPath)) {
            return;
        }

        Map<String, Object> envMap = loadEnvFile(envPath);
        if (envMap.isEmpty()) {
            return;
        }

        // Set system properties so any non-Spring components can read them
        for (Map.Entry<String, Object> entry : envMap.entrySet()) {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        // Insert into environment right after systemEnvironment (OS env vars take precedence over .env, .env overrides application.yml defaults)
        if (environment.getPropertySources().contains(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
            environment.getPropertySources().addAfter(
                    StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                    new MapPropertySource(PROPERTY_SOURCE_NAME, envMap)
            );
        } else {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, envMap));
        }
    }

    private Path locateEnvFile() {
        String customPath = System.getProperty("DOTENV_FILE", System.getenv("DOTENV_FILE"));
        if (customPath != null && !customPath.isBlank()) {
            Path p = Paths.get(customPath);
            if (Files.exists(p)) {
                return p;
            }
        }

        Path currentDir = Paths.get(".env");
        if (Files.exists(currentDir)) {
            return currentDir;
        }

        Path subfolder = Paths.get("account-service", ".env");
        if (Files.exists(subfolder)) {
            return subfolder;
        }

        Path parent = Paths.get("..", ".env");
        if (Files.exists(parent)) {
            return parent;
        }

        return null;
    }

    private Map<String, Object> loadEnvFile(Path path) {
        Map<String, Object> map = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx <= 0) {
                    continue;
                }
                String key = line.substring(0, eqIdx).trim();
                String value = line.substring(eqIdx + 1).trim();

                if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                    if (value.length() >= 2) {
                        value = value.substring(1, value.length() - 1);
                    }
                }
                map.put(key, value);
            }
        } catch (IOException ignored) {
        }
        return map;
    }
}
