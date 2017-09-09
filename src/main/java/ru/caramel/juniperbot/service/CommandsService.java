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
package ru.caramel.juniperbot.service;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import ru.caramel.juniperbot.commands.Command;
import ru.caramel.juniperbot.commands.model.CommandGroup;
import ru.caramel.juniperbot.commands.model.DiscordCommand;
import ru.caramel.juniperbot.model.CustomCommandDto;

import java.util.List;
import java.util.Map;

public interface CommandsService {

    void onMessageReceived(MessageReceivedEvent event);

    Map<String, Command> getCommands();

    Map<CommandGroup, List<DiscordCommand>> getDescriptors();

    Command getByLocale(String localizedKey);

    Command getByLocale(String localizedKey, boolean anyLocale);

    void saveCommands(List<CustomCommandDto> commands, long serverId);
}