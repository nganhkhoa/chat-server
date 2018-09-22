import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;

// import org.json.*;
import com.google.gson.*;
import com.google.gson.reflect.*;

// Client class
public class ChatClient {
    private Socket s;
    private DataInputStream dis;
    private DataOutputStream dos;
    private InetAddress ip;
    private Scanner scn;

    public ChatClient() throws IOException {
        try {
            scn = new Scanner(System.in);

            // getting localhost ip
            ip = InetAddress.getByName("localhost");

            // establish the connection with server port 5056
            s = new Socket(ip, 7789);

            // obtaining input and out streams
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
        } catch (IOException ex) {
            // pass
            System.out.println("Cannot connect to server");
            throw ex;
        }
    }

    public void run() {
        String tosend = "";
        String msg = "";
        try {
            while (true) {
                tosend = build_request();
                dos.writeUTF(tosend);

                byte[] encoded_msg = dis.readUTF().getBytes();
                String json_msg = new String(Base64.getDecoder().decode(encoded_msg));

                Gson g = new Gson();
                Map<String, Object> map_msg =
                    g.fromJson(json_msg, new TypeToken<Map<String, Object>>() {}.getType());

                double status_code = (double) map_msg.get("status");
                if (status_code == 0)
                    break;
                msg = (String) map_msg.get("msg");
                System.out.println(msg);
            }

            // closing resources
            scn.close();
            dis.close();
            dos.close();
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            ChatClient client = new ChatClient();
            client.run();
        } catch (IOException ex) {
            return;
        }
    }

    protected String build_request() {
        System.out.print("Choose action: ");
        // either
        // + signin
        // + signup
        // + chooseroom
        // + exit
        String task = scn.nextLine();
        String tosend = null;

        if (task.equals("signin")) {
            String[] param = signin();
            if (param != null) {
                String username = param[0];
                String password = param[1];
                tosend = "{'task':'" + task + "','param':{'username':'" + username
                    + "','password':'" + password + "'}}";
                tosend = tosend.replace("'", "\"");
            }
        } else {
            tosend = "{\"task\":\"exit\",\"param\":{}}";
        }

        byte[] encoded_msg = Base64.getEncoder().encode(tosend.getBytes());
        tosend = new String(encoded_msg);

        return tosend;
    }

    protected String[] signin() {
        // username
        System.out.print("Username: ");
        String username = scn.nextLine();

        // password
        System.out.print("Password: ");
        String password = scn.nextLine();

        if (username.equals("") || password.equals(""))
            return null;

        String[] param = {username, password};
        return param;
    }

    protected void signup() {}

    protected void chooseroom() {}
}
