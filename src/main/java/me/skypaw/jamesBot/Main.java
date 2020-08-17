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
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;


public class Main extends ListenerAdapter {
    public static void main(String[] args) throws Exception {
        new JDABuilder(AccountType.BOT)
                .setToken(System.getenv("botToken"))
                .addEventListeners(new Main())
                .build();
        JDA api = JDABuilder.createDefault(System.getenv("botToken")).addEventListeners(new TextMessages())
                .build().awaitReady();

    }

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    private Main() throws IOException {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(playerManager);


        listFiles();
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
        if (event.getMember().getUser().getName().equals("Hydra")||event.getMember().getUser().getName().equals("JamesBot")) return;

        String hello = createDirectoryString("Hello", "m4a");

        loadAndPlay(event.getChannelJoined(), hello);

    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        event.getChannelLeft().getUserLimit();

        int i = 0;
        if (i == 0 ){
            event.getGuild().getAudioManager().closeAudioConnection();
        }


    }


    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String[] command = event.getMessage().getContentRaw().split(" ", 2);

        String hello = createDirectoryString("Hello", "m4a"); // TODO - reading sources automatically
        String bonk = createDirectoryString("bonk", "mp3"); // TODO - reading sources automatically

        String hammond = "sounds/hammond.mp3"; // TODO - reading sources automatically


        if ("~play".equals(command[0]) && command.length == 2) {
            loadAndPlay(event.getChannel(), command[1]);

        } else if ("hi".equals(command[0])) {
            loadAndPlay(event.getChannel(), hello); //todo - create automatic commands based on file name

        } else if ("hammond".equals(command[0])) {
            loadAndPlay(event.getChannel(), hammond);

        }else if ("bonk".equals(command[0])) {
            loadAndPlay(event.getChannel(), bonk);

        }

        super.onGuildMessageReceived(event);
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

    private String createDirectoryString(String titleString, String format) {
        StringBuilder builder = new StringBuilder().append("sounds/").append(titleString).append(".").append(format);
        return builder.toString();
    }

    private void listFiles() throws IOException {

        try (Stream<Path> paths = Files.walk(Paths.get("sounds/"))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(System.out::println);

        }

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
}