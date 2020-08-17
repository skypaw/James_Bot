package me.skypaw.jamesBot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;


public class TextMessages extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() && !event.getAuthor().getName().equals("JamesBot")) return;

        Guild guild = event.getGuild();

        Message message = event.getMessage();
        String content = message.getContentRaw();
        MessageChannel channel = guild.getTextChannelsByName("the-grand-tour", true).get(0);
        TextChannel channel1 = guild.getTextChannelsByName("the-grand-tour", true).get(0);

        File file = new File("src/main/resources/james_may_hello.png");

        if (content.equals("hi")) {
            channel.sendFile(file).queue();

            channel.deleteMessageById(channel.getLatestMessageId()).queueAfter(3, TimeUnit.SECONDS);


        } else if (content.equals("!Null")) {
            channel.sendMessage("Pointer exception :feelsbad: ").queue();

            String id = channel1.getLatestMessageId();
            channel.deleteMessageById(id).queue();


        } else if (content.equals("help")) {
            channel.sendMessage("I need somebody, HELP, not just anybody ").queue();

            String id = channel1.getLatestMessageId();
            channel.deleteMessageById(id).queueAfter(3, TimeUnit.SECONDS);


        } else if (content.equals("bonk")) {
            channel.sendMessage("Go to horny jail ").queue();

            String id = channel1.getLatestMessageId();
            channel.deleteMessageById(id).queueAfter(3, TimeUnit.SECONDS);


        }else {
            channel.deleteMessageById(channel.getLatestMessageId()).queueAfter(3, TimeUnit.SECONDS);
        }

    }
}