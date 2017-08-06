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

import com.google.gson.JsonArray;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.commands.ChannelSeniorCommand;
import ch.jamiete.hilda.commands.ChannelSubCommand;
import ch.jamiete.hilda.configuration.Configuration;
import ch.jamiete.hilda.plugins.HildaPlugin;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class VoiceOrderCommand extends ChannelSubCommand {
    HildaPlugin plugin;

    public VoiceOrderCommand(final Hilda hilda, final ChannelSeniorCommand senior, final HildaPlugin plugin) {
        super(hilda, senior);

        this.plugin = plugin;

        this.setName("order");
        this.setDescription("Sets the order that the voice channels should be returned to as the current order.");
    }

    @Override
    public void execute(final Message message, final String[] arguments, final String label) {
        final Configuration cfg = this.hilda.getConfigurationManager().getConfiguration(this.plugin, message.getGuild().getId());
        final JsonArray array = new JsonArray();

        for (final VoiceChannel channel : message.getGuild().getVoiceChannels()) {
            array.add(channel.getId());
        }

        cfg.get().add("channel_order", array);
        cfg.save();
        this.reply(message, "OK, I've set the voice channel order to the order that they're currently in.");

        if (!message.getGuild().getSelfMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            this.reply(message, "I don't have permission to move channels. Please give me the manage channels permission or I won't be able to do anything!");
        }
    }

}
