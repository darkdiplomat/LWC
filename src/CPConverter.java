import com.griefcraft.sql.PhysDB;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;

public class CPConverter implements Runnable {
	private String[] CHESTS_FILES = { "../lockedChests.txt", "lockedChests.txt" };
	private int converted = 0;
	private Player player;
	private PhysDB physicalDatabase;

	public static void main(String[] args) throws Exception {
		new CPConverter();
	}

	public CPConverter() {
		new Thread(this).start();
		this.physicalDatabase = new PhysDB();
    }

	public CPConverter(Player player) {
		this();
		this.player = player;
	}

	public void convertChests() throws FileNotFoundException, IOException {
		File localFile = null;
		for (String str : this.CHESTS_FILES) {
			localFile = new File(str);
			if ((localFile != null) && (localFile.exists())){
				break;
			}
		}
		if ((localFile == null) || (!localFile.exists())){
			throw new FileNotFoundException("No Chest Protect chest database found");
		}
		//int count = 0;
		BufferedReader reader = new BufferedReader(new FileReader(localFile));
		String line = "";
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			//count++;
			if (line.startsWith("#")){
				continue;
			}
			String[] split = line.split(",");
            if (split.length < 5){
                continue;
            }
            String str1 = split[0];
            int x = Integer.parseInt(split[1]);
            int y = Integer.parseInt(split[2]);
            int z = Integer.parseInt(split[3]);
            int i1 = Integer.parseInt(split[4]);
            int i2 = -1;
            String str2 = "";
            if (i1 == 1) {
                i1 = 0;
            } 
            else if (i1 > 1) {
                if (i1 == 3){
                	i2 = 0;
                }
                else if (i1 == 4){
                	i2 = 1;
                }
                i1 = 2;
            }
            if (split.length > 5){
                str2 = split[5].trim();
            }
            log(String.format("Registering chest to %s at location {%d,%d,%d}", new Object[] { str1, Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(z) }));
            this.physicalDatabase.registerProtectedEntity(0, i1, str1, "", x, y, z);
            this.converted += 1;
            if (i2 == -1){
            	continue;
            }
            int i3 = this.physicalDatabase.loadProtectedEntity(0, x, y, z).getID();
            String[] args = str2.split(";");
            for (String str3 : args) {
            	this.physicalDatabase.registerProtectionRights(i3, str3, 0, i2);
            	log(String.format(" -> Registering rights to %s on chest %d", new Object[] { str3, Integer.valueOf(i3) }));
            }
        }
    }

	public void log(String str) {
		System.out.println(str);
		if (this.player != null){
            this.player.sendMessage(str);
		}
    }

    public void run() {
    	try {
    		log("LWC Conversion tool for Chest Protect chests");
    		log("");
    		log("Initializing sqlite");
    		boolean bool = this.physicalDatabase.connect();
    		if (!bool){
    			throw new ConnectException("Failed to connect to the sqlite database");
    		}
    		this.physicalDatabase.load();
    		log("Done.");
    		log("Starting conversion of Chest Protect chests");
    		log("");
    		convertChests();
    		log("Done.");
            log("");
            log("Converted >" + this.converted + "< Chest Protect chests to LWC");
            log("LWC database now holds " + this.physicalDatabase.entityCount() + " protected chests!");
    	} catch (Exception e) {
    		e.printStackTrace();
        }
    }
}