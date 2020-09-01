package me.skypaw.jamesBot;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.log4j.BasicConfigurator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;


public class Main extends ListenerAdapter {
    public static void main(String[] args) throws Exception {

        BasicConfigurator.configure();
        configCreateAndLoad();

        new JDABuilder(AccountType.BOT)
                .setToken(System.getenv("botToken"))
                .addEventListeners(new VoiceMessages())
                .addEventListeners(new TextMessages())
                .build()
                .awaitReady();

    }

    private static void configCreateAndLoad(){
        FileOutputStream fileOut = null;
        File configFile = null;

        try {
            configFile = new File("src\\main\\resources\\config.properties");
            fileOut = new FileOutputStream(configFile);
            Properties properies = new Properties();

            properies.setProperty("VoiceRoom", "Pokój");
            properies.setProperty("TextRoom", "the-grand-tour");

            properies.store(fileOut, "Properties of Bot");
            fileOut.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}