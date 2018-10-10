import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.lang.StringUtils;
// json provider
// import org.json.*;
import com.google.gson.*;
import com.google.gson.reflect.*;
// regex util
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private Pattern pattern;
    private Matcher matcher;
    private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    Gson gson = new Gson();

    public ServerThread(
        Socket socket, DataInputStream dis, DataOutputStream dos, DatabaseProvider dp) {
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
        this.dp = dp;

        pattern = Pattern.compile(IPADDRESS_PATTERN);
    }

    @Override
    public void run() {
        // 1. verifies username and password
        // 2. disconnect on failure
        // 3. wait for room selection
        // 4. connect client to room
        // 5. disconnect from server

        Response result = null;
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

            Request req = gson.fromJson(json_msg, Request.class);

            String task = req.getTask();
            param = req.getParam();

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

            System.out.println("[" + Thread.currentThread().getId() + "]:+:" + json_msg);

        } catch (IOException ex) {
            // pass
        } catch (IllegalArgumentException ex) {
            System.out.println(
                ":[" + Thread.currentThread().getId() + "]:-:" + json_msg + "\n\tNot valid base64");
            qt = QueryType.UNKNOWN;
        } catch (JsonParseException ex) {
            System.out.println(
                ":[" + Thread.currentThread().getId() + "]:-:" + json_msg + "\n\tNot valid json");
            qt = QueryType.UNKNOWN;
        }
    }

    protected Response get_result() {
        String username = "";
        String password = "";
        String ip = "";
        String port = "";
        String[] IPPort = null;
        switch (qt) {
            case SIGNIN:
                /*
                /* {
                /*      "task": "signin",
                /*      "param": [
                /*          "username",     // string
                /*          "password",     // string
                /*          "ip",           // string must be <int>.<int>.<int>.<int>
                /*          "port"          // int
                /*      ]
                /* }
                /*
                /* return code:
                /*      100 -- success
                /*      101 -- method param not found
                /*      102 -- wrong credentials
                /*      103 -- ip or port is not formated right
                /*
                /*  returns true
                 */
                if (param.size() != 4) {
                    return new Response(101, "Method param not found");
                }

                username = param.get(0);
                password = param.get(1);
                isLoggedIn = signin(username, password);
                if (!isLoggedIn) {
                    return new Response(102, "Wrong credentials");
                }

                ip = param.get(2);
                port = param.get(3);
                if (!makeOnline(username, ip, port)) {
                    return new Response(103, "IP Port error");
                }

                this.Username = username;
                this.IP = ip;
                return new Response(100, "Logged in successfully");

            case SIGNUP:
                /*
                /*  {
                /*      "task": "signup",
                /*      "param": [
                /*          "username"      // string
                /*      ]
                /*  }
                /*
                /* return code:
                /*      200 -- success
                /*      201 -- method param not found
                /*      202 -- username existed
                /*      203 -- cannot create new user -- other errors
                /*
                /* returns 'password'
                 */
                if (param.size() != 1)
                    return new Response(201, "method param not found");

                username = param.get(0);
                password = signup(username);
                if (password == null)
                    return new Response(202, "username existed");

                return new Response(200, "Signup successfully", Arrays.asList(password));

            case GETIP:
                /*
                /* {
                /*      "task": "getip",
                /*      "param": [
                /*          "username",
                /*      ]
                /* }
                /*
                /* return code:
                /*      300 -- success
                /*      301 -- method param not found
                /*      302 -- user not online
                /*      303 -- can not query yourself
                /*
                /* returns ['ip','port']
                 */
                username = param.get(0);
                if (username.equals(Username))
                    return new Response(303, "Can not query yourself");

                IPPort = getIP(username);
                if (IPPort == null)
                    return new Response(302, "User not online");

                ip = IPPort[0];
                port = IPPort[1];
                return new Response(200, "Query user successfully", Arrays.asList(ip, port));

            case EXIT:
                /*
                /* {
                /*      "task": "exit",
                /*      "param": [
                /*
                /*      ]
                /* }
                /*
                /* return code:
                /*      0 -- exit
                /*
                /* returns void
                 */
                if (this.Username != null)
                    dp.setOffline(this.Username);
                return new Response(0, "EXIT");

            default:
                break;
        }

        return null;
    }

    protected void return_result(Response result) {
        try {
            String tosend = gson.toJson(result);

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
        } else {
            Account new_account = new Account(username);
            dp.addUser(new_account);
            return new_account.getPassword();
        }
    }

    protected boolean makeOnline(String name, String IP, String port) {
        // check for valid ip
        pattern = Pattern.compile(IPADDRESS_PATTERN);
        matcher = pattern.matcher(IP);

        if (!matcher.matches()) {
            return false;
        }

        // check port is number
        if (!StringUtils.isNumeric(port)) {
            return false;
        }
        dp.setOnline(name, IP, port);
        return true;
    }

    protected String[] getIP(String name) {
        String result = dp.getIP(name);
        if (result == null)
            return null;
        else {
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
