package io.github.derec4.dragonforgekingdoms;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;

// Imports for logger
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.text.Text;

@Plugin(id = "dragonforge_kingdoms", name = "Kingdoms Framework", version = "1.0",
        description = "Example")
public class DragonForgeKingdomsFramework {
    @Inject
    private Logger logger;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.info("Successfully running kingdoms framework!");
    }

    @Listener
    public void onGameInitialization(GameInitializationEvent event) {
        // Create a command specification for the "test" command
        CommandSpec testCommandSpec = CommandSpec.builder()
                .description(Text.of("A test command"))
                .permission("dragonforge_kingdoms.command.test")
                .executor(new TestCommand())
                .build();

        // Register the "test" command with Sponge
        Sponge.getCommandManager().register(this, testCommandSpec, "test");

        Sponge.getCommandManager().register(this, KingdomCommands.getCreateCommand(),
                "createKingdom");
        System.out.println("Registered Commands: " + Sponge.getCommandManager().getPrimaryAliases());
    }
}