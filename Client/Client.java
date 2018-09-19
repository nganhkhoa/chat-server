import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;

// Client class
public class Client {
    private Socket s;
    private DataInputStream dis;
    private DataOutputStream dos;
    private InetAddress ip;
    private Scanner scn;

    public Client() {
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
        }
    }

    public void run() {
        try {
            // the following loop performs the exchange of
            // information between client and client handler
            boolean isSignedIn = false;
            boolean exit = false;
            String tosend = "";
            String received = "";

            while (!isSignedIn) {
                received = dis.readUTF();
                System.out.print(received);
                tosend = scn.nextLine();
                dos.writeUTF(tosend);

                switch (Integer.parseInt(tosend)) {
                    case 1:
                        isSignedIn = signin();
                        break;
                    case 2:
                        signup();
                        break;
                    default:
                        exit = true;
                        break;
                }
            }
            System.out.println("Signin Success");
            while (!exit) {
                received = dis.readUTF();
                System.out.print(received);
                tosend = scn.nextLine();
                dos.writeUTF(tosend);

                switch (Integer.parseInt(tosend)) {
                    case 1:
                        chooseroom();
                        break;
                    default:
                        exit = true;
                        break;
                }
            }

            // closing resources
            scn.close();
            dis.close();
            dos.close();
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.run();
    }

    protected boolean signin() {
        try {
            // username
            String received = dis.readUTF();
            System.out.print(received);
            String tosend = scn.nextLine();
            dos.writeUTF(tosend);

            // password
            received = dis.readUTF();
            System.out.print(received);
            tosend = scn.nextLine();
            dos.writeUTF(tosend);

            // result
            received = dis.readUTF();
            // System.out.print(received);
            if (received.indexOf("Successfully") != -1)
                return true;
            else
                return false;
        } catch (IOException ex) {
            // pass
        }
        return false;
    }

    protected void signup() {
        try {
            // username
            String received = dis.readUTF();
            System.out.print(received);
            String tosend = scn.nextLine();
            dos.writeUTF(tosend);

            // result
            received = dis.readUTF();
            System.out.print(received);
        } catch (IOException ex) {
            // pass
        }
    }

    protected void chooseroom() {}
}
