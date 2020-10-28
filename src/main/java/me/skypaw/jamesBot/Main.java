package me.skypaw.jamesBot;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.log4j.BasicConfigurator;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class Main extends ListenerAdapter {

    public static void main(String[] args) throws LoginException, IOException, InterruptedException {

        BasicConfigurator.configure();
        configLoad();


        JDA api = JDABuilder.createDefault(System.getenv("botToken")).addEventListeners(new VoiceMessages()).addEventListeners(new TextMessages())
                .addEventListeners(new PrivateMessage()).build().awaitReady();


        System.out.println("Bot is ready");
    }

    private static void configLoad() throws IOException {

        File configFile = new File("src\\main\\resources\\config.properties");
        InputStream inputStream = new FileInputStream("src\\main\\resources\\config.properties");

        Properties properties = new Properties();
        properties.load(inputStream);

        List<String> basicPropertiesContent = new ArrayList<>();
        basicPropertiesContent.add("VoiceRoom");
        basicPropertiesContent.add("TextRoom");
        basicPropertiesContent.add("GreetingMessageDirectory");
        basicPropertiesContent.add("RandomSoundDirectory");
        basicPropertiesContent.add("VoiceCommandsDirectory");
        basicPropertiesContent.add("InviteLink");

        List<String> basicPropertiesValues = new ArrayList<>();
        basicPropertiesValues.add("Pokój");
        basicPropertiesValues.add("the-grand-tour");
        basicPropertiesValues.add("sounds\\hi.m4a");
        basicPropertiesValues.add("random");
        basicPropertiesValues.add("sounds");
        basicPropertiesValues.add("");


        if (!configFile.exists()) {
            createConfig(configFile, basicPropertiesContent, basicPropertiesValues, properties);
        } else {
            for (String s : basicPropertiesContent) {
                if (!properties.containsKey(s)) {
                    createConfig(configFile, basicPropertiesContent, basicPropertiesValues, properties); //If there is lack of parameters (keys) in the config -> creating config one more time
                }
            }
        }
    }


    private static void createConfig(File configFile, List<String> basicPropertiesContent, List<String> basicPropertiesValues, Properties properties) {
        try {
            FileOutputStream fileOut = new FileOutputStream(configFile);

            int i = 0;
            for (String s : basicPropertiesContent) {
                properties.setProperty(s, basicPropertiesValues.get(i));
                i++;
            }
            properties.store(fileOut, "Properties of Bot");
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}