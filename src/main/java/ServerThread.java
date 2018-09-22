import java.io.*;
import java.net.*;
import java.util.*;

// json provider
import org.json.*;
import com.google.gson.*;
import com.google.gson.reflect.*;

public class ServerThread extends Thread {
    final Socket socket;
    final DataInputStream dis;
    final DataOutputStream dos;

    final private DatabaseProvider dp;

    boolean isLoggedIn = false;
    String username;
    String IP;

    QueryType qt = null;
    Map<String, Object> param = null;

    public ServerThread(
        Socket socket, DataInputStream dis, DataOutputStream dos, DatabaseProvider dp) {
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
        this.dp = dp;
    }

    @Override
    public void run() {
        // 1. verifies username and password
        // 2. disconnect on failure
        // 3. wait for room selection
        // 4. connect client to room
        // 5. disconnect from server

        String[] result = null;
        while (true) {
            receive_msg();
            result = get_result();
            return_result(result);

            if (qt == QueryType.EXIT)
                break;
        }
        exit();
    }

    protected void receive_msg() {
        try {
            byte[] encoded_msg = dis.readUTF().getBytes();
            String json_msg = new String(Base64.getDecoder().decode(encoded_msg));

            System.out.println(json_msg);

            // using org.json?
            // JSONObject jo = new JSONObject(json_msg);
            // System.out.println(jo.toString(4));

            // using gson?
            // System.out.println(json_msg);
            Gson g = new Gson();
            Map<String, Object> map_msg =
                g.fromJson(json_msg, new TypeToken<Map<String, Object>>() {}.getType());

            String task = (String) map_msg.get("task");
            if (task.equals("signin"))
                qt = QueryType.SIGNIN;
            else if (task.equals("signout"))
                qt = QueryType.SIGNOUT;
            else if (task.equals("signup"))
                qt = QueryType.SIGNUP;
            else if (task.equals("chooseroom"))
                qt = QueryType.CHOOSEROOM;
            else if (task.equals("exit"))
                qt = QueryType.EXIT;
            else
                qt = QueryType.UNKNOWN;

            // param is {} so it is ok to work like this
            @SuppressWarnings("unchecked")
            Map<String, Object> p = (Map<String, Object>) map_msg.get("param");
            param = p;

        } catch (IOException ex) {
            // pass
        } catch (IllegalArgumentException ex) {
            qt = QueryType.UNKNOWN;
        }
    }

    protected String[] get_result() {
        int status_code = -1;
        String msg = "Unknown request";
        switch (qt) {
            case SIGNIN:
                String username = (String) param.get("username");
                String password = (String) param.get("password");
                isLoggedIn = signin(username, password);
                if (isLoggedIn) {
                    status_code = 200;
                    msg = "Logged in successfully";
                } else {
                    status_code = 403;
                    msg = "Wrong credentials";
                }
                break;
            case EXIT:
                status_code = 0;
                msg = "EXIT";
                break;

            default:
                break;
        }

        String[] result = {Integer.toString(status_code), msg};
        return result;
    }

    protected void return_result(String[] result) {
        try {
            String tosend = "{\"status\":" + result[0] + ",\"msg\":\"" + result[1] + "\"}";

            byte[] encoded_msg = Base64.getEncoder().encode(tosend.getBytes());
            tosend = new String(encoded_msg);
            dos.writeUTF(tosend);
        } catch (IOException ex) {
            // pass
        }
    }

    protected boolean signin(String username, String password) {
        if (username == null || password == null)
            return false;
        if (!dp.signIn(username, password)) {
            return false;
        } else {
            return true;
        }
    }

    // return password
    protected String signup(String username) {
        return "random";
    }

    // list of IP Address
    protected List<String> chooseroom(int room) {
        return null;
    }

    protected void exit() {
        try {
            socket.close();
            dis.close();
            dos.close();
        } catch (IOException ex) {
            // pass
        }
    }
}

enum QueryType { SIGNIN, SIGNUP, SIGNOUT, CHOOSEROOM, EXIT, UNKNOWN }
