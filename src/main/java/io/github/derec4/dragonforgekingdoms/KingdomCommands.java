package io.github.derec4.dragonforgekingdoms;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.UUID;

public class KingdomCommands {
    public static CommandSpec getCreateCommand() {
        return CommandSpec.builder()
                .description(Text.of("Creates a new Kingdom!"))
                .permission("kingdom.command.createKingdom")
                .arguments(GenericArguments.string(Text.of("name")))
                .executor(new MyCommand())
                .build();
    }
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
        KingdomManager temp = KingdomManager.getInstance();
        String name = args.<String>getOne("name").orElse("Unnamed " +
                "Kingdom");
        /**
         * UUID of 0 at first but will almost most likely be set to a player UUID
         */
        UUID playerID = new UUID(0L, 0L);
        if(src instanceof Player) {
            Player player = (Player) src;
            playerID = player.getUniqueId();
        }
        Kingdom k = new Kingdom(name, playerID);
        temp.addKingdom(k);

        src.sendMessage(Text.of("The Kingdom of " + k.getName()));
        src.sendMessage(Text.of(k.printMembers()));
        System.out.println(temp.getKingdoms());
        return CommandResult.success();
    }
}