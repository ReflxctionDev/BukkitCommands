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
import io.github.reflxction.commands.PluginSubcommand.ParameterResolver;
import io.github.reflxction.commands.PluginSubcommand.TabContext;
import io.github.reflxction.commands.PluginSubcommand.TabProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A class for resolving specific parameters
 */
public class CommandResolvers {

    private static final List<String> PLAYERS = Collections.singletonList("null");

    /**
     * A map of all resolvers
     */
    private final Map<Class<?>, Resolver<?>> resolvers = new HashMap<>();

    /**
     * A map of all tab providers
     */
    private final Map<String, TabProvider> tabProviders = new HashMap<>();

    /**
     * A map of all tab providers
     */
    private final Map<String, List<String>> staticTabs = new HashMap<>();

    /**
     * Creates an instance of all the resolvers
     */
    public CommandResolvers() {
        // register primitives
        registerResolver(String.class, new Resolver<>("string", (a, c) -> a));

        registerResolver(int.class, new Resolver<>("number", (a, c) -> Integer.parseInt(a)));
        registerResolver(Integer.class, new Resolver<>("number", (a, c) -> Integer.parseInt(a)));

        registerResolver(float.class, new Resolver<>("number", (a, c) -> Float.parseFloat(a)));
        registerResolver(Float.class, new Resolver<>("number", (a, c) -> Float.parseFloat(a)));

        registerResolver(double.class, new Resolver<>("number", (a, c) -> Double.parseDouble(a)));
        registerResolver(Double.class, new Resolver<>("number", (a, c) -> Double.parseDouble(a)));

        registerResolver(short.class, new Resolver<>("number", (a, c) -> Short.parseShort(a)));
        registerResolver(Short.class, new Resolver<>("number", (a, c) -> Short.parseShort(a)));

        registerResolver(long.class, new Resolver<>("number", (a, c) -> Long.parseLong(a)));
        registerResolver(Long.class, new Resolver<>("number", (a, c) -> Long.parseLong(a)));

        registerResolver(byte.class, new Resolver<>("number", (a, c) -> Byte.parseByte(a)));
        registerResolver(Byte.class, new Resolver<>("number", (a, c) -> Byte.parseByte(a)));

        // register Bukkit types
        registerResolver(Player.class, new Resolver<>("player", (a, c) -> Bukkit.getPlayer(a)));
        registerResolver(World.class, new Resolver<>("world", (a, c) -> Bukkit.getWorld(a)));
        registerResolver(OfflinePlayer.class, new Resolver<>("player", (a, c) -> Bukkit.getOfflinePlayer(a)));

        registerStaticTab("players", PLAYERS); // bukkit handles that by itself.
        registerStaticTab("nothing", Collections.emptyList());
    }

    /**
     * Returns the resolver of this type
     *
     * @param type Type to resolve into
     * @return The resolver
     */
    @Nullable
    public Resolver<?> get(Class<?> type) {
        return resolvers.get(type);
    }

    public List<String> getTab(String key, TabContext context) {
        List<String> tabs = staticTabs.get(key);
        if (tabs != null && tabs.equals(PLAYERS)) return null;
        if (tabs == null) {
            TabProvider provider = tabProviders.get(key);
            if (provider == null)
                throw new IllegalArgumentException("Cannot find tabs for key " + key);
            tabs = provider.getTab(context);
        }
        return tabs;
    }

    /**
     * Registers a static list of tabs for the specified key
     *
     * @param key  Key to register for
     * @param tabs A list of all strings. For spaces, use "~~"
     */
    public void registerStaticTab(String key, List<String> tabs) {
        staticTabs.put(key, tabs);
    }

    /**
     * Registers a tab provider for the specified key
     *
     * @param key      Key to register for
     * @param provider The tab provider
     */
    public void registerTabProvider(String key, TabProvider provider) {
        tabProviders.put(key, provider);
    }

    /**
     * Registers the specified resolver. This is mainly wrapping the map's actions so that we can run
     * compiler checks to ensure correct types are passed to the map
     *
     * @param resolvedType Type to resolve
     * @param resolver     The resolver
     * @param <R>          Type to be resolved
     */
    public <R> void registerResolver(Class<R> resolvedType, Resolver<R> resolver) {
        resolvers.put(resolvedType, resolver);
    }

    /**
     * A resolver wrapper for handling exceptions
     *
     * @param <R> Type to resolve into
     */
    public static class Resolver<R> {

        /**
         * The internal resolver
         */
        private final ParameterResolver<R> resolver;

        /**
         * The name in which "invalids" will appear with
         */
        private final String name;

        /**
         * A consumer to run if the command fails. Can be left to use the standard fallback
         */
        private BiConsumer<String, CommandContext> onFail;

        /**
         * Creates a new resolver wrapper
         *
         * @param name     Name of the resolver's "invalid"
         * @param resolver The internal resolver
         */
        public Resolver(String name, ParameterResolver<R> resolver) {
            this.resolver = resolver;
            this.name = name;
        }

        /**
         * Sets the fail fallback consumer
         *
         * @param onFail Task to run when failed
         * @return This resolver instance
         */
        public Resolver<R> setFail(BiConsumer<String, CommandContext> onFail) {
            this.onFail = onFail;
            return this;
        }

        /**
         * Resolves the specified type
         *
         * @param argument Argument to resolve from
         * @param context  Command context
         * @return The resolved type
         * @throws CommandCallbackException If the type could not be resolved
         */
        public R resolve(String argument, CommandContext context) {
            try {
                R resolved = resolver.resolve(argument, context);
                if (resolved == null)
                    throw new NullPointerException(); // this will redirect us down below
                return resolved;
            } catch (Exception e) {
                if (onFail == null && !(e instanceof CommandCallbackException)) {
                    context.getCommandHandler().resolverFail.onFail(name, argument, context);
                    throw new CommandCallbackException();
                }
                if (onFail != null) {
                    onFail.accept(argument, context);
                    throw new CommandCallbackException();
                }
                return null;
            }
        }
    }

    /**
     * A fallback functional interface, with its method invoked when a
     * parameter cannot be resolved
     */
    public interface ResolverFallback {

        void onFail(String name, String invalidValue, CommandContext context);
    }
}
