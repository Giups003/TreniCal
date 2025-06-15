package it.unical.trenical.client.session;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class UserManager {
    private static final String USERS_FILE = System.getProperty("user.home") + "/.trenical_users.json";
    private static Map<String, User> users = new HashMap<>();
    static {
        loadUsers();
    }
    public static class User {
        public String username;
        public String email;
        public String password;
        public boolean fidelityMember = false;
        public List<String> tickets = new ArrayList<>();
        public User(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }
    }
    public static synchronized boolean registerUser(String username, String email, String password) {
        if (users.containsKey(username)) return false;
        users.put(username, new User(username, email, password));
        saveUsers();
        return true;
    }
    public static synchronized boolean validateLogin(String username, String password) {
        User u = users.get(username);
        return u != null && u.password.equals(password);
    }
    public static synchronized boolean userExists(String username) {
        return users.containsKey(username);
    }
    public static synchronized String getEmail(String username) {
        User u = users.get(username);
        return u != null ? u.email : "";
    }
    public static synchronized boolean setFidelityMember(String username, boolean value) {
        User u = users.get(username);
        if (u == null) return false;
        u.fidelityMember = value;
        saveUsers();
        return true;
    }
    public static synchronized boolean isFidelityMember(String username) {
        User u = users.get(username);
        return u != null && u.fidelityMember;
    }
    public static synchronized List<String> getTickets(String username) {
        User u = users.get(username);
        return u != null ? u.tickets : new ArrayList<>();
    }
    public static synchronized void setTickets(String username, List<String> tickets) {
        User u = users.get(username);
        if (u != null) {
            u.tickets = tickets;
            saveUsers();
        }
    }
    public static synchronized java.util.List<User> getAllUsers() {
        return new java.util.ArrayList<>(users.values());
    }
    private static void loadUsers() {
        users.clear();
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
                Files.writeString(Paths.get(USERS_FILE), "[]");
            } catch (IOException ignored) {}
            return;
        }
        try {
            String json = Files.readString(Paths.get(USERS_FILE)).trim();
            if (json.isEmpty() || !json.startsWith("[") || !json.endsWith("]")) {
                Files.writeString(Paths.get(USERS_FILE), "[]");
                return;
            }
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String username = obj.optString("username");
                String email = obj.optString("email");
                String password = obj.optString("password");
                boolean fidelityMember = obj.optBoolean("fidelityMember", false);
                List<String> tickets = new ArrayList<>();
                JSONArray ticketsArr = obj.optJSONArray("tickets");
                if (ticketsArr != null) {
                    for (int j = 0; j < ticketsArr.length(); j++) {
                        tickets.add(ticketsArr.getString(j));
                    }
                }
                if (!username.isEmpty() && !password.isEmpty()) {
                    User user = new User(username, email, password);
                    user.fidelityMember = fidelityMember;
                    user.tickets = tickets;
                    users.put(username, user);
                }
            }
        } catch (Exception e) {
            try { Files.writeString(Paths.get(USERS_FILE), "[]"); } catch (IOException ignored) {}
        }
    }
    private static void saveUsers() {
        JSONArray arr = new JSONArray();
        for (User u : users.values()) {
            JSONObject obj = new JSONObject();
            obj.put("username", u.username);
            obj.put("email", u.email);
            obj.put("password", u.password);
            obj.put("fidelityMember", u.fidelityMember);
            obj.put("tickets", u.tickets);
            arr.put(obj);
        }
        try (FileWriter fw = new FileWriter(USERS_FILE)) {
            fw.write(arr.toString());
        } catch (IOException ignored) {}
    }
}
