package me.skypaw.jamesBot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.util.stream.Stream;


public class Main extends ListenerAdapter {
    public static void main(String[] args) throws Exception {
        new JDABuilder(AccountType.BOT)
                .setToken(System.getenv("botToken"))
                .addEventListeners(new Main())
                .build()
                .awaitReady();

        JDA jda = JDABuilder
                .createDefault(System.getenv("botToken"))
                .addEventListeners(new TextMessages())
                .build()
                .awaitReady();

    }


    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;


    Main() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(playerManager);

    }


    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (!event.getChannelJoined().getName().equals("Pokój")) return;
        if (event.getMember().getUser().getName().equals("Hydra") || event.getMember().getUser().getName().equals("JamesBot"))
            return;

        String hello = createDirectoryString("hi", "m4a", "sounds");

        try {
            Thread.sleep(600);
            loadAndPlay(event.getChannelJoined(), hello);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {

        if (event.getChannelLeft().getName().equals("Pokój") && event.getChannelLeft().getMembers().size() == 1) {
            event.getGuild().getAudioManager().closeAudioConnection();
        } else if (!event.getChannelLeft().getName().equals("Pokój") && event.getGuild().getVoiceChannelsByName("Pokój", true).get(0).getMembers().size() == 1) {
            event.getGuild().getAudioManager().closeAudioConnection();
        }

    }


    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (!event.getChannel().getName().equals("the-grand-tour")) return;
        if (event.getAuthor().getName().equals("JamesBot")) return;
        if (event.getMessage().getContentDisplay().equals("random")) {

            Runnable runnable = () -> {
                while (true) {
                    try {
                        int timeThread = randomPlayer(event.getChannel());

                        Thread.sleep(timeThread);


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            Thread t = new Thread(runnable);
            t.start();


        }


        ArrayList<String> list = null;

        try {
            list = listFiles("sounds");


        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] command = event.getMessage().getContentRaw().split(" ", 2);

        assert list != null;

        String separator = "\\\\";
        String separator1 = "\\.";
        for (String s : list) {
            String[] parts = s.toLowerCase().split(separator);
            String[] parts2 = parts[1].split(separator1);

            if (parts2[0].equals(command[0].toLowerCase())) {
                loadAndPlay(event.getChannel(), createDirectoryString(parts2[0], parts2[1], "sounds"));
            }
        }

        super.onGuildMessageReceived(event);
    }

    private void loadAndPlay(final GuildChannel channel, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                play(channel.getGuild(), musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                play(channel.getGuild(), musicManager, firstTrack);
            }

            @Override
            public void noMatches() {
            }

            @Override
            public void loadFailed(FriendlyException exception) {
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        connectToFirstVoiceChannel(guild.getAudioManager());

        musicManager.scheduler.queue(track);
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannelsByName("Pokój", true)) {
                audioManager.openAudioConnection(voiceChannel);
                break;
            }
        }
    }

    private String createDirectoryString(String titleString, String format, String directory) {
        StringBuilder builder = new StringBuilder().append(directory).append("/").append(titleString).append(".").append(format);
        return builder.toString();
    }

    private ArrayList<String> listFiles(String directory) throws IOException {

        ArrayList<String> list = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach((temp) -> {
                        list.add(temp.toString());
                    });
        }

        return list;
    }

    public int randomPlayer(GuildChannel voiceChannel) {
        Random test = new Random();

        ArrayList<String> list = null;

        try {
            list = listFiles("random");


        } catch (IOException e) {
            e.printStackTrace();
        }

        assert list != null;

        int randomSound = test.nextInt(list.size());

        String randomSoundString = String.valueOf(randomSound);

        String separator = "\\\\";
        String separator1 = "\\.";
        for (String s : list) {
            String[] parts = s.split(separator);
            String[] parts2 = parts[1].split(separator1);

            if (parts2[0].equals(randomSoundString)) {

                loadAndPlay(voiceChannel, createDirectoryString(parts2[0], parts2[1], "random"));
            }
        }


        int min = 1000 * 60 * 10;
        int randomTime = test.nextInt(25)*1000 * 60   + min;

        System.out.println(randomTime/60/1000);

        return randomTime;

    }


}