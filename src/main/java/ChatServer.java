import java.lang.*;
import java.net.*;
import java.io.*;

public class ChatServer {
    public static int PORT = 7789;
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket socket = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            serverSocket = new ServerSocket(PORT);

            // get IP of machine in LAN
            // https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
            final DatagramSocket s = new DatagramSocket();
            s.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String IP = s.getLocalAddress().getHostAddress();
            System.out.println("Server is running on " + IP + ":" + PORT);
        } catch (IOException ex) {
            // pass
        }

        // global database povider
        // Say no to Singleton
        // pass along the way
        DatabaseProvider dp = new MapDB();
        try {
            dp.connect();
        } catch (Exception ex) {
            System.out.println("Cannot connect to database");
            return;
        }

        while (true) {
            try {
                socket = serverSocket.accept();
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                Thread t = new ServerThread(socket, dis, dos, dp);
                t.start();
            } catch (Exception ex) {
                // pass
                break;
            }
        }

        try {
            socket.close();
        } catch (IOException ex) {
            // pass
        }
    }
}
