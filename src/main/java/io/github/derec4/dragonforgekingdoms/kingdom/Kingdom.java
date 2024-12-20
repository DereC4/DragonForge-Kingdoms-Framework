package io.github.derec4.dragonforgekingdoms.kingdom;

import io.github.derec4.dragonforgekingdoms.EggData;
import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@Getter
public class Kingdom {
    private boolean open;
    private int claimedChunks;
    private int level;
    // Getters
    @Getter
    private String name;
    private String description;
    private String creationTime;
    private UUID leader;
    private UUID ID;
    private Set<UUID> members;
    private Location home;
    @Getter
    private EggData eggData;
    private int health;

    public Kingdom(String name, UUID leader, Location home) {
        this.name = name;
        this.description = "";
        this.open = false;
        this.leader = leader;
        this.ID = UUID.randomUUID(); // Change this later when we save kingdoms
        this.members = new HashSet<>();
        this.home = home;
        this.level = 1;
        this.claimedChunks = 1;
        this.members.add(leader);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        this.creationTime = formatter.format(date);
        this.health = (int) getMaxHealth(); // Bad code i know but it is 2 AM
    }

    /**
     * Constructor for loading in from a database
     */
    public Kingdom(UUID ID, String name, UUID leader, Location home, String description, boolean open,
                   String creationTime, int level, int claimedChunks, int health) {
        this.name = name;
        this.description = description;
        this.open = open;
        this.leader = leader;
        this.ID = ID;
        this.members = new HashSet<>();
        this.home = home;
        this.level = level;
        members.add(leader);
        this.creationTime = creationTime;
        this.claimedChunks = claimedChunks;
        this.health = health;
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
     * Chunk claiming is handled in the command manager, here it simply updates the kingdom total chunks assuming
     * a chunk was claimed
     */
    public void incrementChunks() {
//        ChunkCoordinate chunkCoord = new ChunkCoordinate(chunkX, chunkZ, worldID);
//        boolean res = territory.add(chunkCoord);
        claimedChunks++;
//        checkLevelUp();
    }

//    /**
//     * TODO Get actual territory limits per level and then limit it as such
//     */
//    public boolean canClaimMoreChunks() {
//        switch(level) {
//            case 1: {
//                if(claimedChunks < 6) {
//                    return true;
//                }
//            }
//            case 2: {
//                if(claimedChunks < 8) {
//                    return true;
//                }
//            }
//            case 3: {
//                if(claimedChunks < 10) {
//                    return true;
//                }
//            }
//            case 4: {
//                if(claimedChunks < 15) {
//                    return true;
//                }
//            }
//            case 5: {
//                if(claimedChunks < 30) {
//                    return true;
//                }
//            }
//            case 6: {
//                if(claimedChunks < 60) {
//                    return true;
//                }
//            }
//            case 7: {
//                if(claimedChunks < 100) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

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

    public float getMaxHealth() {
        switch (getLevel()) {
            case 2 -> {
                return 40;
            }
            case 3 -> {
                return 60;
            }
            case 4 -> {
                return 80;
            }
            case 5 -> {
                return 100;
            }
            case 6 -> {
                return 200;
            }
            case 7 -> {
                return 300;
            }
            default -> {
                return 30;
            }
        }
    }

    /**
     * Updates the Kingdom's health by the amount specified
     * @param amount Amount to add or deduct to the health
     * @return If the health was updated
     */
    public boolean updateHealth(int amount) {
        // This is bad code but it is 4:12 AM and I am tired
        if(health + amount <= 0) {
            return false;
        }
        health = (int) Math.min(health + amount,getMaxHealth());
        return true;
    }

    public void setName(String name) {
        this.name = name;
//        updateNameInDatabase();
    }

    private void updateNameInDatabase() {
        CreateDB databaseManager = new CreateDB();
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (connection != null) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE kingdoms SET name = ? WHERE ID = ?")) {
                statement.setString(1, this.name);
                statement.setString(2, this.ID.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
            }
        }
    }

    public BaseComponent[] getStats() {
        System.out.println(this.home.getWorld().getUID());
        return new ComponentBuilder(this.name + " (level" + this.level  + ") ").color(net.md_5.bungee.api.ChatColor.BLUE)
                .append("\nHome to " + this.members.size() + " people")
                .append("\nEgg Health " + this.getHealth())
                .append("\nDescription: " + this.description)
                .append("\nDestroyed Kingdoms: 0")
                .append("\nLand Power: " + this.claimedChunks)
                .append("\nLed by Lord " + Bukkit.getOfflinePlayer(this.leader).getName()).create();
    }

//    /**
//     * Checks if a kingdom has met the criteria for leveling up
//     */
//    public void checkLevelUp() {
//        int memberCount = this.members.size();
//        int chunksClaimed = this.claimedChunks;
//        if(memberCount >= 200) {
//            this.level = 7;
//        } else if(memberCount >= 100) {
//            this.level = 6;
//        } else if(memberCount >= 50) {
//            this.level = 5;
//        } else if(memberCount >= 25) {
//            this.level = 4;
//        } else if(memberCount >= 10 ) {
//            this.level = 3;
//        } else if(memberCount >= 3) {
//            this.level = 2;
//        } else if(memberCount >= 1) {
//            this.level = 1;
//        }
//    }

    public void clearMembers() {
        this.members.clear();
    }

    public void setLevel(int level) {
        this.level = level;
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

    public void setHome(Location home) {
        this.home = home;
    }

    public boolean addPlayer(UUID uuid) {
        boolean res = this.members.add(uuid);
//        checkLevelUp();
        return res;
    }

    public boolean removePlayer(UUID uuid) {
        return this.members.remove(uuid);
    }

    public void saveToDatabase(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO kingdoms (ID, name, description, open, creationTime, leader, level, " +
                        "claimedChunks, home_world_id, home_x, home_y, home_z, health) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, this.ID.toString());
            statement.setString(2, this.name);
            statement.setString(3, this.description);
            statement.setBoolean(4, this.open);
            statement.setString(5, this.creationTime);
            statement.setString(6, this.leader.toString());
            statement.setInt(7, this.level);
            statement.setInt(8, this.claimedChunks);
            statement.setString(9, Objects.requireNonNull(this.home.getWorld()).getUID().toString());
            statement.setInt(10, this.home.getBlockX());
            statement.setInt(11, this.home.getBlockY());
            statement.setInt(12, this.home.getBlockZ());
            statement.setInt(13,this.health);
            statement.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
        }
    }


    @Override
    public String toString() {
        return this.name + ", created on " + this.creationTime;
    }

    public void setEggData(EggData eggData) {
        this.eggData = eggData;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Kingdom)) {
            return false;
        }
        return this.ID.equals(((Kingdom) obj).ID);
    }

    //    // Nested class to represent chunk coordinates
//    private static class ChunkCoordinate {
//        private final int x;
//        private final int z;
//        private final UUID worldID;
//
//        public ChunkCoordinate(int x, int z, UUID worldID) {
//            this.x = x;
//            this.z = z;
//            this.worldID = worldID;
//        }
//
//        public int getX() {
//            return x;
//        }
//
//        public int getZ() {
//            return z;
//        }
//
//        public UUID getWorldID() {
//            return worldID;
//        }
//
//        @Override
//        public int hashCode() {
//            // Implement a custom hash code that combines x and z values
//            return 31 * x + z;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (this == obj) {
//                return true;
//            }
//            if (obj == null || getClass() != obj.getClass()) {
//                return false;
//            }
//            ChunkCoordinate other = (ChunkCoordinate) obj;
//            return x == other.x && z == other.z;
//        }
//    }
}