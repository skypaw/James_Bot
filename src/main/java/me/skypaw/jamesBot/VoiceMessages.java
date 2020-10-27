package me.skypaw.jamesBot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VoiceMessages extends ListenerAdapter {

    private static Logger logger = LoggerFactory.getLogger(VoiceMessages.class.getName());

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    private Thread t = null;

    private final ArrayList<String> soundsToCommand;
    private final ArrayList<String> randomSounds;

    private final List<String> randomSoundsName;
    final List<String> soundsToCommandName;


    private String randomDirectory;
    private String soundsDirectory;
    String voiceChannelConfig;
    private String greetingSource;

    String textChannelConfig;
    Properties properties;


    VoiceMessages() throws IOException {
        logger.info("Starting VoiceMessages");

        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(playerManager);

        properties = new Properties();
        try {
            File configFile = new File("src\\main\\resources\\config.properties");
            FileInputStream fileInputStream = new FileInputStream(configFile);


            properties.load(fileInputStream);

            this.voiceChannelConfig = properties.getProperty("VoiceRoom");
            this.textChannelConfig = properties.getProperty("TextRoom");

            this.randomDirectory = properties.getProperty("RandomSoundDirectory");
            this.soundsDirectory = properties.getProperty("VoiceCommandsDirectory");
            this.greetingSource = properties.getProperty("GreetingMessageDirectory");


        } catch (Exception e) {
            e.printStackTrace();
        }


        //Listing files on the start
        this.soundsToCommand = listFiles(soundsDirectory);
        this.randomSounds = listFiles(randomDirectory);


        //Dividing directory strings
        this.randomSoundsName = stringDirectorySeparator(randomSounds);
        this.soundsToCommandName = stringDirectorySeparator(soundsToCommand);
    }


    /**
     * LIST THE FILES
     * Method responsible for creating the list of sounds in 'random' and 'sounds' directory.
     */

    private ArrayList<String> listFiles(String directory) throws IOException {
        ArrayList<String> list = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach((temp) -> list.add(temp.toString()));
        }

        return list;
    }

    /**
     * DIVIDING DIRECTORY STRINGS
     * Method responsible for finding the name of the file, and creating the list of filenames to use it as commands for
     * playing the sounds.
     */

    private List<String> stringDirectorySeparator(ArrayList<String> list) {
        String separator = "\\\\";
        String separator1 = "\\.";
        List<String> name = new ArrayList<>();

        for (String s : list) {
            String[] parts = s.toLowerCase().split(separator);
            String[] parts2 = parts[1].split(separator1);

            name.add(parts2[0]);
        }

        return name;
    }


    /**
     * JOIN
     * Event on joining the channel by user. When is connected the bot plays welcome message.
     */

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (!event.getChannelJoined().getName().equals(voiceChannelConfig)) return;
        if (event.getMember().getUser().isBot())
            return;

        String hello = greetingSource;

        try {
            Thread.sleep(600);
            loadAndPlay(event.getChannelJoined(), hello);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * LEAVE
     * Event on leaving the channel by user. When the only user connected is bot on the Discord Guild he leave the
     * channel and stop playing random sounds (if turned on)
     */

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getChannelLeft().getName().equals(voiceChannelConfig) && event.getChannelLeft().getMembers().size() == 1) {
            event.getGuild().getAudioManager().closeAudioConnection();
            stopRandomThread();
        } else if (!event.getChannelLeft().getName().equals(voiceChannelConfig) && event.getGuild().getVoiceChannelsByName(voiceChannelConfig, true).get(0).getMembers().size() == 1) {
            event.getGuild().getAudioManager().closeAudioConnection();
            stopRandomThread();
        }

    }

    /**
     * TEXT COMMAND
     * Event on getting text command to the bot text-channel. It is responsible for playing sounds by command,
     * and random sounds.
     */

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (!event.getChannel().getName().equals(textChannelConfig)) return;
        if (event.getAuthor().getName().equals(voiceChannelConfig)) return;
        if (event.getMessage().getContentDisplay().equals("random")) {
            randomThread(event);
        }
        if (event.getMessage().getContentDisplay().equals("stoprandom")) {
            stopRandomThread();
        }

        String[] command = event.getMessage().getContentRaw().split(" ", 2);
        int i = 0;
        for (String s : soundsToCommandName) {
            if (s.equals(command[0].toLowerCase())) {
                loadAndPlay(event.getChannel(), soundsToCommand.get(i));
            }
            i++;
        }

        super.onGuildMessageReceived(event);
    }


    // Method from LavaPlayer
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

    // Method from LavaPlayer
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

    // Method from LavaPlayer
    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        connectToFirstVoiceChannel(guild.getAudioManager());

        musicManager.scheduler.queue(track);
    }


    // Method responsible for connecting bot to the channel
    private void connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect() && !audioManager.getGuild().getVoiceChannelsByName("Pokój", true).isEmpty()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannelsByName(voiceChannelConfig, true)) {
                audioManager.openAudioConnection(voiceChannel);
                break;
            }
        }
    }


    /**
     * RANDOM
     * Methods responsible for playing random sounds from dictionary "random"
     * Sounds will be waiting to queue in new thread, so the main functionality will be available.
     */

    public int randomPlayer(GuildChannel voiceChannel) {
        Random random = new Random();

        int randomSound = random.nextInt(randomSounds.size());

        int i = 0;
        for (String ignored : randomSoundsName) {
            if (i == randomSound) {
                loadAndPlay(voiceChannel, randomSounds.get(i));
            }
            i++;
        }

        int min = 60 * 1000 * 12;
        int randomTime = random.nextInt(23) * 1000 * 60 + min;

        return randomTime;

    }


    private void randomThread(GuildMessageReceivedEvent event) {

        //Infinite loop to adding to play queue random sounds, and sleeping the thread for specific pseudo random time.
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

        //Statement to avoid creating many threads with random sounds
        if (t == null) {
            t = new Thread(runnable);
            t.start();
        }

    }

    private void stopRandomThread() {

        //Stopping the thread, and changing t to null so it can be launch again
        if (t != null) {
            t.stop();
            t = null;
        }

    }

}

