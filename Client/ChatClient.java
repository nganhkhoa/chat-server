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

    private Gson gson = new Gson();

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
        try {
            while (true) {
                tosend = build_request();
                dos.writeUTF(tosend);

                byte[] encoded_msg = dis.readUTF().getBytes();
                String json_msg = new String(Base64.getDecoder().decode(encoded_msg));

                Response response = gson.fromJson(json_msg, Response.class);

                if (response.getStatus() == 0)
                    break;

                System.out.println("----------");
                System.out.println(response.getMsg());

                List<String> result = response.getResult();
                if (result == null)
                    System.out.println(response.success());
                else
                    for (String s : result) System.out.println(s);
                System.out.println("----------");
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

                tosend = gson.toJson(new Request(
                    "signin", Arrays.asList(username, password, "198.168.0.1", "7778")));
            }
        } else if (task.equals("signup")) {
            String param = signup();
            if (param != null) {
                String username = param;
                tosend = gson.toJson(new Request("signup", Arrays.asList(username)));
            }
        } else {
            tosend = gson.toJson(new Request("exit"));
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

    protected String signup() {
        // username
        System.out.print("Username: ");
        String username = scn.nextLine();
        if (username.equals(""))
            return null;
        return username;
    }

    protected void chooseroom() {}
}
