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
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.UUID;

public class KingdomCommands {
    public static CommandSpec getCreateCommand() {
        return CommandSpec.builder()
                .description(Text.of("Creates a new Kingdom!"))
                .permission("dragonforge.command.kingdom.createKingdom")
                .arguments(GenericArguments.string(Text.of("name")))
                .executor(new MyCommand())
                .build();
    }

    public static CommandSpec getRemoveCommand() {
        return CommandSpec.builder()
                .description(Text.of("Destroys your Kingdom!"))
                .permission("dragonforge.command.kingdom.removeKingdom")
                .arguments(GenericArguments.string(Text.of("name")))
                .executor(new MyCommand())
                .build();
    }

    public static CommandSpec getClaimCommand() {
        return CommandSpec.builder()
                .description(Text.of("Creates a new Kingdom!"))
                .permission("dragonforge.command.kingdom.claimLand")
                .executor(new CommandExecutor() {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        if(src instanceof Player) {
                            KingdomManager temp = KingdomManager.getInstance();
                            Player p = (Player) src;
                            World world = p.getWorld();
                            WorldProperties worldProperties = world.getProperties();
                            int chunkX = p.getLocation().getChunkPosition().getX();
                            int chunkZ = p.getLocation().getChunkPosition().getZ();
                            String name = "TEMP";
                            boolean success = temp.getKingdoms().
                        } else {
                            src.sendMessage(Text.of("Don't run this command on console!"));
//                                    "This chunk may already be claimed or there was an error."));
                        }
                        return CommandResult.success();
                    }
                })
                .build();
    }

    public static CommandSpec getKingdomParentCommand() {
        return CommandSpec.builder()
                .description(Text.of("Kingdom parent command"))
                .permission("dragonforge.command.kingdom")
                .child(getCreateCommand(), "create", "establish")
                .child(getRemoveCommand(), "abolish", "destroy")
                .child(getClaimCommand(), "claim")
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