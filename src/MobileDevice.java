import edu.rit.cs.steven_landau.shiftmobile.RetrievedContacts;
import edu.rit.cs.steven_landau.shiftmobile.SendCard;

import java.io.EOFException;
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
        System.out.println("creating new mobile device");
        ShiftServer.MobileThreads.add(this);
        sendToPC(new ConnectedToMobile(true));
        ShiftServer.isConnectedToMobile = true;
        listenToDevice();
    }

    private void listenToDevice() {
        try {
            while (true) {
                Thread.sleep(100);
                try {
                    Object received = in.readObject();
                    System.out.println("object received from mobile");
                    if (received instanceof SendCard) {
                        SendCard sc = (SendCard) received;
                        new Thread(() -> {
                            ShiftServer.usc.updateConversation(sc);
                        }).start();
                        sendToPC(sc);
                    }
                    // TODO: brainstorm a few more objects
                    else if (received instanceof RetrievedContacts) {
                        System.out.println("received contacts");
                        RetrievedContacts rc = (RetrievedContacts) received;
                        new Thread(() -> {
                            ShiftServer.usrc.updateEntries(rc);
                        }).start();
                        System.out.println(rc.cc);
                        /*try {
                            System.out.println(rc.cc);
                            System.out.println(rc.cc.size());
                        } catch (Exception e) {
                            // none
                        }*/
                        sendToPC(rc);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }


        } catch (Exception e) {
            // App probably shut down.
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                client.close(); // Will probably throw an exception because the client is already closed
                ShiftServer.isConnectedToMobile = false;
                sendToPC(new ConnectedToMobile(false));
                ShiftServer.MobileThreads.remove(this);
            } catch (IOException e) {
                e.printStackTrace(); // If this occurs, delete client.close. The client is already (forcibly) closed when here.
            }
        }
        System.out.println("closed mobile");
    }


    private void sendToPC(Object o) {
        for (PCDevice pcd : ShiftServer.PCThreads) {   // Will it really be this simple?
            pcd.sendToPCClient(o);
        }
    }

    public void sendToMobile(Object o) {
        try {
            out.writeObject(o);
            out.flush();
            System.out.println("message sent to mobile");
        } catch (IOException e) {
            e.printStackTrace();  // Mobile device not online?
        }
    }
}
