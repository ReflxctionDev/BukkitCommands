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

import io.github.reflxction.commands.CommandCallback.CommandCallbackException;
import io.github.reflxction.commands.CommandResolvers.Resolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class MethodSubcommand extends SubcommandInvokation {

    protected final Method method;
    protected final Object instance;

    public MethodSubcommand(Method method, Object instance, String name, String description, String parameters, String[] aliases, List<String> helpMenu, String permission, PermissionDefault permissionAccess, int minimumArgs, boolean requirePlayer, String tab) {
        super(name, description, parameters, aliases, helpMenu, permission, permissionAccess, minimumArgs, requirePlayer, tab);
        this.method = method;
        method.setAccessible(true);
        this.instance = instance;
    }

    @Override
    public void invoke(CommandContext context) {
        Class<?>[] parameters = method.getParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        switch (method.getParameterCount()) {
            case 0:
                callback();
                break;
            case 1: {
                Class<?> first = parameters[0];
                if (CommandContext.class.isAssignableFrom(first)) callback(context);
                else if (Player.class.isAssignableFrom(first)) callback(context.player());
                else if (CommandSender.class.isAssignableFrom(first)) callback(context.getSender());
                else {
                    Resolver<?> resolver = context.getCommandHandler().getResolvers().get(first);
                    if (resolver != null) callback(resolver.resolve(context.getArgs()[0], context));
                    else
                        throw new IllegalStateException(String.format("Cannot resolve argument to appropriate type in %s#%s()", instance.getClass().getName(), method.getName()));
                }
            }
            break;
            default:
                throw new IllegalArgumentException(String.format("Cannot invoke method %s#%s(): Invalid parameter count", instance.getClass().getName(), method.getName()));
        }
    }

    private void callback(Object... parameters) {
        try {
            method.invoke(instance, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (e.getCause() instanceof CommandCallbackException) throw (CommandCallbackException) e.getCause();
            e.printStackTrace();
            throw new CommandCallback.CommandCallbackException("An error occurred while executing the command method callback. Check console for errors.");
        }
    }
}
