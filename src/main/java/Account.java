import java.lang.*;
import java.io.*;

class Account implements Serializable {
    static final long serialVersionUID = -6540111480558873206L;

    String name;
    String password;

    public Account(String name) {
        this.name = name;
        this.password = randomPassword();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return this.name;
    }

    static String randomPassword() {
        return "Random";
    }

    //     public static byte[] serialize(Account obj) throws IOException {
    //         ByteArrayOutputStream out = new ByteArrayOutputStream();
    //         ObjectOutputStream os = new ObjectOutputStream(out);
    //         os.writeObject(obj);
    //         return out.toByteArray();
    //     }
    //     public static Account deserialize(byte[] data) throws IOException, ClassNotFoundException
    //     {
    //         ByteArrayInputStream in = new ByteArrayInputStream(data);
    //         ObjectInputStream is = new ObjectInputStream(in);
    //         return is.readObject();
    //     }
}
