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
package ch.jamiete.hilda.voiceactive.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.commands.ChannelSeniorCommand;
import ch.jamiete.hilda.commands.ChannelSubCommand;
import ch.jamiete.hilda.configuration.Configuration;
import ch.jamiete.hilda.plugins.HildaPlugin;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.requests.restaction.order.ChannelOrderAction;

public class VoiceReorderCommand extends ChannelSubCommand {
    HildaPlugin plugin;

    public VoiceReorderCommand(final Hilda hilda, final ChannelSeniorCommand senior, final HildaPlugin plugin) {
        super(hilda, senior);

        this.plugin = plugin;

        this.setName("reorder");
        this.setAliases(Arrays.asList(new String[] { "return" }));
        this.setDescription("Returns the voice channels to their proper order.");
    }

    @Override
    public void execute(final Message message, final String[] arguments, final String label) {
        final Configuration cfg = this.hilda.getConfigurationManager().getConfiguration(this.plugin, message.getGuild().getId());
        final JsonArray array = cfg.get().getAsJsonArray("channel_order");

        if (array == null) {
            this.reply(message, "You haven't yet set an order for the channels.");
            return;
        }

        if (!message.getGuild().getSelfMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            this.reply(message, "I don't have permission to move the channels.");
            return;
        }

        final List<VoiceChannel> order = new ArrayList<VoiceChannel>();
        final Iterator<JsonElement> iterator = array.iterator();

        while (iterator.hasNext()) {
            final String id = iterator.next().getAsString();
            final VoiceChannel channel = message.getGuild().getVoiceChannelById(id);

            if (channel != null) {
                order.add(channel);
            }
        }

        final ChannelOrderAction<VoiceChannel> action = message.getGuild().getController().modifyVoiceChannelPositions();

        for (int i = 0; i < order.size(); i++) {
            action.selectPosition(order.get(i)).moveTo(i);
        }

        action.queue();
        this.reply(message, "OK, the voice channels are back in their proper order.");
    }

}
