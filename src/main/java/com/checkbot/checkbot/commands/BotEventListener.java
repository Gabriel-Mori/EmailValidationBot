package com.checkbot.checkbot.commands;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;


@Component
public class BotEventListener extends ListenerAdapter {

    private static final String CHANNEL_ID = "1309538302885498913";

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        TextChannel channel = event.getGuild().getTextChannelById(CHANNEL_ID);
        if (channel != null) {
            channel.sendMessage(
                    "Bem-vindo ao servidor, " + event.getUser().getAsMention() +
                            "! Para acessar o servidor, clique no botão abaixo para verificar seu e-mail."
            ).setActionRow(
                    Button.link(
                            "https://discord.com/oauth2/authorize?client_id=1309177195393978470&response_type=code&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fcallback&scope=identify+email",
                            "Verificar e-mail"
                    )
            ).queue();
        } else {
            System.err.println("Canal não encontrado! Verifique o ID do canal.");
        }
    }
}

