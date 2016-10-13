import edu.rit.cs.steven_landau.shiftmobile.SendCard;

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
        ShiftServer.PCThreads.add(this);
        listenToDevice();
    }

    private void listenToDevice() {
        try {
            while (true) {
                Thread.sleep(10);
                try {
                    Object received = in.readObject();  // Reading from PC
                    if (received instanceof SendCard) {
                        System.out.println("received from pc");
                        SendCard sc = (SendCard) received;
                        sendToMobile(sc);
                    } // TODO: Think of anything else that would be received.
                    else if (received instanceof SendCard) {
                        SendCard sc = (SendCard) received;
                        ShiftServer.usc.updateConversation(sc);
                        // TODO: get the new conversations and send it to the client
                    }
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
            System.out.println("PC closed");
        }
    }

    private void sendToMobile(Object o) {
        for (MobileDevice md : ShiftServer.MobileThreads) {  // I highly doubt there will ever be more than one mobile device.
            md.sendToMobile(o);
            System.out.println("sent from pc to mobile");
        }
    }
    public void sendToPCClient(Object o) {
        try {
            out.writeObject(o);
            out.flush();
            System.out.println("sent to pc");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Message not sent.");
        }
    }

}
