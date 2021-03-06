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
package ru.juniperbot.common.worker.event.listeners;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import ru.juniperbot.common.worker.event.service.ContextService;
import ru.juniperbot.common.worker.modules.audit.service.AuditService;
import ru.juniperbot.common.worker.shared.service.DiscordEntityAccessor;

public abstract class DiscordEventListener extends ListenerAdapter {

    @Autowired
    protected TaskExecutor taskExecutor;

    @Autowired
    protected ContextService contextService;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected DiscordEntityAccessor entityAccessor;

    private AuditService auditService;

    protected AuditService getAuditService() {
        if (auditService == null) {
            auditService = applicationContext.getBean(AuditService.class);
        }
        return auditService;
    }
}
