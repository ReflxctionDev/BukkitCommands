package io.github.reflxction.commands;

import io.github.reflxction.commands.CommandResolvers.Resolver;
import io.github.reflxction.commands.CommandResolvers.ResolverFallback;
import io.github.reflxction.commands.PluginSubcommand.ParameterResolver;
import io.github.reflxction.commands.PluginSubcommand.TabContext;
import io.github.reflxction.commands.PluginSubcommand.TabProvider;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a parent command
 */
public class ParentCommand implements TabExecutor {

    /**
     * A default fallback when no arguments are specified
     */
    private static final String[] HELP = new String[]{"help"};

    /**
     * The command handler
     */
    private final CommandHandler commandHandler = new CommandHandler();

    /**
     * @see #create().
     */
    private ParentCommand() {
    }

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) args = HELP;
        commandHandler.onCommand(command, sender, args);
        return true;
    }

    /**
     * Requests a list of possible completions for a command argument.
     *
     * @param sender  Source of the command.  For players tab-completing a
     *                command inside of a command block, this will be the player, not
     *                the command block.
     * @param command Command which was executed
     * @param alias   The alias used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed and command label
     * @return A List of possible completions for the final argument, or null
     * to default to the command executor
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1)
            return commandHandler.getCommands().values().stream().filter(a -> a.name.startsWith(args[0]))
                    .filter(s -> s.hasPermission(sender))
                    .map(c -> c.name)
                    .distinct()
                    .collect(Collectors.toList());
        if (args.length > 1) {
            String[] finalArgs = (String[]) ArrayUtils.subarray(args, 1, args.length);
            if (finalArgs.length == 0) return Collections.emptyList();
            SubcommandInvokation subcommand = commandHandler.getCommands().get(args[0]);
            if (subcommand == null) return Collections.emptyList();
            String tab = subcommand.tab;
            if (subcommand.tab.equals(PluginSubcommand.DEFAULT_COMPLETION)) return Collections.emptyList();
            TabContext context = new TabContext(finalArgs, sender, subcommand, command, commandHandler);
            String[] tabs = tab.split(" ");

            String thisTab;
            try {
                thisTab = tabs[finalArgs.length - 1];
            } catch (ArrayIndexOutOfBoundsException e) {
                return Collections.emptyList();
            }
            if (thisTab.startsWith("@")) {
                List<String> text = commandHandler.getResolvers()
                        .getTab(thisTab.substring(1), context);
                return text == null ? null : text
                        .stream()
                        .distinct()
                        .filter(c -> c.startsWith(finalArgs[finalArgs.length - 1]))
                        .collect(Collectors.toList());
            }
            return Arrays
                    .stream(StringUtils.split(thisTab, "|"))
                    .map(a -> a.replace("~~", " "))
                    .distinct()
                    .filter(a -> a.startsWith(finalArgs[finalArgs.length - 1]))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Registers/Contains the specified command in the processing
     *
     * @param callback Command callback to register
     * @return This command
     */
    public ParentCommand contain(Object callback) {
        commandHandler.register(callback);
        return this;
    }

    /**
     * Registers/Contains the specified command in the processing, and allows the registry to
     * access the command handler instance
     *
     * @param withHandler Function to register
     * @return This command
     */
    public ParentCommand contain(Function<CommandHandler, Object> withHandler) {
        commandHandler.register(withHandler.apply(commandHandler));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public ParentCommand setMessagingPrefix(String prefix) {
        commandHandler.setMessagingPrefix(prefix);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public ParentCommand registerTabProvider(String key, TabProvider provider) {
        commandHandler.getResolvers().registerTabProvider(key, provider);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public ParentCommand registerStaticTabs(String key, List<String> tabs) {
        commandHandler.getResolvers().registerStaticTab(key, tabs);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public <R> ParentCommand registerResolver(String name, Class<R> type, ParameterResolver<R> resolver) {
        commandHandler.getResolvers().registerResolver(type, new Resolver<>(name, resolver));
        return this;
    }

    /**
     * Sets the task ran when an invalid subcommand is inputted
     *
     * @param action Action to run
     * @return This parent command for chaining
     */
    public ParentCommand setOnInvalidCommand(@NotNull Consumer<CommandContext> action) {
        commandHandler.invalidCommand = action;
        return this;
    }

    /**
     * Sets the task ran when the sender does not have the required permission for the subcommand
     *
     * @param action Action to run
     * @return This parent command for chaining
     */
    public ParentCommand setOnNoPermission(@NotNull Consumer<CommandContext> action) {
        commandHandler.noPermission = action;
        return this;
    }

    /**
     * Sets the task ran when a subcommand's usage is invalid
     *
     * @param action Action to run
     * @return This parent command for chaining
     */
    public ParentCommand setOnInvalidCommandUsage(@NotNull Consumer<CommandContext> action) {
        commandHandler.invalidCommandUsage = action;
        return this;
    }

    /**
     * Sets the task ran when a resolver gets an invalid argument
     *
     * @param action Action to run
     * @return This parent command for chaining
     */
    public ParentCommand setOnResolverFail(@NotNull ResolverFallback action) {
        commandHandler.resolverFail = action;
        return this;
    }

    /**
     * Creates a new command
     */
    public static ParentCommand create() {
        return new ParentCommand();
    }

    /**
     * Returns the command handler of this parent command
     *
     * @return The command handler
     */
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }
}
