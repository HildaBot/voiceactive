/*******************************************************************************
 * Copyright 2017 jamietech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ch.jamiete.hilda.voiceactive.listeners;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.configuration.Configuration;
import ch.jamiete.hilda.events.EventHandler;
import ch.jamiete.hilda.voiceactive.VoiceActivePlugin;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.requests.restaction.order.ChannelOrderAction;

public class VoiceListener {
    private final VoiceActivePlugin plugin;
    private final Hilda hilda;

    public VoiceListener(final VoiceActivePlugin plugin, final Hilda hilda) {
        this.plugin = plugin;
        this.hilda = hilda;
    }

    @EventHandler
    public void onGuildVoiceJoin(final GuildVoiceJoinEvent event) {
        this.tryModify(event.getGuild());
    }

    @EventHandler
    public void onGuildVoiceLeave(final GuildVoiceLeaveEvent event) {
        this.tryModify(event.getGuild());
    }

    @EventHandler
    public void onGuildVoiceMove(final GuildVoiceMoveEvent event) {
        this.tryModify(event.getGuild());
    }

    public void tryModify(final Guild guild) {
        if (guild == null || !guild.getSelfMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            return;
        }

        final Configuration cfg = this.hilda.getConfigurationManager().getConfiguration(this.plugin, guild.getId());

        if (!cfg.getBoolean("enabled", false)) {
            return;
        }

        final JsonArray array = cfg.get().getAsJsonArray("channel_order");

        if (array == null) {
            return;
        }

        final List<VoiceChannel> order = new ArrayList<VoiceChannel>();
        final Iterator<JsonElement> iterator = array.iterator();

        while (iterator.hasNext()) {
            final String id = iterator.next().getAsString();
            final VoiceChannel channel = guild.getVoiceChannelById(id);

            if (channel != null) {
                order.add(channel);
            }
        }

        final List<VoiceChannel> hasusers = new ArrayList<VoiceChannel>();

        for (final VoiceChannel channel : guild.getVoiceChannels()) {
            if (!channel.getMembers().isEmpty()) {
                hasusers.add(channel);
                order.remove(channel);
            }
        }

        hasusers.sort(new Comparator<VoiceChannel>() {

            @Override
            public int compare(final VoiceChannel one, final VoiceChannel two) {
                return one.getMembers().size() + two.getMembers().size();
            }

        });

        final ChannelOrderAction<VoiceChannel> action = guild.getController().modifyVoiceChannelPositions();

        for (int i = 0; i < hasusers.size(); i++) {
            final VoiceChannel channel = hasusers.get(i);

            if (guild.getAfkChannel() == channel) {
                continue;
            }

            action.selectPosition(channel).moveTo(i);
        }

        final int offset = hasusers.size();

        for (int i = 0; i < order.size(); i++) {
            final VoiceChannel channel = order.get(i);
            int position = i + offset;

            if (position > action.getCurrentOrder().size()) {
                position = action.getCurrentOrder().size();
            }

            action.selectPosition(channel).moveTo(position);
        }

        action.queue();
    }

}
