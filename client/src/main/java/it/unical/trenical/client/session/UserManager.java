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

/**
 * Gestisce sessioni utente e determinazione automatica tipo cliente.
 * Implementa Pattern Strategy per categorizzazione VIP/Corporate/Standard.
 */
public class UserManager {
    // Unifica con il file del server per evitare duplicazioni
    private static final String USERS_FILE = "C:/Users/Giuseppe/Documents/TreniCal/TreniCal/server/data/users.json";
    private static Map<String, User> users = new HashMap<>();
    static {
        loadUsers();
    }
    public static class User {
        public String username;
        public String email;
        public String password;
        public boolean fidelityMember = false;
        public String customerType = "standard"; // tipo cliente
        public List<String> tickets = new ArrayList<>();

        public User(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }

        /**
         * PATTERN STRATEGY - BUSINESS LOGIC CORRETTA
         * Determina il tipo di cliente in base alle regole di business.
         */
        public String getCustomerType() {
            // Se esplicitamente impostato e diverso da standard, usa quello (priorità massima)
            if (customerType != null && !customerType.equals("standard") && !customerType.isEmpty()) {
                return customerType;
            }

            if (email != null && !email.isEmpty()) {
                // Regola aziendale VIP: se l'email contiene parole chiave VIP
                if (isVipEmail(email)) {
                    this.customerType = "vip"; // Aggiorna anche il campo interno
                    return "vip";
                }

                if (isBusinessEmail(email)) {
                    this.customerType = "corporate"; // Aggiorna anche il campo interno
                    return "corporate";
                }
            }

            // DEFAULT: tutti gli altri sono standard
            this.customerType = "standard";
            return "standard";
        }

        /**
         * Imposta esplicitamente il tipo di cliente.
         */
        public void setCustomerType(String type) {
            this.customerType = type != null ? type.toLowerCase() : "standard";
        }

        /**
         * Regola business: determina se un'email è VIP.
         */
        private boolean isVipEmail(String email) {
            String[] vipKeywords = {"vip", "premium", "platinum", "gold", "elite", "executive", "priority"};
            String lowerEmail = email.toLowerCase();

            for (String keyword : vipKeywords) {
                if (lowerEmail.contains(keyword)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Regola business: determina se un'email è aziendale.
         */
        private boolean isBusinessEmail(String email) {
            String[] businessDomains = {
                    // Domini aziendali italiani
                    ".spa", ".srl", ".snc", ".sas", ".sapa",
                    // Domini aziendali internazionali
                    ".corp", ".company", ".business", ".enterprise", ".inc", ".ltd", ".llc",
                    // Altri pattern aziendali comuni
                    ".group", ".holding", ".industries", ".solutions", ".consulting",
                    ".services", ".tech", ".systems", ".global", ".international",
                    // Pattern email aziendali generici
                    "corporate", "business", "company", "enterprise", "azienda", "società"
            };
            String lowerEmail = email.toLowerCase();

            for (String domain : businessDomains) {
                if (lowerEmail.contains(domain)) {
                    return true;
                }
            }

            // Controllo aggiuntivo: se l'email non è da provider gratuiti comuni,
            // potrebbe essere aziendale
            String[] freeProviders = {"gmail", "yahoo", "hotmail", "outlook", "libero", "tiscali", "virgilio"};
            boolean isFreeProvider = false;
            for (String provider : freeProviders) {
                if (lowerEmail.contains(provider)) {
                    isFreeProvider = true;
                    break;
                }
            }

            // Se non è un provider gratuito e ha un dominio personalizzato,
            // consideralo potenzialmente aziendale
            if (!isFreeProvider && lowerEmail.contains("@") && !lowerEmail.endsWith(".com") && !lowerEmail.endsWith(".it")) {
                return true;
            }

            return false;
        }
    }
    public static synchronized boolean registerUser(String username, String email, String password) {
        if (users.containsKey(username)) return false;

        User newUser = new User(username, email, password);
        // DETERMINAZIONE AUTOMATICA del tipo utente SOLO durante la registrazione
        String autoDetectedType = newUser.getCustomerType();
        newUser.customerType = autoDetectedType; // Imposta il tipo determinato dall'email

        users.put(username, newUser);
        saveUsers();

        System.out.println("[REGISTRATION] Utente " + username + " registrato con tipo: " + autoDetectedType);
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
        if (u != null) {
            return u.email;
        }
        return "";
    }

    public static synchronized boolean isFidelityMember(String username) {
        User u = users.get(username);
        return u != null && u.fidelityMember;
    }
    public static synchronized List<String> getTickets(String username) {
        User u = users.get(username);
        return u != null ? u.tickets : new ArrayList<>();
    }

    public static synchronized java.util.List<User> getAllUsers() {
        return new java.util.ArrayList<>(users.values());
    }

    public static synchronized String getCustomerType(String username) {
        User u = users.get(username);
        return u != null ? u.getCustomerType() : "standard";
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
                String customerType = obj.optString("customerType", "standard"); // NUOVO
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
                    user.customerType = customerType; // NUOVO
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
            obj.put("customerType", u.customerType); // NUOVO
            obj.put("tickets", u.tickets);
            arr.put(obj);
        }
        try (FileWriter fw = new FileWriter(USERS_FILE)) {
            fw.write(arr.toString(2)); // Indentazione per leggibilità
        } catch (IOException ignored) {}
    }
}