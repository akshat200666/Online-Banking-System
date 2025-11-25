package atm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple cache to hold Account objects loaded from DB.
 * Demonstrates Collections & Generics and thread-safe map.
 */
public class InMemoryCache {
    private final Map<Integer, Account> accounts = new ConcurrentHashMap<>();

    public Account get(int id) {
        return accounts.get(id);
    }

    public void put(Account a) {
        accounts.put(a.getAccountId(), a);
    }

    public void remove(int id) {
        accounts.remove(id);
    }

    // For demo only: load from DB
    public void loadFromDB(int id, AccountDAO dao) throws DatabaseException {
        Account a = dao.findById(id);
        if (a != null) put(a);
    }
}
