import java.io.*;
import java.net.*;

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
        while (true) {
            try {
                dos.writeUTF("Username: ");
                String username = dis.readUTF();
                System.out.print(username);
                dos.writeUTF("Password: ");
                String password = dis.readUTF();

                if (!dp.signIn(username, password)) {
                    dos.writeUTF("Good bye");

                    socket.close();
                    dis.close();
                    dos.close();
                    dp.close();
                    return;
                }

                dos.writeUTF("Successfully signed in as " + username);
                break;

            } catch (IOException ex) {
                // pass
            }
        }

        while (true) {
            try {
                dos.writeUTF("> ");
                received = dis.readUTF();

                if (received.equals("quit")) {
                    socket.close();
                    dis.close();
                    dos.close();
                    break;
                }

                dos.writeUTF(received);

            } catch (IOException ex) {
                // pass
            }
        }

        dp.close();

        // 2. disconnect on failure
        // 3. wait for room selection
        // 4. connect client to room
        // 5. disconnect from server
    }
}
