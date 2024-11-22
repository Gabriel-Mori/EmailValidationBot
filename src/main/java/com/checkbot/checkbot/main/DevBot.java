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


public class DevBot {

    public static JDA jda;

    public static void main(String[] args) throws Exception {

        ApplicationContext context = SpringApplication.run(CheckbotApplication.class, args);

        BotEventListener botEventListener = context.getBean(BotEventListener.class);
        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("DISCORD_BOT_TOKEN");
     jda = JDABuilder
             .createDefault(token)
             .enableIntents(GatewayIntent.GUILD_MEMBERS)
             .setActivity(Activity.playing("validating email"))
             .addEventListeners(botEventListener)
             .build();
    }
}
