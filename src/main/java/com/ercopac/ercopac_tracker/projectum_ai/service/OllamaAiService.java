package com.ercopac.ercopac_tracker.projectum_ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class OllamaAiService {

    @Value("${projectum.ai.url}")
    private String ollamaUrl;

    @Value("${projectum.ai.model}")
    private String model;

    @Value("${projectum.ai.max-tokens:80}")
    private int maxTokens;

    @Value("${projectum.ai.keep-alive:30m}")
    private String keepAlive;

    private final ObjectMapper mapper;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    public OllamaAiService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String ask(String prompt) {
        StringBuilder answer = new StringBuilder();
        stream(prompt, answer::append);
        return answer.toString().trim();
    }

    public void stream(String prompt, Consumer<String> onToken) {
        try {
            String body = mapper.writeValueAsString(Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", true,
                    "keep_alive", keepAlive,
                    "options", Map.of("num_predict", maxTokens, "num_ctx", 2048, "temperature", 0.1)
            ));
            HttpRequest request = HttpRequest.newBuilder(URI.create(ollamaUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<java.io.InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                response.body().close();
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Ollama model is unavailable");
            }
            try (var reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    JsonNode event = mapper.readTree(line);
                    if (event.hasNonNull("error")) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Ollama model error");
                    String token = event.path("response").asText("");
                    if (!token.isEmpty()) onToken.accept(token);
                    if (event.path("done").asBoolean(false)) break;
                }
            }
        } catch (ConnectException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Ollama is offline", e);
        } catch (java.net.http.HttpTimeoutException e) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Ollama timed out", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI request interrupted", e);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to reach Ollama", e);
        }
    }
}
