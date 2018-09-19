import java.io.*;
import java.net.*;
import java.util.*;

public class ServerThread extends Thread {
    final Socket socket;
    final DataInputStream dis;
    final DataOutputStream dos;

    final private DatabaseProvider dp;

    public ServerThread(Socket socket, DataInputStream dis, DataOutputStream dos) {
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
        dp = new MapDB();

        try {
            dp.connect();
        } catch (Exception ex) {
            System.out.println("Cannot connect to database");
        }
    }

    @Override
    public void run() {
        String received;
        String toreturn;

        // 1. verifies username and password
        // 2. disconnect on failure
        // 3. wait for room selection
        // 4. connect client to room
        // 5. disconnect from server

        menu();
        exit();
    }

    protected void menu() {
        Formater formater = (boolean isSignedIn) -> {
            // List<String> menu_items = new ArrayList<String>();
            Map<Integer, String> menu_items = new TreeMap<Integer, String>();
            int menutype;
            if (isSignedIn) {
                menutype = 2;
            } else {
                menutype = 1;
            }

            String format = "Choose an option\n";

            for (MenuEnum me : MenuEnum.values()) {
                if (me.type() != menutype)
                    continue;
                menu_items.put(me.value(), me.string());
            }

            Set set = menu_items.entrySet();
            Iterator iter = set.iterator();
            int counter = 1;
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                format += entry.getKey() + ". " + entry.getValue() + "\n";
                counter++;
            }

            format += counter + ". exit\n";

            format += "> ";

            return format;
        };

        boolean isSignedIn = false;
        boolean exit = false;
        String format = formater.format(isSignedIn);
        while (!exit) {
            try {
                dos.writeUTF(format);
                int option = Integer.parseInt(dis.readUTF());

                if (isSignedIn) {
                    switch (MenuEnum.get(2, option)) {
                        case CHOOSEROOM:
                            chooseroom();
                            break;
                        default:
                            exit = true;
                            break;
                    }
                } else {
                    switch (MenuEnum.get(1, option)) {
                        case SIGNIN:
                            isSignedIn = signin();
                            format = formater.format(isSignedIn);
                            break;
                        case SIGNUP:
                            signup();
                            break;
                        default:
                            exit = true;
                            break;
                    }
                }
            } catch (IOException ex) {
                // client or host dis-connected?
                break;
            } catch (NumberFormatException ex) {
                // re-enter please
                continue;
            }
        }
    }
    protected boolean signin() {
        try {
            dos.writeUTF("Username: ");
            String username = dis.readUTF();
            dos.writeUTF("Password: ");
            String password = dis.readUTF();

            if (!dp.signIn(username, password)) {
                dos.writeUTF("Username or Password incorrect\n");
                return false;
            } else {
                dos.writeUTF("Successfully signed in as " + username + "\n");
                return true;
            }

        } catch (IOException ex) {
            // pass
        }
        return false;
    }
    protected void signup() {
        try {
            dos.writeUTF("Username: ");
            String username = dis.readUTF();

            Account newAccount = new Account(username);
            String password = newAccount.getPassword();

            if (dp.existUser(username)) {
                dos.writeUTF("Username is already taken\n");
                return;
            }

            dp.addUser(newAccount);
            dos.writeUTF(
                "Successfully signed up as " + username + " with password: \"" + password + "\"\n");
        } catch (IOException ex) {
            // pass
        } catch (Exception ex) {
            // dos.writeUTF("An error occured while register a new user\n");
        }
    }
    protected void chooseroom() {}
    protected void exit() {
        try {
            socket.close();
            dis.close();
            dos.close();
        } catch (IOException ex) {
            // pass
        }
        dp.close();
    }
}

@FunctionalInterface
interface Formater {
    String format(boolean isSignedIn);
}

enum MenuEnum {
    // normal menu -- 1
    SIGNIN(1, 1),
    SIGNUP(1, 2),

    // menu for logged in user -- 2
    CHOOSEROOM(2, 1),

    ERROR(99, 99);

    private final int menutype;
    private final int menuitem;

    private static final String[][] menuitems = {{"Sign in", "Sign up"}, {"Choose room"}};

    MenuEnum(int menutype, int menuitem) {
        this.menutype = menutype;
        this.menuitem = menuitem;
    }

    public static final int limit(int menutype) {
        return menuitems[menutype - 1].length;
    }

    public int type() {
        return menutype;
    }

    public final int value() {
        return menuitem;
    }

    public final String string() {
        return menuitems[menutype - 1][menuitem - 1];
    }

    public static MenuEnum get(int menutype, int menuitem) {
        for (final MenuEnum me : EnumSet.allOf(MenuEnum.class))
            if (me.menutype == menutype && me.menuitem == menuitem)
                return me;
        return ERROR;
    }
}
