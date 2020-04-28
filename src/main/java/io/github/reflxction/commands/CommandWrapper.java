/*
 * * Copyright 2019-2020 github.com/ReflxctionDev
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
 */
package io.github.reflxction.commands;

import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * An object wrapper for {@link CommandCallback} which is annotated by {@link PluginSubcommand}
 */
public class CommandWrapper extends SubcommandInvokation {

    private CommandCallback callback;

    public CommandWrapper(String name, String description, String parameters, String[] aliases, List<String> helpMenu, String permission, PermissionDefault permissionAccess, int minimumArgs, boolean requirePlayer, String tab, CommandCallback callback) {
        super(name, description, parameters, aliases, helpMenu, permission, permissionAccess, minimumArgs, requirePlayer, tab);
        this.callback = callback;
    }

    @Override
    public void invoke(CommandContext context) {
        callback.onProcess(context);
    }
}