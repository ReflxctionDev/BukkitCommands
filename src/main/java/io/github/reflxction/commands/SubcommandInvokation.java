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

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * Represents a method or class that handles a subcommand
 */
public abstract class SubcommandInvokation {

    public final String name, description, parameters;
    public final String[] aliases;
    public final List<String> helpMenu;
    public Permission permission;
    public final int minimumArgs;
    public final boolean requirePlayer;
    public final String tab;

    public SubcommandInvokation(String name,
                                String description,
                                String parameters,
                                String[] aliases,
                                List<String> helpMenu,
                                String permission,
                                PermissionDefault permissionAccess,
                                int minimumArgs,
                                boolean requirePlayer,
                                String tab) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.aliases = aliases;
        this.helpMenu = helpMenu;
        if (!permission.equals(PluginSubcommand.NO_PERMISSION))
            this.permission = new Permission(name, permission);
        this.minimumArgs = minimumArgs;
        this.requirePlayer = requirePlayer;
        this.tab = tab;
    }

    public abstract void invoke(CommandContext context);

    /**
     * Checks whether does the sender have the permission to use this
     *
     * @param sender Sender to check for
     * @return True if the sender has the permission, false if otherwise.
     */
    public boolean hasPermission(CommandSender sender) {
        return permission == null || sender.hasPermission(permission);
    }

}
