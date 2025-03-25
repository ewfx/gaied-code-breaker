package com.codebreaker.gaied.controller.service;

import com.codebreaker.gaied.entity.EmailRequest;
import com.codebreaker.gaied.entity.EmailResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmailRoutingService {
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);
    @Autowired
    EmailReaderService emailReaderService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmailRoutingService(WebClient.Builder webClientBuilder,@Value("${llm_application_base_url}") String llmUrl) {
        this.webClient = webClientBuilder.baseUrl(llmUrl).build();
    }

    public List<EmailResponse> queryMistralEmailRoute(EmailRequest emailRequest) throws JsonProcessingException {
        // Create prompt for Mistral
        String prompt = "You are a Loan Department email classifier. Classify the following emails into request type, sub-type, reasoning behind the extraction, confidence score of your extraction in percentage and if any key properties that can be extracted as key value pair. Return ONLY valid JSON with NO extra text. Do NOT include explanations or comments.###Email Name : ### "+emailRequest.getEmailName()+",### Email Body: ### "+emailRequest.getEmailBody()+" ### Email Subject : ### "+emailRequest.getEmailSubject()+" ### Email Attachment Text : ### "+emailRequest.getAttachmentText()+" JSON Output Format: [{emailName: Please keep the email name here, requestType: Please keep the request type here, requestSubType: please keep the sub request type here, reasoning: Please keep the reasoning behind the extraction, confidenceScore: Please keep the confidence score of your extraction in percentage, keyProperties: Please keep the key properties here as a key value pair}] Output strictly in valid Array JSON format as shown above.";
        prompt = prompt.replace("\r", "").replace("\n", " ");
        log.info(prompt);
        // Call Ollama API
        String response = webClient.post()
                .uri("/api/generate")
                .header("Content-Type", "application/json")
                .bodyValue("{\"model\":\"mistral\", \"prompt\":\"" + prompt + "\"}")
                .retrieve()
                .bodyToMono(String.class)
                .block(); // Blocking call to get the full response

        // Parse response JSON
        String formattedResponse = formatResponse(response);
        log.info("AI Response --> "+formattedResponse);
        JsonNode responseNode = objectMapper.readTree(response);
        String jsonResponse = responseNode.get("response").asText();

        // Convert JSON string to list of EmailResponse objects
        return objectMapper.readValue(formattedResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, EmailResponse.class));
    }

    public List<EmailResponse> classifyEmails(List<EmailRequest> emails) throws Exception {
        List<CompletableFuture<List<EmailResponse>>> futures = emails.stream().map(emailRequest -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return queryMistralEmailRoute(emailRequest);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }, executor))
                .toList();
        // Combine all futures and collect results
        List<EmailResponse> results = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join) // Get results
                        .flatMap(List::stream)
                        .collect(Collectors.toList())) // Convert to List
                .get(); // Blocking call to ensure results are available
        // Print the final combined list
        log.info("Final Results: " + results);
        return results;
    }

    public List<EmailResponse> getEmailRoute() throws Exception {
        List<EmailRequest> emailRequestList = prepare();
        List<EmailResponse> emailResponseList = classifyEmails(emailRequestList);
        emailResponseList.forEach(emailResponse -> {
            log.info("Email Response: "+emailResponse);
        });
        return emailResponseList;
    }

    public List<EmailRequest> prepare(){
        return emailReaderService.getEmailRequests();
    }

    public String queryMistral(String prompt) {
        String rawResponse = webClient.post()
                .uri("/api/generate")
                .header("Content-Type", "application/json")
                .bodyValue("{\"model\":\"mistral\", \"prompt\":\"" + prompt + "\"}")
                .retrieve()
                .bodyToMono(String.class)
                .block(); // Blocking call to get the full response
        return formatResponse(rawResponse);
    }

    private String formatResponse(String rawResponse) {
        List<String> responses = new ArrayList<>();
        try {
            // Split response into JSON lines
            String[] lines = rawResponse.split("\n");
            for (String line : lines) {
                JsonNode jsonNode = objectMapper.readTree(line);
                if (jsonNode.has("response")) {
                    responses.add(jsonNode.get("response").asText());
                }
            }
        } catch (Exception e) {
            return "Error processing response: " + e.getMessage();
        }

        return String.join("", responses).trim(); // Join responses into a single formatted string
    }

}
