package io.github.derec4.dragonforgekingdoms;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.world.Chunk;

import java.text.SimpleDateFormat;
import java.util.*;

public class Kingdom {
    private String name;
    private String description;
    private boolean open;
    private final String creationTime;
    private UUID leader;
    private Set<UUID> members;
    private Set<Chunk> territory;
    public Kingdom(String name, UUID leader) {
        this.name = name;
        this.description = "";
        this.open = false;
        this.leader = leader;
        this.members = new HashSet<>();
        members.add(leader);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        creationTime = formatter.format(date);
    }

    public String printMembers() {
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        if (!userStorage.isPresent()) {
            // UserStorageService is not available, handle the error
            System.out.println("ERROR");
            return "Error";
        }
        StringBuilder memberNames = new StringBuilder("Members ");
        for(UUID u: members) {
            Optional<User> temp = userStorage.get().get(u);
            if (!temp.isPresent()) {
                // UserStorageService is not available, handle the error
                System.out.println("ERROR2");
                return "Error";
            }
            User user = temp.get();
            memberNames.append(user.getName());
            memberNames.append(", ");
        }
        memberNames.setLength(memberNames.length() - 1);

        System.out.println(memberNames.toString());
        return memberNames.toString();
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOpen() {
        return open;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public UUID getLeader() {
        return leader;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Optional<User> getUser(UUID uuid) {
        Optional<UserStorageService> userStorageOptional = Sponge.getServiceManager().provide(UserStorageService.class);
        if (!userStorageOptional.isPresent()) {
            // UserStorageService is not available, handle the error (return empty)
            System.out.println("Optional is not present");
            return Optional.empty();
        }
        UserStorageService userStorage = userStorageOptional.get();
        Optional<User> userOptional = userStorage.get(uuid);

        // Check if the user is online (a Player)
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user instanceof Player) {
                Player player = (Player) user;
            }
        }

        return userOptional;
    }

    public Set<Chunk> getTerritory() {
        return territory;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public void setMembers(Set<UUID> members) {
        this.members = members;
    }

    public void setTerritory(Set<Chunk> territory) {
        this.territory = territory;
    }

    @Override
    public String toString() {
        return this.name + ", created on " + this.creationTime;
    }
}
