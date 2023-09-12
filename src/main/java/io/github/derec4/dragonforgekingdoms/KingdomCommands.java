package io.github.derec4.dragonforgekingdoms;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

public class KingdomCommands {
    public static CommandSpec getCreateCommand() {
        return CommandSpec.builder()
                .description(Text.of("Creates a new Kingdom!"))
                .permission("kingdom.command.createKingdom")
                .arguments(GenericArguments.string(Text.of("name")))
                .executor(new MyCommand())
                .build();
    }
}

class MyCommand implements CommandExecutor {
    /**
     * Callback for the execution of a command.
     *
     * @param src  The commander who is executing this command
     * @param args The parsed command arguments for this command
     * @return the result of executing this command
     * @throws CommandException If a user-facing error occurs while
     *                          executing this command
     */
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(Text.of("The Kingdom of" + args.<String>getOne("name").orElse("Unnamed " +
                "Kingdom")));
        return CommandResult.success();
    }
}