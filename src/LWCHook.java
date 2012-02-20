public class LWCHook {
    LWC lwc;
    PluginInterface AccessCheck = new AccessCheck();

    public LWCHook(LWC lwc){
        this.lwc = lwc;
    }

    public class AccessCheck implements PluginInterface{
        
        public String getName(){
            return "LWC-AccessCheck";
        }
        
        public int getNumParameters() {
            return 2;
        }
        
        public String checkParameters(Object[] args) {
            if (args.length != getNumParameters()) {
                return "[LWC] Invalid amount of parameters. Proper Parameters are:' Player player, Block block '";
            }
            return null;
        }

        public Object run(Object[] args) {
            Player player = null;
            Block block = null;
            
            if(args[0] instanceof Player){
                player = (Player)args[0];
            }
            
            if(args[1] instanceof Block){
                block = (Block)args[1];
            }

            if(player != null && block != null){
                return lwc.canAccessChest(player, block.getX(), block.getY(), block.getZ());
            }
            return null;
        }
    }
}
