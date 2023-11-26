package io.github.derec4.dragonforgekingdoms;

import io.github.derec4.dragonforgekingdoms.database.CreateDB;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Kingdom {
    private boolean open;
    private int level;
    private String name;
    private String description;
    private String creationTime;
    private UUID leader;
    private UUID ID;
    private Set<UUID> members;
    private Location home;
    private EggData eggData;
    private Set<ChunkCoordinate> territory;

    public Kingdom(String name, UUID leader, Location home) {
        this.name = name;
        this.description = "";
        this.open = false;
        this.leader = leader;
        this.ID = UUID.randomUUID(); // Change this later when we save kingdoms
        this.members = new HashSet<>();
        this.territory = new HashSet<>();
        this.home = home;
        this.level = 1;
        members.add(leader);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        this.creationTime = formatter.format(date);
    }

    /**
     * Constructor for loading in from a database
     */
    public Kingdom(UUID ID, String name, UUID leader, Location home, String description, boolean open,
                   String creationTime, int level) {
        this.name = name;
        this.description = description;
        this.open = open;
        this.leader = leader;
        this.ID = ID;
        this.members = new HashSet<>();
        this.territory = new HashSet<>();
        this.home = home;
        this.level = level;
        members.add(leader);
        this.creationTime = creationTime;
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
    public boolean claimChunk(int chunkX, int chunkZ, UUID worldID) {
        ChunkCoordinate chunkCoord = new ChunkCoordinate(chunkX, chunkZ, worldID);
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

    public int getLevel() {
        return level;
    }

    public Location getHome() {
        return home;
    }
    // Print the territory (claimed chunks) of the kingdom to the player source
    public void printTerritory(Player player) {
        player.sendMessage(ChatColor.GREEN + "Territory of Kingdom " + name + ": [");
        boolean first = true;
        for (ChunkCoordinate chunkCoord : territory) {
            if (!first) {
                player.sendMessage(", ");
            } else {
                first = false;
            }
            player.sendMessage("Chunk X: " + chunkCoord.getX() + ", Z: " + chunkCoord.getZ() + " in the world of " + chunkCoord.getWorldID());
        }
        player.sendMessage(ChatColor.GREEN + "]");
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

    public void setName(String name) {
        this.name = name;
        updateNameInDatabase();
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

    public void levelUp() {
        this.level++;
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
        return members.add(uuid);
    }

    public boolean removePlayer(UUID uuid) {
        return members.remove(uuid);
    }

    public void saveToDatabase(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO kingdoms (ID, name, description, open, leader, home_location_x, home_location_y, " +
                        "home_location_z, home_location_world, creationTime) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, ID.toString());
            statement.setString(2, name);
            statement.setString(3, description);
            statement.setBoolean(4, open);
            statement.setString(5, leader.toString());
            statement.setDouble(6, home.getX()); // Assuming home is a Location object
            statement.setDouble(7, home.getY());
            statement.setDouble(8, home.getZ());
            statement.setString(9, Objects.requireNonNull(home.getWorld()).getUID().toString());
            statement.setString(10, creationTime);
            statement.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getServer().getConsoleSender().sendMessage(e.toString());
        }
    }

    @Override
    public String toString() {
        return this.name + ", created on " + this.creationTime;
    }

    public EggData getEggData() {
        return eggData;
    }

    public void setEggData(EggData eggData) {
        if(eggData != null) {
            throw new IllegalStateException("eggData must be null, overwritten detect");
        }

        this.eggData = eggData;
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