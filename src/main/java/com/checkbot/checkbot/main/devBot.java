package com.checkbot.checkbot.main;
import io.github.cdimascio.dotenv.Dotenv;

import com.checkbot.checkbot.CheckbotApplication;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import com.checkbot.checkbot.commands.BotEventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class devBot {

    @Bean
    public JDA jda(BotEventListener botEventListener) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("DISCORD_BOT_TOKEN");
        return JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setActivity(Activity.playing("validating email"))
                .addEventListeners(botEventListener)
                .build();
    }

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(CheckbotApplication.class, args);

        System.out.println("Bot is running!");
    }
}

