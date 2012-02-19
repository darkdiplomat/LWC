import com.griefcraft.model.Action;
import com.griefcraft.model.Entity;
import com.griefcraft.sql.MemDB;
import com.griefcraft.sql.PhysDB;
import java.util.Iterator;
import java.util.List;

public class LWCListener extends PluginListener {
    private final LWC lwc;
    public boolean debugMode = false;
    private PhysDB physicalDatabase;
    private MemDB memoryDatabase;

    public LWCListener(LWC lwc) {
        this.lwc = lwc;
        this.physicalDatabase = lwc.getPhysicalDatabase();
        this.memoryDatabase = lwc.getMemoryDatabase();
    }

    public boolean onBlockBreak(Player player, Block block) {

        if (!isProtectable(block)) {
            return false;
        }
        int worldID = player.getWorld().getType().getId();

        List<ComplexBlock> cblist = this.lwc.getEntitySet(player.getWorld(), block.getX(), block.getY(), block.getZ());
        boolean access = true;
        boolean admin = true;
        Entity entity = null;

        for (ComplexBlock cb : cblist) {
            if (cb == null) {
                continue;
            }
            entity = this.physicalDatabase.loadProtectedEntity(worldID, cb.getX(), cb.getY(), cb.getZ());

            if (entity == null) {
                continue;
            }
            
            access = this.lwc.canAccessChest(player, entity);
            admin = this.lwc.canAdminChest(player, entity);
        }

        if ((access) && (entity != null) && (admin)) {
            this.physicalDatabase.unregisterProtectedEntity(worldID, entity.getX(), entity.getY(), entity.getZ());
            this.physicalDatabase.unregisterProtectionRights(entity.getID());
            player.sendMessage("§4Chest unregistered.");
        }

        return !admin;
    }

    @SuppressWarnings("null")
	public boolean onBlockDestroy(Player player, Block block) {
        int worldID = player.getWorld().getType().getId();

        if (!isProtectable(block)) {
            return false;
        }
        boolean access = true;
        Entity entity = null;
        int i = 1;
        List<ComplexBlock> cblist = null;
        Iterator<ComplexBlock> iBlocks = null;

        if (isComplexBlock(block)) {
            cblist = this.lwc.getEntitySet(player.getWorld(), block.getX(), block.getY(), block.getZ());

            for (iBlocks = cblist.iterator(); iBlocks.hasNext();) {
                ComplexBlock cb = iBlocks.next();
                if (cb == null) {
                    continue;
                }
                entity = this.physicalDatabase.loadProtectedEntity(worldID, cb.getX(), cb.getY(), cb.getZ());
                if (entity == null) {
                    continue;
                }
                
                access = this.lwc.canAccessChest(player, entity);
                i = 0;
            }
        } else if (block.getType() == 64){
            if (isUpperDoor(block)) {
                block = block.getWorld().getBlockAt(block.getX(), block.getY()-1, block.getZ());
            }
            entity = this.physicalDatabase.loadProtectedEntity(worldID, block.getX(), block.getY(), block.getZ());
            access = this.lwc.canAccessChest(player, entity);
        } else {
            // Only ComplexBlocks or Doors
            return false;
        }

        if (block.getStatus() != 0) {
            return !access;
        }
        String name = player.getName();
        List<String> acts = this.memoryDatabase.getActions(name);

        boolean op_free = acts.contains("free");
        boolean op_info = acts.contains("info");
        boolean op_create = acts.contains("create");
        boolean op_modify = acts.contains("modify");
        boolean op_drop = acts.contains("dropTransferSelect");
        List<String> sess;
        Iterator<String> iterator = null;
        Action action;
        // Object localObject5;
        int n;
        int i1;
        if (entity != null) {
            i = 0;

            if (op_info) {
                String str1 = "";

                if (entity.getType() == 1) {
                    sess = this.memoryDatabase.getSessionUsers(entity.getID());

                    for (iterator = sess.iterator(); iterator.hasNext();) {
                        String str2 = iterator.next();
                        Player localPlayer = etc.getServer().getPlayer(str2);

                        if (localPlayer == null) {
                            continue;
                        }
                        str1 = new StringBuilder().append(str1).append(localPlayer.getColor()).append(str2)
                                .append("§f").append(", ").toString();
                    }

                    if (sess.size() > 0) {
                        str1 = str1.substring(0, str1.length() - 4);
                    }
                }

                String str = " ";

                switch (entity.getType()) {
                case 0:
                    str = "Public";
                    break;
                case 1:
                    str = "Password";
                    break;
                case 2:
                    str = "Private";
                }

                boolean admin = this.lwc.canAdminChest(player, entity);

                if (admin) {
                    player.sendMessage(new StringBuilder().append("§2ID: §6").append(entity.getID()).toString());
                }

                player.sendMessage(new StringBuilder().append("§2Type: §6").append(str).toString());
                player.sendMessage(new StringBuilder().append("§2Owner: §6").append(entity.getOwner()).toString());

                if ((entity.getType() == 1) && (admin)) {
                    player.sendMessage(new StringBuilder().append("§2Authed players: ").append(str1).toString());
                }

                if (admin) {
                    String world = com.griefcraft.util.StringUtils.capitalizeFirstLetter(entity.getWorldName());
                    player.sendMessage(new StringBuilder().append("§2World: §6").append(world).toString());
                    player.sendMessage(new StringBuilder().append("§2Location: §6{").append(entity.getX()).append(", ").append(entity.getY()).append(", ").append(entity.getZ()).append("}").toString());
                    player.sendMessage(new StringBuilder().append("§2Date created: §6").append(entity.getDate()).toString());
                }

                if (this.lwc.notInPersistentMode(player.getName())) {
                	this.memoryDatabase.unregisterAllActions(player.getName());
                }
                return false;
            }
            if (op_drop) {
                access = this.lwc.canAccessChest(player, entity);
                if (!access) {
                    player.sendMessage("§4You cannot use a chest that you cannot access as a drop transfer target.");
                    player.sendMessage("§4If this is a passworded chest, please unlock it before retrying.");
                    player.sendMessage("§4Use \"/lwc droptransfer select\" to try again.");
                } else {
                    if (cblist == null) {
                        player.sendMessage("§4You need to select a chest as the Drop Transfer target!");
                        this.memoryDatabase.unregisterAllActions(player.getName());
                        return false;
                    }
                    for (iBlocks = cblist.iterator();  iBlocks.hasNext();) {
                        ComplexBlock cb = iBlocks.next();
                        if ((!(cb instanceof Chest)) && (!(cb instanceof DoubleChest))) {
                            player.sendMessage("§4You need to select a chest as the Drop Transfer target!");
                            this.memoryDatabase.unregisterAllActions(player.getName());
                            return false;
                        }
                    }

                    this.memoryDatabase.registerMode(player.getName(), "dropTransfer", new StringBuilder().append("f").append(entity.getID()).toString());
                    player.sendMessage("§2Successfully registered chest as drop transfer target.");
                }
                this.memoryDatabase.unregisterAllActions(player.getName());

                return false;
            }
            if (op_free) {
                if ((this.lwc.isAdmin(player)) || (entity.getOwner().equals(player.getName()))) {
                    player.sendMessage("§aRemoved lock on the chest succesfully!");
                    this.physicalDatabase.unregisterProtectedEntity(worldID, entity.getX(), entity.getY(), entity.getZ());
                    this.physicalDatabase.unregisterProtectionRights(entity.getID());
                    if (this.lwc.notInPersistentMode(player.getName())) {
                        this.memoryDatabase.unregisterAllActions(player.getName());
                    }
                    return false;
                }
                player.sendMessage("§4You do not own that chest!");
                if (this.lwc.notInPersistentMode(player.getName())) {
                    this.memoryDatabase.unregisterAllActions(player.getName());
                }
                return true;
            }
            if (op_modify) {
                if (this.lwc.canAdminChest(player, entity)) {
                    action = this.memoryDatabase.getAction("modify", player.getName());

                    String str3 = action.getData();
                    String[] splits = { "" };

                    if (str3.length() > 0) {
                        splits = str3.split(" ");
                    }

                    if (this.lwc.notInPersistentMode(player.getName())) {
                        this.memoryDatabase.unregisterAllActions(player.getName());
                    }

                    for (String str : splits) {
                        int m = 0;
                        n = 0;
                        i1 = 1;

                        if (str.startsWith("-")) {
                            m = 1;
                            str = str.substring(1);
                        }

                        if (str.startsWith("@")) {
                            n = 1;
                            str = str.substring(1);
                        }

                        if (str.toLowerCase().startsWith("g:")) {
                            i1 = 0;
                            str = str.substring(2);
                        }

                        int i2 = this.physicalDatabase.loadProtectedEntity(worldID, block.getX(), block.getY(), block.getZ()).getID();

                        if (m == 0) {
                            this.physicalDatabase.unregisterProtectionRights(i2, str);
                            this.physicalDatabase.registerProtectionRights(i2, str, n != 0 ? 1 : 0, i1);
                            player.sendMessage(new StringBuilder().append("§aRegistered rights for §6").append(str).append("§2").append(" ").append(n != 0 ? "[§4ADMIN§6]" : "").append(" [").append(i1 == 1 ? "Player" : "Group").append("]").toString());
                        } else {
                            this.physicalDatabase.unregisterProtectionRights(i2, str);
                            player.sendMessage(new StringBuilder().append("§aRemoved rights for §6").append(str).append("§2").append(" [").append(i1 == 1 ? "Player" : "Group").append("]").toString());
                        }
                    }
                    return false;
                }
                player.sendMessage("§4You do not own that chest!");
                if (this.lwc.notInPersistentMode(player.getName())) {
                    this.memoryDatabase.unregisterAllActions(player.getName());
                }
                return true;
            }

        }

        if (op_drop) {
            player.sendMessage("§4Cannot select unregistered chest as drop transfer target.");
            player.sendMessage("§4Use \"/lwc droptransfer select\" to try again.");
            this.memoryDatabase.unregisterAllActions(player.getName());

            return false;
        }

        if ((op_info) || (op_free)) {
            player.sendMessage("§4Chest is unregistered");
            if (this.lwc.notInPersistentMode(player.getName())) {
                this.memoryDatabase.unregisterAllActions(player.getName());
            }
            return false;
        }

        if (((op_create) || (op_modify)) && (i == 0)) {
            if (!this.lwc.canAdminChest(player, entity))
                player.sendMessage("§4You do not own that chest!");
            else {
                player.sendMessage("§4You have already registered that chest!");
            }
            if (this.lwc.notInPersistentMode(player.getName())) {
                this.memoryDatabase.unregisterAllActions(player.getName());
            }
            return true;
        }

        if ((i != 0) && (op_create)) {
            action = this.memoryDatabase.getAction("create", player.getName());

            String str = action.getData();
            String[] split = str.split(" ");
            String cmd = split[1].toLowerCase();
            String str3 = "";

            if (split.length > 1) {
                for (int i2 = 1; i2 < split.length; i2++) {
                    str3 = new StringBuilder().append(str3).append(split[i2]).append(" ").toString();
                }
            }

            if (this.lwc.enforceChestLimits(player)) {
                if (this.lwc.notInPersistentMode(player.getName())) {
                    this.memoryDatabase.unregisterAllActions(player.getName());
                }
                return false;
            }

        /*  if ((!this.lwc.isAdmin(player)) && (this.lwc.isInCuboidSafeZone(player))) {
         *      player.sendMessage("§4You need to be in a Cuboid-protected safe zone to do that!");
         *      this.memoryDatabase.unregisterAllActions(player.getName());
         *      return false;
         *	}
	     */
            
            if (cmd.equals("public")) {
                this.physicalDatabase.registerProtectedEntity(worldID, 0, player.getName(), "", block.getX(), block.getY(), block.getZ());
                player.sendMessage("§2Created public protection successfully");
            } else {
                String str4;
                ComplexBlock cb;
                if (cmd.equals("password")) {
                    str4 = action.getData().substring("password ".length());
                    str4 = this.lwc.encrypt(str4);

                    this.physicalDatabase.registerProtectedEntity(worldID, 1, player.getName(), str4, block.getX(), block.getY(), block.getZ());
                    this.memoryDatabase.registerPlayer(player.getName(), this.physicalDatabase.loadProtectedEntity(worldID, block.getX(), block.getY(), block.getZ()).getID());
                    player.sendMessage("§2Created password protection successfully");
                    player.sendMessage("§aFor convenience, you don't have to enter your password until");
                    player.sendMessage("§ayou next log in");

                    if (action != null) {
                    for (Iterator<ComplexBlock> it = cblist.iterator(); it.hasNext();) {
                        cb = it.next();
                        if (cb != null) {
                            cb.update();
                        }
                    }}
                } else if (cmd.equals("private")) {
                    str4 = action.getData();
                    String[] split2 = new String[0];

                    if (str4.length() > "private ".length()) {
                        str4 = str4.substring("private ".length());
                        split2 = str4.split(" ");
                    }

                    this.physicalDatabase.registerProtectedEntity(worldID, 2, player.getName(), "", block.getX(), block.getY(), block.getZ());

                    player.sendMessage("§2Created private protection successfully");

                    for (String str5 : split2) {
                        int i3 = 0;
                        int i4 = 1;

                        if (str5.startsWith("@")) {
                            i3 = 1;
                            str5 = str5.substring(1);
                        }

                        if (str5.toLowerCase().startsWith("g:")) {
                            i4 = 0;
                            str5 = str5.substring(2);
                        }

                        this.physicalDatabase.registerProtectionRights(this.physicalDatabase.loadProtectedEntity(worldID, block.getX(), block.getY(), block.getZ()).getID(), str5, i3 != 0 ? 1 : 0, i4);
                        //player.sendMessage(new StringBuilder().append("§aRegistered rights for §6").append(str5).append(": ").append(i3 != 0 ? "[§4ADMIN§6]" : "").append(" [").append(i4 == 1 ? "Player" : "Group").append("]").toString());
                    }
                }
            }
            if (this.lwc.notInPersistentMode(player.getName())) {
                this.memoryDatabase.unregisterAllActions(player.getName());
            }
        }

        return !access;
    }

    private boolean isUpperDoor(Block b) {
        int data = b.getWorld().getBlockData(b.getX(), b.getY(), b.getZ());
        return ((data & 8) == 8);
    }

    public boolean onCommand(Player player, String[] args) {
        String str1 = args[0].substring(1);
        String str2 = "";
        String[] str = args.length > 1 ? new String[args.length - 1]
                : new String[0];

        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                args[i] = args[i].trim();

                if (args[i].isEmpty()) {
                    continue;
                }
                str[(i - 1)] = args[i];
                str2 = new StringBuilder().append(str2).append(args[i]).append(" ").toString();
            }
        }

        if (str1.equals("cpublic")) {
            return onCommand(player, "/lwc -c public".split(" "));
        }
        if (str1.equals("cpassword")) {
            return onCommand(player, new StringBuilder().append("/lwc -c password ").append(str2).toString().split(" "));
        }
        if (str1.equals("cprivate")) {
            return onCommand(player, "/lwc -c private".split(" "));
        }
        if (str1.equals("cinfo")) {
            return onCommand(player, "/lwc -i".split(" "));
        }
        if (str1.equals("cunlock")) {
            return onCommand(player, "/lwc -u".split(" "));
        }

        if (!player.canUseCommand(args[0])) {
            return false;
        }

        if (!"lwc".equalsIgnoreCase(str1)) {
            return false;
        }

        if (str.length == 0) {
            this.lwc.sendFullHelp(player);
            return true;
        }

        for (LWC_Command localCommand : this.lwc.getCommands()) {
            if (!localCommand.validate(this.lwc, player, str)) {
                continue;
            }
            localCommand.execute(this.lwc, player, str);
            return true;
        }

        return false;
    }

    public void onDisconnect(Player player) {
        this.memoryDatabase.unregisterPlayer(player.getName());
        this.memoryDatabase.unregisterUnlock(player.getName());
        this.memoryDatabase.unregisterChest(player.getName());
        this.memoryDatabase.unregisterAllActions(player.getName());
    }

    public boolean onExplode(Block block) {
        int worldID = block.getWorld().getType().getId();
        int i = this.physicalDatabase.loadProtectedEntities(worldID, block.getX(), block.getY(), block.getZ(), 4).size() > 0 ? 1 : 0;
        return i != 0;
    }

    public boolean onItemDrop(Player player, ItemEntity itementity) {
    	Item dropItem = itementity.getItem();
        String playerName = player.getName();
        int i = this.lwc.getPlayerDropTransferTarget(playerName);

        if ((i == -1) || (!this.lwc.isPlayerDropTransferring(playerName))) {
            return false;
        }

        if (!this.physicalDatabase.doesChestExist(i)) {
            player.sendMessage("§4Your drop transfer target was unregistered and/or destroyed.");
            player.sendMessage("§4Please re-register a target chest. Drop transfer will be deactivated.");

            this.memoryDatabase.unregisterMode(playerName, "dropTransfer");
            return false;
        }

        Entity localEntity = this.physicalDatabase.loadProtectedEntity(i);

        if (localEntity == null) {
            player.sendMessage("§4An unknown error occured. Drop transfer will be deactivated.");

            this.memoryDatabase.unregisterMode(playerName, "dropTransfer");
            return false;
        }

        if (!this.lwc.canAccessChest(player, localEntity)) {
            player.sendMessage("§4You have lost access to your target chest.");
            player.sendMessage("§4Please re-register a target chest. Drop transfer will be deactivated.");

            this.memoryDatabase.unregisterMode(playerName, "dropTransfer");
            return false;
        }
        World world = etc.getServer().getWorld(localEntity.getWorldID());
        List<ComplexBlock> chestList = this.lwc.getEntitySet(world, localEntity.getX(), localEntity.getY(),
                localEntity.getZ());
        int amount = dropItem.getAmount();

        for (ComplexBlock localComplexBlock : chestList) {
            Inventory chest = (Inventory) localComplexBlock;
            Item item;
            while ((((item = chest.getItemFromId(dropItem.getItemId(), 63)) != null) || (chest.getEmptySlot() != -1)) && (amount > 0)) {
                if (item != null) {
                    int k = Math.min(64 - item.getAmount(), dropItem.getAmount());
                    chest.setSlot(dropItem.getItemId(), item.getAmount() + k, item.getSlot());
                    amount -= k;
                    continue;
                }
                if (amount > 0) {
                    chest.addItem(new Item(dropItem.getItemId(), amount));
                    amount = 0;
                }
            }
            localComplexBlock.update();

            if (amount == 0) {
                break;
            }
        }

      //  player.sendMessage(String.format("Amount dropped: %d", dropItem.getAmount()));
      //  player.sendMessage(String.format("Amount left: %d", amount));
       // if (dropItem.getAmount() - amount > 0) {
       //     player.getInventory().removeItem(dropItem.getItemId(), dropItem.getAmount() - amount);
       // }

        if (amount > 0) {
            player.sendMessage("§4Your chest is full. Drop transfer will be deactivated.");
            player.sendMessage("§4Any remaining quantity that could not be stored will be returned.");
            this.memoryDatabase.unregisterMode(playerName, "dropTransfer");
            this.memoryDatabase.registerMode(playerName, "dropTransfer", new StringBuilder().append("f").append(i).toString());
            dropItem.setAmount(amount);
            player.getInventory().addItem(dropItem);
        }
        player.getInventory().update();
        itementity.destroy();
        return false;
    }

    public boolean onOpenInventory(HookParametersOpenInventory openInventory) {
    	Player player = openInventory.getPlayer();
    	Inventory inventory = openInventory.getInventory();
    	
        if ((this.lwc.isAdmin(player)) && (!this.debugMode)) {
            return false;
        }

        if ((inventory instanceof Workbench)) {
            return false;
        }

        if ((inventory instanceof EnchantmentTable)) { return false; }

        ComplexBlock cb = (ComplexBlock) inventory;

        if (!isProtectable(cb.getBlock())) {
            return false;
        }

        List<ComplexBlock> localList = this.lwc.getEntitySet(player.getWorld(), cb.getX(), cb.getY(), cb.getZ());
        boolean access = true;

        for (ComplexBlock cb2 : localList) {
            if (cb2 == null) {
                continue;
            }
            int worldID = player.getWorld().getType().getId();
            Entity entity = this.physicalDatabase.loadProtectedEntity(worldID, cb2.getX(), cb2.getY(), cb2.getZ());

            if (entity == null) {
                continue;
            }
            access = this.lwc.canAccessChest(player, entity);

            switch (entity.getType()) {
            case 1:
                if (access){
                    break;
                }
                this.memoryDatabase.unregisterUnlock(player.getName());
                this.memoryDatabase.registerUnlock(player.getName(), entity.getID());

                player.sendMessage("§4This chest is locked.");
                player.sendMessage("§4Type §6/lwc -u <password>§4 to unlock it");
                break;
            case 2:
                if (access){
                	break;
                }
                player.sendMessage("§4This chest is locked with a magical spell.");
            }

        }

        return !access;
    }
    
    public boolean onBlockRightClick(Player player, Block block, Item iih){
    	int worldID = player.getWorld().getType().getId();
    	if (isUpperDoor(block)) {
            block = block.getWorld().getBlockAt(block.getX(), block.getY()-1, block.getZ());
        }
    	Entity localEntity = this.physicalDatabase.loadProtectedEntity(worldID, block.getX(), block.getY(), block.getZ());
    	boolean bool1 = this.lwc.canAccessChest(player, localEntity);
    	return !bool1;
    }

    private boolean isProtectable(Block paramBlock) {
        switch (paramBlock.getType()) {
        case 23: /* Dispensers */
            return true;
        case 54:
            return true;
        case 61:
        case 62:
        case 64:
            return true;
        }

        return false;
    }

    private boolean isComplexBlock(Block paramBlock) {
        switch (paramBlock.getType()) {
        case 23: /* Dispensers */
            return true;
        case 54:
            return true;
        case 61:
        case 62:
            return true;
        }

        return false;
    }

}