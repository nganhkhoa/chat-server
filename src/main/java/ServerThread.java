import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.lang.StringUtils;
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
    String Username;
    String IP;

    QueryType qt = QueryType.UNKNOWN;
    List<String> param = null;

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
        String json_msg = "";
        try {
            byte[] encoded_msg = dis.readUTF().getBytes();
            json_msg = new String(Base64.getDecoder().decode(encoded_msg));

            System.out.println("QUERY::" + json_msg);

            // using org.json?
            // JSONObject jo = new JSONObject(json_msg);
            // System.out.println(jo.toString(4));

            // using gson?
            // System.out.println(json_msg);
            Gson g = new Gson();
            Request req = g.fromJson(json_msg, Request.class );
            //req.print();
            System.out.print(req);
            
            String task = req.getTask();
            if (task.equals("signin"))
                qt = QueryType.SIGNIN;
            else if (task.equals("signout"))
                qt = QueryType.SIGNOUT;
            else if (task.equals("signup"))
                qt = QueryType.SIGNUP;
            else if (task.equals("chooseroom"))
                qt = QueryType.CHOOSEROOM;
            else if (task.equals("getip"))
                qt = QueryType.GETIP;
            else if (task.equals("exit"))
                qt = QueryType.EXIT;
            else
                qt = QueryType.UNKNOWN;

            // param is {} so it is ok to work like this
           // @SuppressWarnings("unchecked")
            param = req.getParam();


        } catch (IOException ex) {
            // pass
        } catch (IllegalArgumentException ex) {
            qt = QueryType.UNKNOWN;
        } catch (JsonParseException ex) {
            System.out.println("BAD_QUERY::"+json_msg);
            qt = QueryType.UNKNOWN;
        }
    }

    protected String[] get_result() {
        int status_code = -1;
        String msg = "Unknown request";
        String username = "";
        String password = "";
        String ip = "";
        String port = "";
        String[] IPPort = null;
        switch (qt) {
            case SIGNIN:
                username = param.get(0);
                password = param.get(1);
                isLoggedIn = signin(username, password);
                if (isLoggedIn) {
                    ip = param.get(2);
                    port = param.get(3);
                    if(makeOnline(username, ip, port)){
                        status_code = 200;
                        msg = "Logged in successfully";
                        this.Username = username;
                        this.IP = ip;
                    }
                    else{
                        status_code = 433; 
                        msg = "IP port Error!";
                    }

                } else {
                    status_code = 403;
                    msg = "Wrong credentials";
                }
                break;
            case SIGNUP:
                username = param.get(0);
                signup(username);
                status_code = 200;
                msg = "Signup successfully";
                break;
            case GETIP:
                username = param.get(0);
                IPPort = getIP(username);
                if(IPPort == null) {
                    status_code = 444;
                    msg = "User not online";           
                }
                else{
                    ip = IPPort[0];
                    port = IPPort[1];
                    status_code = 200;
                    msg = ip + ":" + port;   
                }
            case EXIT:
                status_code = 0;
                msg = "EXIT";
                dp.setOffline(this.Username);
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
        if (dp.existUser(username)) {
            return null;
        }
        else {
            Account new_account = new Account(username);
            dp.addUser(new_account);
            return new_account.getPassword();
        }
    }

    protected boolean makeOnline(String name, String IP, String port){
        
        if(!StringUtils.isNumeric(port)){

            return false;
        }  
        else{
            dp.setOnline(name, IP, port);
            return true;
        }      
    }

    protected String[] getIP(String name){
        String result = dp.getIP(name);
        if(result == null) return null;
        else{
            return result.split(":");
        } 
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

enum QueryType { SIGNIN, SIGNUP, SIGNOUT, CHOOSEROOM, GETIP, EXIT, UNKNOWN }
