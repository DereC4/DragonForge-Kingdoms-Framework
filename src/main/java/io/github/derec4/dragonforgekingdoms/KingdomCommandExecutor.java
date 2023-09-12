package io.github.derec4.dragonforgekingdoms;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class KingdomCommandExecutor implements CommandExecutor {

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
        return null;
    }
}
