package me.skypaw.jamesBot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class TextMessages extends ListenerAdapter {
    private final List<String> commandFromSounds;

    TextMessages() throws IOException {
        /*Constructor taking list form VoiceMessages. VoiceMessages are first to create on the start,
        * and than TextMessages class is taking the list of the files created in VoiceMessages
        */

        VoiceMessages test = new VoiceMessages();
        commandFromSounds = test.soundsToCommandName;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() && !event.getAuthor().getName().equals("JamesBot")) return;
        if (!event.getChannel().getName().equals("the-grand-tour")) return;

        Guild guild = event.getGuild();
        String content = event.getMessage().getContentRaw();

        MessageChannel channel = guild.getTextChannelsByName("the-grand-tour", true).get(0);
        TextChannel channel1 = guild.getTextChannelsByName("the-grand-tour", true).get(0);

        File file = new File("src/main/resources/james_may_hello.png");

        switch (content) {
            //Hardcoded commands -> Todo to config.
            case "hi":
                channel.sendFile(file).queue();

                channel.deleteMessageById(channel.getLatestMessageId()).queueAfter(3, TimeUnit.SECONDS);


                break;
            case "!Null": {
                channel.sendMessage("Pointer exception :feelsbad: ").queue();

                String id = channel1.getLatestMessageId();
                channel.deleteMessageById(id).queue();


                break;
            }
            case "help": {
                channel.sendMessage("I need somebody, HELP, not just anybody ").queue();

                String id = channel1.getLatestMessageId();
                channel.deleteMessageById(id).queueAfter(3, TimeUnit.SECONDS);


                break;
            }
            case "bonk": {
                channel.sendMessage("Go to horny jail ").queue();

                String id = channel1.getLatestMessageId();
                channel.deleteMessageById(id).queueAfter(3, TimeUnit.SECONDS);


                break;
            }

            default:
                //Auto deleting JamesBot commands
                if (event.getAuthor().getName().equals("JamesBot")) {
                    channel.deleteMessageById(channel.getLatestMessageId()).queueAfter(3, TimeUnit.SECONDS);
                    return;
                }

                //for loop to check is there any "sounds" command, from the list in VoiceMessages

                for (String s : commandFromSounds) {
                    if (event.getMessage().getContentDisplay().equals(s)) {
                        channel.deleteMessageById(channel.getLatestMessageId()).queueAfter(3, TimeUnit.SECONDS);
                        return;
                    }
                }

                //Other messages -> 'There is no command like that, use help'
                channel.sendMessage("Nie ma takiej komedny użyj \"help\"").queue();
                channel.deleteMessageById(channel.getLatestMessageId()).queueAfter(3, TimeUnit.SECONDS);
                break;
        }

    }

}
