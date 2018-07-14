/*
 * This file is part of JuniperBotJ.
 *
 * JuniperBotJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * JuniperBotJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JuniperBotJ. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.caramel.juniperbot.web.dao.api;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caramel.juniperbot.core.model.exception.NotFoundException;
import ru.caramel.juniperbot.core.persistence.entity.GuildConfig;
import ru.caramel.juniperbot.web.dao.AbstractDao;
import ru.caramel.juniperbot.web.dto.api.discord.GuildDto;
import ru.caramel.juniperbot.web.dto.api.discord.RoleDto;
import ru.caramel.juniperbot.web.dto.api.discord.TextChannelDto;
import ru.caramel.juniperbot.web.dto.api.discord.VoiceChannelDto;
import ru.caramel.juniperbot.web.dto.api.request.GuildInfoRequest;
import ru.caramel.juniperbot.web.security.auth.DiscordTokenServices;
import ru.caramel.juniperbot.web.security.utils.SecurityUtils;

import java.util.stream.Collectors;

@Service
public class GuildDao extends AbstractDao {

    @Autowired
    private DiscordTokenServices tokenServices;

    @Transactional
    public GuildDto getGuild(GuildInfoRequest request) {
        GuildConfig config = configService.getById(request.getId());
        if (config == null) {
            throw new NotFoundException();
        }
        GuildDto.Builder builder = GuildDto.builder()
                .name(config.getName())
                .prefix(config.getPrefix())
                .locale(config.getLocale())
                .id(String.valueOf(config.getGuildId()))
                .icon(config.getIconUrl());

        if (!discordService.isConnected()) {
            return builder.build();
        }

        Guild guild = discordService.getGuildById(request.getId());
        if (guild == null || !guild.isAvailable()) {
            return builder.build();
        }

        builder.name(guild.getName())
                .id(guild.getId())
                .icon(guild.getIconUrl())
                .available(true);

        if (CollectionUtils.isEmpty(request.getParts())
                || !SecurityUtils.isAuthenticated()
                || !tokenServices.hasPermission(guild.getIdLong())) {
            return builder.build();
        }

        for (GuildInfoRequest.PartType part : request.getParts()) {
            switch (part) {
                case ROLES:
                    builder.roles(guild.getRoles().stream()
                            .filter(e -> !e.isPublicRole() && !e.isManaged())
                            .map(e -> {
                                RoleDto dto = apiMapper.getRoleDto(e);
                                dto.setInteractable(guild.getSelfMember().canInteract(e));
                                return dto;
                            })
                            .collect(Collectors.toList()));
                    break;

                case TEXT_CHANNELS:
                    builder.textChannels(guild.getTextChannels().stream()
                        .map(e -> {
                            TextChannelDto dto = apiMapper.getTextChannelDto(e);
                            dto.setPermissions(Permission.getRaw(guild.getSelfMember().getPermissions(e)));
                            return dto;
                        }).collect(Collectors.toList()));
                    break;

                case VOICE_CHANNELS:
                    builder.voiceChannels(guild.getVoiceChannels().stream()
                            .map(e -> {
                                VoiceChannelDto dto = apiMapper.getVoiceChannelDto(e);
                                dto.setPermissions(Permission.getRaw(guild.getSelfMember().getPermissions(e)));
                                return dto;
                            }).collect(Collectors.toList()));
                    break;
            }
        }
        return builder.build();
    }
}
