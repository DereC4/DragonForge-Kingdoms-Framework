package io.github.derec4.dragonforgekingdoms;

import java.util.UUID;

// Nested class to represent chunk coordinates
public class ChunkCoordinate {
    private final int x;
    private final int z;
    private final UUID worldID;

    public ChunkCoordinate(int x, int z, UUID worldID) {
        this.x = x;
        this.z = z;
        this.worldID = worldID;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public UUID getWorldID() {
        return worldID;
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
        return x == other.x && z == other.z && worldID.equals(other.worldID);
    }
}