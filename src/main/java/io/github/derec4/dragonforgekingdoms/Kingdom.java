package io.github.derec4.dragonforgekingdoms;

import org.spongepowered.api.world.Chunk;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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


}
