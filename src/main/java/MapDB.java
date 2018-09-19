import org.mapdb.*;
// serialization in db
import org.apache.commons.lang.SerializationUtils;
import java.lang.*;

public class MapDB extends DatabaseProvider {
    private DB db;
    private HTreeMap<String, byte[]> accounts;

    public boolean connect() throws Exception {
        try {
            db = DBMaker.fileDB("database.db").make();

            // store Account as class --> serialize to bytes
            // ID --> Class (must find by id)
            // Name --> Password (better)
            accounts = db.hashMap("accounts")
                           .keySerializer(Serializer.STRING)
                           .valueSerializer(Serializer.BYTE_ARRAY)
                           .counterEnable()
                           .createOrOpen();

        } catch (Exception ex) {
            throw ex;
        }
        return true;
    }

    public boolean close() {
        db.close();
        return true;
    }

    protected HTreeMap<String, byte[]> getAccountDatabase() {
        return accounts;
    }

    protected Object getRoomDatabase() {
        return null;
    }

    public boolean signIn(String username, String password) {
        byte[] b = accounts.get(username);
        if (b == null)
            return false;
        Account account = (Account) SerializationUtils.deserialize(b);
        if (account.getPassword().equals(password))
            return true;
        return false;
    }

    public void addUser(Account account) {
        accounts.put(account.getUsername(), SerializationUtils.serialize(account));
        db.commit();
    }

    public boolean existUser(String username) {
        // System.out.println(accounts.get(username));
        return accounts.get(username) != null;
    }
}
