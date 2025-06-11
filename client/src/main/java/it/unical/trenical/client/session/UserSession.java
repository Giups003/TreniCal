package it.unical.trenical.client.session;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class UserSession {
    private static String username;
    private static boolean isAdmin = false;
    private static boolean loyaltyMember = false;
    private static final Preferences prefs = Preferences.userNodeForPackage(UserSession.class);
    private static final String LOYALTY_KEY = "loyaltyMember";
    private static String email;
    private static int userId = -1;
    private static List<String> tickets = new ArrayList<>(); // Puoi sostituire String con una classe Ticket se esiste

    static {
        // Carica lo stato fedeltà all'avvio
        loyaltyMember = prefs.getBoolean(LOYALTY_KEY, false);
    }

    public static void setUsername(String name) {
        username = name;
    }

    public static String getUsername() {
        return username;
    }

    public static boolean isLoggedIn() {
        return username != null && !username.trim().isEmpty();
    }

    public static void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public static boolean isAdmin() {
        return isAdmin;
    }

    public static boolean isLoyaltyMember() {
        return loyaltyMember;
    }

    public static void setLoyaltyMember(boolean value) {
        loyaltyMember = value;
        prefs.putBoolean(LOYALTY_KEY, value);
    }

    public static void setUserId(int id) {
        userId = id;
    }

    public static int getUserId() {
        return userId;
    }

    public static void setEmail(String mail) {
        email = mail;
    }

    public static String getEmail() {
        return email;
    }

    public static List<String> getTickets() {
        return tickets;
    }

    public static void setTickets(List<String> newTickets) {
        tickets = newTickets;
    }

    public static void clearTickets() {
        tickets.clear();
    }

    public static void reset() {
        username = null;
        isAdmin = false;
        loyaltyMember = false;
        email = null;
        userId = -1;
        prefs.remove(LOYALTY_KEY);
    }
}
