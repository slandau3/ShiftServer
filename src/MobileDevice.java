import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Steven Landau on 10/7/2016.
 */
public class MobileDevice {

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket client;

    public MobileDevice(Socket client, ObjectInputStream in, ObjectOutputStream out) {
        this.client = client;
        this.in = in;
        this.out = out;
        listenToDevice();
    }

    private void listenToDevice() {
        try {
            while (true) {
                Thread.sleep(10);
                try {
                    Object received = in.readObject();
                    if (received instanceof SendCard) {
                        SendCard sc = (SendCard) received;
                        sendToPC(sc);
                    }
                    // TODO: brainstorm a few more objects
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }


        } catch (Exception e) {
            // App probably shut down.
            ShiftServer.MobileThreads.remove(this); // Keep an eye on this.
        } finally {
            try {
                in.close();
                out.close();
                client.close(); // Will probably throw an exception because the client is already closed
            } catch (IOException e) {
                e.printStackTrace(); // If this occurs, delete client.close. The client is already (forcibly) closed when here.
            }
        }
    }


    private void sendToPC(SendCard sc) {
        for (PCDevice pcd : ShiftServer.PCThreads) {   // Will it really be this simple?
            pcd.sendToPCClient(sc);
        }
    }

    public void sendToMobile(SendCard sc) {
        try {
            out.writeObject(sc);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();  // Mobile device not online?
        }
    }
}
