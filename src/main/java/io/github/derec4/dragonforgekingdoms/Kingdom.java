package io.github.derec4.dragonforgekingdoms;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.text.SimpleDateFormat;
import java.util.*;

public class Kingdom {
    private String name;
    private String description;
    private boolean open;
    private final String creationTime;
    private UUID leader;
    private UUID ID;
    private Set<UUID> members;
    private Set<ChunkCoordinate> territory;

    public Kingdom(String name, UUID leader) {
        this.name = name;
        this.description = "";
        this.open = false;
        this.leader = leader;
        this.ID = UUID.randomUUID();
        this.members = new HashSet<>();
        this.territory = new HashSet<>();
        members.add(leader);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        creationTime = formatter.format(date);
        KingdomManager temp = KingdomManager.getInstance();
        temp.addPlayerToKingdom(leader, getID());
    }

    /**
     * Iterates through the memberlist, converting UUIDs to player usernames
     * @return A string of members in the kingdom
     */
    public String printMembers() {
        StringBuilder memberNames = new StringBuilder("Members ");
        for(UUID u: members) {
            Player player = Bukkit.getPlayer(u);
            if (player != null) {
                memberNames.append(player.getName());
                memberNames.append(", ");
            }
        }
        if (!memberNames.isEmpty()) {
            memberNames.setLength(memberNames.length() - 2); // Remove the trailing comma and space
        }
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

    public UUID getID() {
        return ID;
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

    public Optional<Player> getPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);

        if (player != null) {
            // The player is online
            return Optional.of(player);
        } else {
            // The player is not online
            return Optional.empty();
        }
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