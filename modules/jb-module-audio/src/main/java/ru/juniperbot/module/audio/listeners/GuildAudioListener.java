/*
 * This file is part of JuniperBot.
 *
 * JuniperBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * JuniperBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JuniperBot. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.juniperbot.module.audio.listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.juniperbot.common.persistence.entity.MusicConfig;
import ru.juniperbot.common.service.MusicConfigService;
import ru.juniperbot.common.worker.event.DiscordEvent;
import ru.juniperbot.common.worker.event.listeners.DiscordEventListener;
import ru.juniperbot.common.worker.feature.service.FeatureSetService;
import ru.juniperbot.module.audio.service.PlayerService;

import java.util.Objects;

@DiscordEvent
public class GuildAudioListener extends DiscordEventListener {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private MusicConfigService musicConfigService;

    @Autowired
    private FeatureSetService featureSetService;

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        playerService.stop(null, event.getGuild());
    }

    @Override
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        if (playerService.isActive(event.getGuild()) &&
                Objects.equals(event.getChannel(), playerService.getConnectedChannel(event.getGuild()))) {
            playerService.stop(null, event.getGuild());
        }
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        tryAutoPlay(event.getMember(), event.getGuild(), event.getChannelJoined());
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        tryAutoPlay(event.getMember(), event.getGuild(), event.getChannelJoined());
    }

    private void tryAutoPlay(Member member, Guild guild, VoiceChannel joinedChannel) {
        if (member.getUser().isBot()
                || !featureSetService.isAvailable(guild)
                || playerService.isActive(guild)) {
            return;
        }
        contextService.withContextAsync(guild, () -> {
            MusicConfig config = musicConfigService.get(guild);
            if (config == null) {
                return;
            }

            VoiceChannel targetChannel = null;
            if (config.getChannelId() != null) {
                targetChannel = guild.getVoiceChannelById(config.getChannelId());
            }
            if (!hasPermission(targetChannel)) {
                for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
                    if (hasPermission(voiceChannel)) {
                        targetChannel = voiceChannel;
                        break;
                    }
                }
            }

            if (joinedChannel.equals(targetChannel)
                    && countListeners(targetChannel) < 2
                    && StringUtils.isNotEmpty(config.getAutoPlay())) {
                TextChannel channel = null;
                if (config.getTextChannelId() != null) {
                    channel = guild.getTextChannelById(config.getTextChannelId());
                }

                if (!hasPermission(channel)) {
                    for (TextChannel textChannel : guild.getTextChannels()) {
                        if (hasPermission(textChannel)) {
                            channel = textChannel;
                            break;
                        }
                    }
                }

                if (channel != null) {
                    playerService.loadAndPlay(channel, member, config.getAutoPlay());
                }
            }
        });
    }

    private long countListeners(VoiceChannel channel) {
        if (channel == null) {
            return 0;
        }
        Member self = channel.getGuild().getSelfMember();
        return channel.getMembers().stream()
                .filter(e -> !self.equals(e))
                .count();
    }

    private boolean hasPermission(TextChannel channel) {
        return channel != null && channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE,
                Permission.MESSAGE_EMBED_LINKS);
    }

    private boolean hasPermission(VoiceChannel channel) {
        return channel != null && channel.getGuild().getSelfMember().hasPermission(channel, Permission.VOICE_CONNECT,
                Permission.VOICE_SPEAK);
    }
}
