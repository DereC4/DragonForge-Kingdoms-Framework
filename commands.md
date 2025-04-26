# DragonForge Kingdoms Commands

Below is a list of all commands, their arguments, and descriptions of what they do. These commands include both player and admin commands related to the plugin.

---

## **Player Commands**

### `/kingdom create <name>`
- **Arguments**: `<name>` - The name of the kingdom to create.
- **Description**: Creates a new kingdom with the specified name.

### `/kingdom remove`
- **Arguments**: None.
- **Description**: Deletes the player's current kingdom after confirmation.

### `/kingdom claim`
- **Arguments**: None.
- **Description**: Claims the chunk the player is currently in for their kingdom.

### `/kingdom rename <name>`
- **Arguments**: `<name>` - The new name for the kingdom.
- **Description**: Renames the player's current kingdom.

### `/kingdom leave`
- **Arguments**: None.
- **Description**: Leaves the player's current kingdom.

### `/kingdom description <description>`
- **Arguments**: `<description>` - A new description for the kingdom.
- **Description**: Sets a description for the player's current kingdom.

### `/kingdom sethome`
- **Arguments**: None.
- **Description**: Sets the home location for the player's current kingdom.

### `/kingdom territory`
- **Arguments**: None.
- **Description**: Displays the territory owned by the player's kingdom.

### `/kingdom join <name>`
- **Arguments**: `<name>` - The name of the kingdom to join.
- **Description**: Allows a player to join an existing kingdom.

### `/kingdom home`
- **Arguments**: None.
- **Description**: Teleports the player to their kingdom's home location.

### `/kingdom promote <player>`
- **Arguments**: `<player>` - The name of the player to promote.
- **Description**: Promotes a player within the kingdom.

### `/kingdom stats`
- **Arguments**: None.
- **Description**: Displays the stats of the player's current kingdom.

### `/kingdom map`
- **Arguments**: None.
- **Description**: Displays a map of the surrounding chunks, showing claimed and unclaimed areas.

### `/kingdom invite <player>`
- **Arguments**: `<player>` - The name of the player to invite.
- **Description**: Invites a player to join the player's kingdom.

### `/kingdom banish <player>`
- **Arguments**: `<player>` - The name of the player to banish.
- **Description**: Removes a player from the kingdom.

### `/kingdom transfer <amount>`
- **Arguments**: `<amount>` - The amount of money to transfer to the kingdom's treasury.
- **Description**: Transfers money from the player's balance to the kingdom's wealth.

---

## **Admin Commands**

### `/admin forcedelete <kingdom name>`
- **Arguments**: `<kingdom name>` - The name of the kingdom to delete.
- **Description**: Forcefully deletes a kingdom and resets all its members to adventurers.

### `/admin changeowner <kingdom name> <new owner>`
- **Arguments**:
    - `<kingdom name>` - The name of the kingdom.
    - `<new owner>` - The name of the new owner.
- **Description**: Changes the ownership of a kingdom to a new player.

### `/admin guardegg [quantity]`
- **Arguments**: `[quantity]` - The number of guard eggs to give (default is 1).
- **Description**: Gives the player custom guard spawn eggs.

### `/admin soldieregg [quantity]`
- **Arguments**: `[quantity]` - The number of soldier eggs to give (default is 1).
- **Description**: Gives the player custom soldier spawn eggs.

### `/admin archeregg [quantity]`
- **Arguments**: `[quantity]` - The number of archer eggs to give (default is 1).
- **Description**: Gives the player custom archer spawn eggs.

---
