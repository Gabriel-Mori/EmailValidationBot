package com.checkbot.checkbot.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import net.dv8tion.jda.api.JDA;

import java.util.Map;

import static com.checkbot.checkbot.main.DevBot.jda;

@RestController
@RequestMapping("/callback")
public class OAuth2CallbackController {

    @Value("${discord.client-id}")
    private String clientId;

    @Value("${discord.client-secret}")
    private String clientSecret;

    @GetMapping
    public ResponseEntity<String> handleCallback(@RequestParam("code") String code) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", "http://localhost:8082/callback");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://discord.com/api/oauth2/token",
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            String accessToken = (String) response.getBody().get("access_token");

            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setBearerAuth(accessToken);
            HttpEntity<String> authRequest = new HttpEntity<>(authHeaders);

            ResponseEntity<Map> userResponse = restTemplate.exchange(
                    "https://discord.com/api/users/@me",
                    HttpMethod.GET,
                    authRequest,
                    Map.class
            );

            Map<String, Object> userInfo = userResponse.getBody();
            String email = (String) userInfo.get("email");
            String userId = (String) userInfo.get("id");

            if (email != null && email.endsWith("@grupoirrah.com")) {
                releaseAccess(userId);
                return ResponseEntity.ok("Verificação concluída com sucesso.");
            } else {
                // Obtenha o usuário pelo ID
                jda.retrieveUserById(userId).queue(user -> {
                    // Enviar mensagem privada ao usuário
                    user.openPrivateChannel().queue(channel -> {
                        channel.sendMessage("Seu e-mail não é válido. Para acessar o servidor, por favor, entre com um e-mail com o domínio @grupoirrah.com.").queue();
                    });
                });

                // Remover o usuário do servidor
                removeUser(userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("E-mail inválido. Você foi removido do servidor.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar o callback.");
        }
    }

    private void releaseAccess(String userId) {
        JDA jdaInstance = jda;
        Guild guild = jdaInstance.getGuildById("1309538302285709395");
        if (guild != null) {
            guild.removeRoleFromMember(UserSnowflake.fromId(userId), guild.getRoleById("1309575295187030167")).queue();
            guild.addRoleToMember(UserSnowflake.fromId(userId), guild.getRoleById("1309538404903420018")).queue();
        }
    }

    private void removeUser(String userId) {
        JDA jdaInstance = jda;
        Guild guild = jdaInstance.getGuildById("1309538302285709395");
        if (guild != null) {
            guild.kick(UserSnowflake.fromId(userId)).queue();
        }
    }

}