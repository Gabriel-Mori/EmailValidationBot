package com.checkbot.checkbot.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.springframework.stereotype.Component;


@Component
public class BotEventListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        User user = event.getUser();

        user.openPrivateChannel().queue(channel ->
                channel.sendMessage("Bem-vindo! Para acessar o servidor, por favor, autentique-se usando este link: " +
                        "https://discord.com/oauth2/authorize?client_id=1309177195393978470&response_type=code&redirect_uri=http%3A%2F%2Flocalhost%3A8082%2Fcallback&scope=email+identify").queue()
        );

        event.getGuild().addRoleToMember(UserSnowflake.fromId(user.getId()), event.getGuild().getRoleById("ID_DO_ROLE_RESTRITO")).queue();
    }
}

