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
        return true;
    }

    public String signUp(String name) {
        return "Random Password";
    }
}
