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
package ru.juniperbot.api.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.juniperbot.api.dto.playlist.PlaylistDto;
import ru.juniperbot.common.persistence.entity.Playlist;
import ru.juniperbot.common.service.PlaylistService;

@Service
public class PlaylistDao extends AbstractDao {

    @Autowired
    private GuildDao guildDao;

    @Autowired
    private PlaylistService playlistService;

    @Transactional
    public PlaylistDto get(String uuid, boolean withGuild) {
        Playlist playlist = playlistService.getPlaylist(uuid);
        if (playlist == null) {
            return null;
        }
        PlaylistDto dto = apiMapper.getPlaylistDto(playlist);
        if (withGuild) {
            dto.setGuild(guildDao.getGuild(playlist.getGuildId()));
        }
        return dto;
    }
}
