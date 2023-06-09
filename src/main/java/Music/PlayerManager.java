package Music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    public PlayerManager() {
        musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(playerManager);
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    public synchronized GuildMusicManager getGuildMusicManager(Guild guild){
        long guildId =guild.getIdLong();
        GuildMusicManager musicManager= musicManagers.get(guildId);

        if(musicManager==null){
            musicManager= new GuildMusicManager(playerManager);
            musicManagers.put(guildId,musicManager);
        }
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        return musicManager;
    }

    public void loadAndPlay(final TextChannel channel, String trackUrl){
        final GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());
        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                channel.sendMessage("Funking  "+audioTrack.getInfo().title+" right up!!").queue();
                play(musicManager,audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                AudioTrack firstTrack = audioPlaylist.getSelectedTrack();

                if(firstTrack==null) {
                    firstTrack = audioPlaylist.getTracks().get(0);
                }
                channel.sendMessage("Hot choice!! adding "+ firstTrack.getInfo().title+ " to the queue ").queue();

                play(musicManager,firstTrack);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("You're killing my buzz I can't find that shit").queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                channel.sendMessage("The funk level is too low, I refuse to play this").queue();
            }
        });
    }

    private void play (GuildMusicManager musicManager, AudioTrack track){
        musicManager.scheduler.queue(track);
    }

    public static PlayerManager getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }
}
