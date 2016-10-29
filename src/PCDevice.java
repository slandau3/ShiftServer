import edu.rit.cs.steven_landau.shiftmobile.RetrievedContacts;
import edu.rit.cs.steven_landau.shiftmobile.SendCard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.io.EOFException;

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
        sendToPCClient(new ConversationHolder(ShiftServer.usc.getConversations())); // Send an arrayList containing the current contact info
        sendToPCClient(ShiftServer.usrc.getEntries());
        System.out.println(ShiftServer.usrc.getEntries());
        sendToPCClient(new ConnectedToMobile(ShiftServer.isConnectedToMobile));
        System.out.println("Connected to a new pc!");
        listenToDevice(); // sending Conversations, entries, and isConnectedToMobile.
    }

    private void listenToDevice() {
        try {
            while (true) {
                Thread.sleep(100);
                try {
                    //if (in.available() > 0) {
                        Object received = in.readObject();  // Reading from PC
                    // TODO: Think of anything else that would be received.

                    //}
                    if (received instanceof SendCard) {
                        SendCard sc = (SendCard) received;
                        new Thread(() -> {
                            ShiftServer.usc.updateConversation(new SendCard("--Client--: " + sc.getMsg(), sc.getNumber(), sc.getName()));
                        }).start();
                        System.out.println("got a sendcard: " + sc.getMsg());
                        sendToMobile(sc);
                    } else if (received instanceof ClearRequest) {
                        System.out.println("deleting everything");
                        ShiftServer.usc.deleteEverything();
                    } else if (received instanceof RemoveRequest) {
                        RemoveRequest rr = (RemoveRequest) received;
                        ShiftServer.usc.removeConversation(rr.getContact());
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                //} catch (EOFException eofe) {
                    // do nothing
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Client probably shut down.
            //ShiftServer.PCThreads.remove(this); // Keep an eye on this
        } finally {
            try {
                in.close();
                System.out.println("in closeD");
                out.close();
                System.out.println("out closed");
                client.close(); // will probably throw an exception because the client is already closed.
                System.out.println("client closed");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ShiftServer.PCThreads.remove(this);
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
