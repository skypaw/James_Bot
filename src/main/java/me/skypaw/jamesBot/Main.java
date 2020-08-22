package me.skypaw.jamesBot;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;



public class Main extends ListenerAdapter {
    public static void main(String[] args) throws Exception {
        new JDABuilder(AccountType.BOT)
                .setToken(System.getenv("botToken"))
                .addEventListeners(new VoiceMessages())
                .addEventListeners(new TextMessages())
                .build()
                .awaitReady();

    }

}