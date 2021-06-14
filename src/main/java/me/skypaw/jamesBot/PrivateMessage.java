package me.skypaw.jamesBot;


import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class PrivateMessage extends ListenerAdapter {
    private String link;  //todo To config

    public PrivateMessage() {

        Properties properties = new Properties();
        try {
            File configFile = new File("src/main/resources/config.properties");
            FileInputStream fileInputStream = new FileInputStream(configFile);

            properties.load(fileInputStream);

            this.link = properties.getProperty("InviteLink");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String userName = event.getAuthor().getName();
        String content ="\u200E\n**Hey "+userName+ "** :grin: , \nif you want to see me on your server please click the link below! " +
                "\n\n When I join on your server make sure to write /\"setup/\", on any text channel\n >>> " + link;


        event.getAuthor().openPrivateChannel().queue(privateChannel -> {privateChannel.sendMessage(content).queue();});

    }

}
