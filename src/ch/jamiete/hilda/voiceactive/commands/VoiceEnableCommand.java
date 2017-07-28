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

import java.util.Arrays;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.commands.ChannelSeniorCommand;
import ch.jamiete.hilda.commands.ChannelSubCommand;
import ch.jamiete.hilda.configuration.Configuration;
import ch.jamiete.hilda.plugins.HildaPlugin;
import net.dv8tion.jda.core.entities.Message;

public class VoiceEnableCommand extends ChannelSubCommand {
    HildaPlugin plugin;

    public VoiceEnableCommand(final Hilda hilda, final ChannelSeniorCommand senior, final HildaPlugin plugin) {
        super(hilda, senior);

        this.plugin = plugin;

        this.setName("enable");
        this.setAliases(Arrays.asList(new String[] { "disable" }));
        this.setDescription("Toggles the operation of the voice activity checker.");
    }

    @Override
    public void execute(final Message message, final String[] arguments, final String label) {
        final Configuration cfg = this.hilda.getConfigurationManager().getConfiguration(this.plugin, message.getGuild().getId());

        final boolean enable = label.equalsIgnoreCase("enable");

        cfg.get().addProperty("enabled", enable);
        cfg.save();

        this.reply(message, "OK, I've " + (enable ? "enabled" : "disabled") + " the voice activity checker.");

        if (cfg.get().getAsJsonArray("channel_order") == null) {
            this.reply(message, "You haven't yet set the order of the channels. You must do that before I'll start changing them.");
        }
    }

}
