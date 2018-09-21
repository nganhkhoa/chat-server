import java.lang.*;
import java.net.*;
import java.io.*;

public class App {
    public static int PORT = 7789;
    public static void main(String[] args) {
        // add interupt event
        // Runtime.getRuntime().addShutdownHook(new Thread() {
        //     public void run() {
        //         System.out.println("Exited!");
        //     }
        // });

        ServerSocket serverSocket = null;
        Socket socket = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running on Port: " + PORT);
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
            // pass
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