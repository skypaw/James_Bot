package me.skypaw.jamesBot;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.log4j.BasicConfigurator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class Main extends ListenerAdapter {
    public static void main(String[] args) throws Exception {

        BasicConfigurator.configure();
        configLoad();

        new JDABuilder(AccountType.BOT)
                .setToken(System.getenv("botToken"))
                .addEventListeners(new VoiceMessages())
                .addEventListeners(new TextMessages())
                .build()
                .awaitReady();

    }

    private static void configLoad() {
        FileOutputStream fileOut;
        File configFile = new File("src\\main\\resources\\config.properties");

        List<String> basicPropertiesContent = new ArrayList<>();
        basicPropertiesContent.add("VoiceRoom");
        basicPropertiesContent.add("TextRoom");

        List<String> basicPropertiesValues = new ArrayList<>();
        basicPropertiesValues.add("Pokój");
        basicPropertiesValues.add("the-grand-tour");


        if (!configFile.exists()) {
            try {
                fileOut = new FileOutputStream(configFile);
                Properties properies = new Properties();

                properies.setProperty(basicPropertiesContent.get(0), basicPropertiesValues.get(0));
                properies.setProperty(basicPropertiesContent.get(1), basicPropertiesValues.get(1));

                properies.store(fileOut, "Properties of Bot");
                fileOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            try {
                List<String> contentPropertiesToCompare = Files.readAllLines(Paths.get("src\\main\\resources\\config.properties"));
                System.out.println(contentPropertiesToCompare);

                int i = 0;

                for (String s : contentPropertiesToCompare) {
                    String[] splitContent = s.split("=");
                    System.out.println(splitContent[0]);

                    if (splitContent[0].charAt(0) != '#') {
                        if (basicPropertiesContent.get(i).equals(splitContent[0])) {
                            System.out.println(true);
                        }
                        i++;
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}