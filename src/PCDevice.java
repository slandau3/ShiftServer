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
                try {
                    Object received = in.readObject();  // Reading from PC
                    if (received instanceof SendCard) {
                        SendCard sc = (SendCard) received;
                        String message = sc.getMsg();
                        String number = sc.getNumber();
                        String name = sc.getName();
                        // TODO: Make a function for the mobile device that takes a message and phone number.
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
                return; // This class should end once this is returned.
            } catch (IOException e) {
                e.printStackTrace();
            }
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
