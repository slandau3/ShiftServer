import edu.rit.cs.steven_landau.shiftmobile.Mobile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Steven Landau on 10/7/2016.
 */
public class ShiftServer {
    public static final int PORT = 8012;
    private ServerSocket server;
    public static ArrayList<PCDevice> PCThreads = new ArrayList<>();
    public static ArrayList<MobileDevice> MobileThreads = new ArrayList<>();
    public static UpdateStoredContacts usc = new UpdateStoredContacts();
    public static UpdateStoredRetrievedContacts usrc = new UpdateStoredRetrievedContacts();

    public ShiftServer() {
        try {
            server = new ServerSocket(PORT, 5);
            System.out.println(server.getInetAddress().getLocalHost());
            while (true) {
                try {
                    Socket client = server.accept();
                    System.out.println("connected to " + client.getInetAddress().getHostName());
                    ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                    out.flush();
                    ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                    Object device = in.readObject();
                    if (device instanceof PC) {
                        System.out.println("about to start a new pc device");
                        new Thread(() -> {
                            new PCDevice(client, in, out);
                        }).start();

                    } else if (device instanceof Mobile) {
                        if (MobileThreads.size() == 0) {
                            System.out.println("about to start a new Mobile device");
                            new Thread(() -> {
                                new MobileDevice(client, in, out);
                            }).start();
                        }
                    }
                    Thread.sleep(100);
                    System.out.println(PCThreads);
                    System.out.println(MobileThreads);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();  // Client configured incorrectly
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                usc.closeAll();
                usrc.closeAll();
                server.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new ShiftServer();
    }
}
