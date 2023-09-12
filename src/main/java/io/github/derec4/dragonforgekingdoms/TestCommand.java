package io.github.derec4.dragonforgekingdoms;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

public class TestCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, org.spongepowered.api.command.args.CommandContext args) {
        // Your command logic goes here
        src.sendMessage(Text.of("This is a test command!"));
        return CommandResult.success();
    }
}