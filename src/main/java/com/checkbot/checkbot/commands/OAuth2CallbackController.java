package com.checkbot.checkbot.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;


@RestController
@RequestMapping("/callback")
public class OAuth2CallbackController {

    @Autowired
    private VerificationService verificationService;

    @Value("${discord.client-id}")
    private String clientId;

    @Value("${discord.client-secret}")
    private String clientSecret;


    private final WebClient webClient = WebClient.create("https://discord.com/api");

    @GetMapping
    public ResponseEntity<String> handleCallback(@RequestParam("code") String code) {
        try {

            String tokenResponse = webClient.post()
                    .uri("/oauth2/token")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue("client_id=" + clientId +
                            "&client_secret=" + clientSecret +
                            "&grant_type=authorization_code" +
                            "&code=" + code +
                            "&redirect_uri=http://localhost:8080/callback")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            String accessToken = extractAccessToken(tokenResponse);

            String userInfo = webClient.get()
                    .uri("/users/@me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();


            String email = extractEmail(userInfo);
            String userId = extractUserId(userInfo);

            return verificationService.verifyEmail(email, userId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar o callback.");
        }
    }


    private String extractAccessToken(String tokenResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(tokenResponse);
            return node.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao extrair access_token da resposta: " + tokenResponse, e);
        }
    }

    private String extractEmail(String userInfo) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(userInfo);
            return node.get("email").asText();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao extrair e-mail da resposta: " + userInfo, e);
        }
    }

    private String extractUserId(String userInfo) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(userInfo);
            return node.get("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao extrair ID do usuário da resposta: " + userInfo, e);
        }
    }

}