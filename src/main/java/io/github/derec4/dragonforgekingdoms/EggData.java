package io.github.derec4.dragonforgekingdoms;

import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import io.github.derec4.dragonforgekingdoms.util.EncoderUtils;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/*
    Persistence EggData class to store into chunks

    @author Potatoes_duck
    @date 11/17/2023

    COPYRIGHT, DO NOT MODIFY OR FACE DEATH
 */
@Getter
public class EggData {

    private final static float MAX_HEALTH = 200;
    private final static NamespacedKey EGG_SPACE = new NamespacedKey(JavaPlugin.getPlugin(DragonForgeKingdoms.class), "egg");

    private float health;
    private String world;
    private String name;
    private String kingdomUuid;
    private int x, y, z;

    public static EggData assignEggData(Kingdom kingdom, Location location) {
        // checks if a kingdom already contains
        if(kingdom.getEggData() != null) {
            return null;
        }

        EggData eggData = new EggData(location);
        eggData.kingdomUuid = kingdom.getID().toString();
        location.getChunk().getPersistentDataContainer().set(EGG_SPACE, PersistentDataType.BYTE_ARRAY,
                eggData.encode());
        Block block = location.getBlock();

        // Check if the block is air (optional)
        if (block.getType() == Material.AIR) {
            block.setType(Material.DRAGON_EGG);
//            DragonEgg dragonEgg = (DragonEgg) block.getBlockData();
//            dragonEgg.setTeleportable(true);
//            block.setBlockData(dragonEgg);
        }
        kingdom.setEggData(eggData);

        return eggData;
    }

    public static boolean destroyEgg(Block block) {
        return destroyEgg(block, false);
    }

    public static boolean destroyEgg(Block block, boolean chunkSearch) {
        PersistentDataContainer container = block.getChunk().getPersistentDataContainer();

        if (!container.has(EGG_SPACE, PersistentDataType.BYTE_ARRAY)) {
            return false; // chunk doesn't have an egg
        }

        byte[] data = container.get(EGG_SPACE, PersistentDataType.BYTE_ARRAY);
        EggData eggData = decode(data);

        if (!eggData.world.equals(block.getWorld().getUID().toString())) {
            return false; // not in the same world
        }

        if(!chunkSearch && (eggData.x != block.getX() || eggData.y != block.getY() || eggData.z == block.getZ())) {
            return false; // not on the provided block
        }

        // get the kingdom and set the egg to null

        container.remove(EGG_SPACE);

        return true;
    }

    public static EggData decode(byte[] data) {
        EggData eggData = new EggData();

        int pos = 0;
        eggData.health = EncoderUtils.byteArrayToFloat(data, pos);
        pos += 4;
        eggData.world = EncoderUtils.byteArrayToString(data, pos);
        pos += 4 + eggData.world.length();
        eggData.name = EncoderUtils.byteArrayToString(data, pos);
        pos += 4 + eggData.name.length();
        eggData.kingdomUuid = EncoderUtils.byteArrayToString(data, pos);
        pos += 4 + eggData.kingdomUuid.length();
        eggData.x = EncoderUtils.byteArrayToInt(data, pos);
        pos += 4;
        eggData.y = EncoderUtils.byteArrayToInt(data, pos);
        pos += 4;
        eggData.z = EncoderUtils.byteArrayToInt(data, pos);
        pos += 4;

        return eggData;
    }

    private EggData(Location location) {
        health = MAX_HEALTH;

        world = location.getWorld().getUID().toString();
        x = location.getBlockX();
        y = location.getBlockY();
        z = location.getBlockZ();

//        name = "Heath's " + (Math.random() > 0.5 ? "Left " : "Right ") + "Balls";
        // Bersam wtf??
    }

    private EggData() {

    }

    public byte[] encode() {
        byte[] hpEncode = EncoderUtils.encodeFloat(health);
        byte[] worldEncode = EncoderUtils.encodeString(world);
        byte[] nameEncode = EncoderUtils.encodeString(name);
        byte[] kingdomUuidEncode = EncoderUtils.encodeString(kingdomUuid);
        byte[] xEncode = EncoderUtils.encodeInt(x);
        byte[] yEncode = EncoderUtils.encodeInt(y);
        byte[] zEncode = EncoderUtils.encodeInt(z);

        return packEncoding(hpEncode, worldEncode, nameEncode, kingdomUuidEncode, xEncode, yEncode, zEncode);
    }

    private byte[] packEncoding(byte[]... data) {
        int byteCount = 0;
        for(byte[] encodedData : data) {
            byteCount += encodedData.length;
        }

        byte[] packedData = new byte[byteCount];
        int current = 0;
        for(byte[] encodedData : data) {
            for(byte b : encodedData) {
                packedData[current++] = b;
            }
        }

        return packedData;
    }

    // example code
    static {
        PlayerInteractEvent event = new PlayerInteractEvent(null, null, null, null, null, null, null);

        // check if it has first, probably want a helper method to go from block -> egg, check destroyEgg
        byte[] data = event.getClickedBlock().getChunk().getPersistentDataContainer().get(EGG_SPACE,
                PersistentDataType.BYTE_ARRAY);

        EggData eggData = decode(data);

        eggData.updateHealth(eggData.health - 20, true);
    }

    public static EggData getChunkEggData(Chunk chunk) {

        if(!chunk.getPersistentDataContainer().has(EGG_SPACE,PersistentDataType.BYTE_ARRAY)) {
            return null;
        }

        byte[] data = chunk.getPersistentDataContainer().get(EGG_SPACE,
                PersistentDataType.BYTE_ARRAY);

        return decode(data);
    }

    public void updateHealth(float health, boolean hardUpdate) {
        boolean majorHealthChange = false;

        this.health = health;

        if(health <= 0) {
            majorHealthChange = true;
            destroyEgg(new Location(Bukkit.getWorld(UUID.fromString(world)), x, y, z).getBlock());
            return;
        }
        // like a db, we dont ALWAYS have to push
        if(majorHealthChange || hardUpdate) {
            new Location(Bukkit.getWorld(UUID.fromString(world)), x, y, z).getChunk().getPersistentDataContainer()
                    .set(EGG_SPACE, PersistentDataType.BYTE_ARRAY, encode());
        }


    }
}