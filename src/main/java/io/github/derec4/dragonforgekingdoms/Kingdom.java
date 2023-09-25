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
    private Set<ChunkCoordinate> territory;

    public Kingdom(String name, UUID leader) {
        this.name = name;
        this.description = "";
        this.open = false;
        this.leader = leader;
        this.members = new HashSet<>();
        this.territory = new HashSet<>();
        members.add(leader);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        creationTime = formatter.format(date);
    }

    /**
     * Iterates through the memberlist, converting UUIDs to player usernames
     * @return A string of members in the kingdom
     */
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

    /**
     * Claim a chunk. Chunks spawn 16x16 section on the world grid and they extend from the
     * bottom void of the world, all the way up to the top sky, thus not needing a y coordinate
     *
     * @param chunkX Chunk x coordinate
     * @param chunkZ Chunk z coordinate
     * @return True on claim success, false on failure (which means it's already claimed)
     */
    public boolean claimChunk(int chunkX, int chunkZ) {
        ChunkCoordinate chunkCoord = new ChunkCoordinate(chunkX, chunkZ);
        return territory.add(chunkCoord);
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

    // Print the territory (claimed chunks) of the kingdom
    public void printTerritory() {
        System.out.print("Territory of Kingdom " + name + ": [");
        boolean first = true;
        for (ChunkCoordinate chunkCoord : territory) {
            if (!first) {
                System.out.print(", ");
            } else {
                first = false;
            }
            System.out.print("Chunk X: " + chunkCoord.getX() + ", Z: " + chunkCoord.getZ());
        }
        System.out.println("]");
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

    @Override
    public String toString() {
        return this.name + ", created on " + this.creationTime;
    }

    // Nested class to represent chunk coordinates
    private static class ChunkCoordinate {
        private final int x;
        private final int z;

        public ChunkCoordinate(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        @Override
        public int hashCode() {
            // Implement a custom hash code that combines x and z values
            return 31 * x + z;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ChunkCoordinate other = (ChunkCoordinate) obj;
            return x == other.x && z == other.z;
        }
    }
}
