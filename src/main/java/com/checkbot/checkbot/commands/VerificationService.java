package com.checkbot.checkbot.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class VerificationService {

    private static final String GUILD_ID = "1309538302285709395";
    private static final String VALID_EMAIL_DOMAIN = "@grupoirrah.com";

    private final JDA jda;

    public VerificationService(JDA jda) {
        this.jda = jda;
    }

    public ResponseEntity<String> verifyEmail(String email, String userId) {
        if (isValidEmail(email)) {
            Guild guild = jda.getGuildById(GUILD_ID);
            if (guild == null) {
                return ResponseEntity.badRequest().body("Servidor não encontrado.");
            }

           Member member =  guild.retrieveMemberById(userId).complete();
           Role role = guild.getRoleById("1309575295187030167");
            guild.addRoleToMember(member, role).queue();

            grantPrivateChannelAccess(member);

            return ResponseEntity.ok("Verificação concluída com sucesso");
        } else {
            removeUser(userId);
            return ResponseEntity.badRequest().body("O e-mail fornecido não é válido. Por favor, utilize um e-mail com o domínio '" + VALID_EMAIL_DOMAIN + "'.");
        }
    }

    private void grantPrivateChannelAccess(Member member) {
        Guild guild = member.getGuild();
        TextChannel privateChannel = guild.getTextChannelById("1309538303384748143");

        if (privateChannel != null) {
            privateChannel.getManager()
                    .putMemberPermissionOverride(
                            member.getIdLong(),
                            java.util.EnumSet.of(net.dv8tion.jda.api.Permission.VIEW_CHANNEL),
                            null
                    ).queue();
        } else {
            System.err.println("Canal privado não encontrado!");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.endsWith(VALID_EMAIL_DOMAIN);
    }

    private void removeUser(String userId) {
        Guild guild = jda.getGuildById(GUILD_ID);
        if (guild == null) {
            System.err.println("Guild não encontrado. Verifique o ID do servidor.");
            return;
        }


        jda.retrieveUserById(userId).queue(
                user -> kickUser(guild, user),
                error -> System.err.println("Usuário não encontrado. Não foi possível removê-lo.")
        );
    }

    private void kickUser(Guild guild, User user) {
        user.openPrivateChannel().queue(channel ->
                channel.sendMessage(
                        "Olá, percebemos que você não possui uma conta com o domínio `" + VALID_EMAIL_DOMAIN + "`. " +
                                "Por favor, crie uma conta com esse domínio e tente novamente acessar o servidor."
                ).queue(
                        success -> guild.kick(UserSnowflake.fromId(user.getId())).queue(),
                        failure -> {
                            System.err.println("Não foi possível enviar mensagem privada ao usuário. Prosseguindo com o kick.");
                            guild.kick(UserSnowflake.fromId(user.getId())).queue();
                        }
                )
        );
    }
}

