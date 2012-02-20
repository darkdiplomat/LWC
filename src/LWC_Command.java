public abstract interface LWC_Command{

    public abstract void execute(LWC lwc, Player player, String[] args);
    public abstract boolean validate(LWC lwc, Player player, String[] args);

}
