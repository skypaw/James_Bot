package me.skypaw.jamesBot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;


public class TextMessages extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        Guild guild = event.getGuild();

        Message message = event.getMessage();
        String content = message.getContentRaw();
        MessageChannel channel = guild.getTextChannelsByName("the-grand-tour", true).get(0);

        File file = new File("src/main/resources/james_may_hello.png");

        if (content.equals("hi")) {
            channel.sendFile(file).queue();
        } else if (content.equals("!Null")) {
            channel.sendMessage("Pointer exception").queue();
        }
    }
}