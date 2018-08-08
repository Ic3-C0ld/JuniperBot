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
package ru.caramel.juniperbot.module.ranking.listeners;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageType;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import org.springframework.beans.factory.annotation.Autowired;
import ru.caramel.juniperbot.core.listeners.DiscordEventListener;
import ru.caramel.juniperbot.core.model.DiscordEvent;
import ru.caramel.juniperbot.module.ranking.persistence.entity.RankingConfig;
import ru.caramel.juniperbot.module.ranking.service.RankingService;

@DiscordEvent
public class RankingListener extends DiscordEventListener {

    @Autowired
    private RankingService rankingService;

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (!event.getAuthor().isBot() && event.getMessage().getType() == MessageType.DEFAULT) {
            rankingService.onMessage(event);
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        taskExecutor.execute(() -> {
            RankingConfig config = rankingService.get(event.getGuild());
            if (config != null && config.isResetOnLeave()) {
                rankingService.setLevel(event.getGuild().getIdLong(), event.getMember().getUser().getId(), 0);
            }
        });
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        Member sender = event.getMember();
        if (sender == null
                || sender.getUser().isBot()
                || !event.getReactionEmote().getName().equals(RankingService.COOKIE_EMOTE)) {
            return;
        }
        Member self = event.getGuild().getSelfMember();
        TextChannel channel = event.getChannel();
        if (channel == null || !self.hasPermission(channel, Permission.MESSAGE_HISTORY)) {
            return;
        }
        channel.getMessageById(event.getMessageId()).queue(m -> {
            User author = m.getAuthor();
            if (author == null || author.isBot() || author.equals(sender.getUser())) {
                return;
            }
            Member recipient = event.getGuild().getMember(author);
            if (recipient != null) {
                rankingService.giveCookie(sender, recipient);
            }
        });
    }
}
