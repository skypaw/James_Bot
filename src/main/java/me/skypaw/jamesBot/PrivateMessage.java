package me.skypaw.jamesBot;


import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PrivateMessage extends ListenerAdapter {
    private String link = "https://discord.com/api/oauth2/authorize?client_id=743498183950204948&permissions=37088336&scope=bot"; //todo To config

    public PrivateMessage() {
    }


    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String userName = event.getAuthor().getName();
        String content ="\u200E\n**Hey "+userName+ "** :grin: , \nif you want to see me on your server please click the link below! " +
                "\n\n When I join on your server make sure to write \\\"setup\\\", on any text channel\n >>> " + link;


        event.getAuthor().openPrivateChannel().queue(privateChannel -> {privateChannel.sendMessage(content).queue();});

    }

}
