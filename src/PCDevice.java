import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Steven Landau on 10/7/2016.
 */
public class PCDevice{

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket client;

    public PCDevice(Socket client, ObjectInputStream in, ObjectOutputStream out) {
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
                    Object received = in.readObject();  // Reading from PC
                    if (received instanceof SendCard) {
                        SendCard sc = (SendCard) received;
                        sendToMobile(sc);
                    } // TODO: Think of anything else that would be received.
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            // Client probably shut down.
            ShiftServer.PCThreads.remove(this); // Keep an eye on this
        } finally {
            try {
                in.close();
                out.close();
                client.close(); // will probably throw an exception because the client is already closed.
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendToMobile(SendCard sc) {
        for (MobileDevice md : ShiftServer.MobileThreads) {  // I highly doubt there will ever be more than one mobile device.
            md.sendToMobile(sc);
        }
    }
    public void sendToPCClient(SendCard sc) {
        try {
            out.writeObject(sc);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Message not sent.");
        }
    }

}
