package com.griefcraft.sql;

import com.griefcraft.model.Action;
import com.griefcraft.model.Entity;
import com.griefcraft.util.Performance;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MemDB extends Database {
	Logger log = Logger.getLogger("Minecraft");
    public Action getAction(String act, String name) {
        try {
        	Action theAction = new Action();
            PreparedStatement ps = this.connection.prepareStatement("SELECT * FROM `actions` WHERE `player` = ? AND `action` = ?");
            ps.setString(1, name);
            ps.setString(2, act);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                theAction.setID(rs.getInt("id"));
                theAction.setAction(rs.getString("action"));
                theAction.setPlayer(rs.getString("player"));
                theAction.setChestID(rs.getInt("chest"));
                theAction.setData(rs.getString("data"));
            }
            ps.close();
            Performance.addMemDBQuery();
            return theAction;
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
        return null;
    }

    public int getActionID(String act, String name) {
        try {
            int i = -1;
            PreparedStatement ps = this.connection.prepareStatement("SELECT `chest` FROM `actions` WHERE `action` = ? AND `player` = ?");
            ps.setString(1, act);
            ps.setString(2, name);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
            	i = rs.getInt("chest");
            }
            rs.close();
            Performance.addMemDBQuery();
            return i;
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
        return -1;
    }

    public List<String> getActions(String name) {
    	ArrayList<String> actlist = new ArrayList<String>();
        try {
        	PreparedStatement ps = this.connection.prepareStatement("SELECT `action` FROM `actions` WHERE `player` = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	actlist.add(rs.getString("action"));
            }
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
        return actlist;
    }

    public String getDatabasePath() {
        return ":memory:";
    }

    public String getLockPassword(String name) {
        try {
            String pass = "";
            PreparedStatement ps = this.connection.prepareStatement("SELECT `password` FROM `locks` WHERE `player` = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
            	pass = rs.getString("password");
            }
            ps.close();
            Performance.addMemDBQuery();
            return pass;
        }
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
        return null;
    }

    public String getModeData(String name, String mode) {
    	String data = null;
    	try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT `data` from `modes` WHERE `player` = ? AND `mode` = ?");
            ps.setString(1, name);
            ps.setString(2, mode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                data = rs.getString("data");
            }
            ps.close();
            Performance.addMemDBQuery();
        } 
    	catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
        return data;
    }

    public List<String> getModes(String name) {
        ArrayList<String> modes = new ArrayList<String>();
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT * from `modes` WHERE `player` = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modes.add(rs.getString("mode"));
            }
            ps.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
        return modes;
    }

    public List<String> getSessionUsers(int id) {
        ArrayList<String> users = new ArrayList<String>();
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT `player` FROM `sessions` WHERE `chest` = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                users.add(rs.getString("player"));
            }
        }
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
        return users;
    }

    public int getUnlockID(String name) {
        return getActionID("unlock", name);
    }

    public boolean hasAccess(String name, Entity entity) {
        if (entity == null){
            return true;
        }
        return hasAccess(name, entity.getID());
    }

    public boolean hasAccess(String name, int id) {
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT `player` FROM `sessions` WHERE `chest` = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (name.equals(rs.getString("player"))) {
                    ps.close();
                    Performance.addMemDBQuery();
                    return true;
                }
            }
            ps.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
        return false;
    }

    public boolean hasMode(String name, String mode) {
        List<String> modes = getModes(name);
        return (modes.size() > 0) && (modes.contains(mode));
    }

    public boolean hasPendingAction(String act, String name) {
        return getAction(act, name) != null;
    }

    public boolean hasPendingChest(String name) {
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT `id` FROM `locks` WHERE `player` = ?");
            ps.setString(1, name);
            ResultSet localResultSet = ps.executeQuery();
            if (localResultSet.next()) {
                ps.close();
                Performance.addMemDBQuery();
                return true;
            }
            ps.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
        return false;
    }

    public boolean hasPendingUnlock(String name) {
        return getUnlockID(name) != -1;
    }

    public void load() {
        try {
            Statement st = this.connection.createStatement();
            log("Creating memory tables");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS 'sessions' (id INTEGER PRIMARY KEY,player TEXT,chest INTEGER);");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS 'locks' (id INTEGER PRIMARY KEY,player TEXT,password TEXT);");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS 'actions' (id INTEGER PRIMARY KEY,action TEXT,player TEXT,chest INTEGER,data TEXT);");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS 'modes' (id INTEGER PRIMARY KEY,player TEXT,mode TEXT,data TEXT);");
            st.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
    }

    public int pendingCount() {
        int count = 0;
        try {
            Statement st = this.connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT `id` FROM `locks`");
            while (rs.next()){
            	count++;
            }
            rs.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
        return count;
    }

    public void registerAction(String act, String name) {
        try {
            unregisterAction(act, name);
            PreparedStatement ps = this.connection.prepareStatement("INSERT INTO `actions` (action, player, chest) VALUES (?, ?, ?)");
            ps.setString(1, act);
            ps.setString(2, name);
            ps.setInt(3, -1);
            ps.executeUpdate();
            ps.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
    }

    public void registerAction(String act, String name, int id) {
        try {
            unregisterAction(act, name);
            PreparedStatement ps = this.connection.prepareStatement("INSERT INTO `actions` (action, player, chest) VALUES (?, ?, ?)");
            ps.setString(1, act);
            ps.setString(2, name);
            ps.setInt(3, id);
            ps.executeUpdate();
            ps.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
    }

    public void registerAction(String act, String name, String data) {
        try {
            unregisterAction(act, name);
            PreparedStatement ps = this.connection.prepareStatement("INSERT INTO `actions` (action, player, data) VALUES (?, ?, ?)");
            ps.setString(1, act);
            ps.setString(2, name);
            ps.setString(3, data);
            ps.executeUpdate();
            ps.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
    }

    public void registerChest(String name, String pass) {
        try {
            PreparedStatement ps = this.connection.prepareStatement("INSERT INTO `locks` (player, password) VALUES (?, ?)");
            ps.setString(1, name);
            ps.setString(2, pass);
            ps.executeUpdate();
            ps.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
    }

    public void registerMode(String name, String mode) {
        try {
            PreparedStatement ps = this.connection.prepareStatement("INSERT INTO `modes` (player, mode) VALUES (?, ?)");
            ps.setString(1, name);
            ps.setString(2, mode);
            ps.executeUpdate();
            ps.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
    }

    public void registerMode(String name, String mode, String data) {
        try {
            PreparedStatement ps = this.connection.prepareStatement("INSERT INTO `modes` (player, mode, data) VALUES (?, ?, ?)");
            ps.setString(1, name);
            ps.setString(2, mode);
            ps.setString(3, data);
            ps.executeUpdate();
            ps.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
    }

    public void registerPlayer(String name, int id) {
        try {
            PreparedStatement ps = this.connection.prepareStatement("INSERT INTO `sessions` (player, chest) VALUES(?, ?)");
            ps.setString(1, name);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
    }

    public void registerUnlock(String name, int id) {
        registerAction("unlock", name, id);
    }

    public int sessionCount() {
        int i = 0;
        try {
            Statement st = this.connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT `id` FROM `sessions`");
            while (rs.next())
                i++;
            st.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
        return i;
    }

    public void unregisterAction(String act, String name) {
        try {
            PreparedStatement ps = this.connection.prepareStatement("DELETE FROM `actions` WHERE `action` = ? AND `player` = ?");
            ps.setString(1, act);
            ps.setString(2, name);
            ps.executeUpdate();
            ps.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
    }

    public void unregisterAllActions(String name) {
        try {
            PreparedStatement ps = this.connection.prepareStatement("DELETE FROM `actions` WHERE `player` = ?");
            ps.setString(1, name);
            ps.executeUpdate();
            ps.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
    }

    public void unregisterAllChests() {
        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate("DELETE FROM `locks`");
            st.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
    }

    public void unregisterAllModes(String name) {
        try {
            PreparedStatement ps = this.connection.prepareStatement("DELETE FROM `modes` WHERE `player` = ?");
            ps.setString(1, name);
            ps.executeUpdate();
            ps.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
    }

    public void unregisterChest(String name) {
        try {
            PreparedStatement ps = this.connection.prepareStatement("DELETE FROM `locks` WHERE `player` = ?");
            ps.setString(1, name);
            ps.executeUpdate();
            ps.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
    }

    public void unregisterMode(String name, String mode) {
        try {
            PreparedStatement ps = this.connection.prepareStatement("DELETE FROM `modes` WHERE `player` = ? AND `mode` = ?");
            ps.setString(1, name);
            ps.setString(2, mode);
            ps.executeUpdate();
            ps.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
    }

    public void unregisterPlayer(String name) {
        try {
            PreparedStatement ps = this.connection.prepareStatement("DELETE FROM `sessions` WHERE `player` = ?");
            ps.setString(1, name);
            ps.executeUpdate();
            ps.close();
            Performance.addMemDBQuery();
        } 
        catch (SQLException SQLE) {
        	SQLE.printStackTrace();
        }
    }

    public void unregisterUnlock(String name) {
        unregisterAction("unlock", name);
    }
}
