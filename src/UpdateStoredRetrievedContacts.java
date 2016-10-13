import edu.rit.cs.steven_landau.shiftmobile.ContactCard;
import edu.rit.cs.steven_landau.shiftmobile.RetrievedContacts;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by slandau on 10/13/2016.
 *
 * I will rename this file at some point.
 */
public class UpdateStoredRetrievedContacts {
    private static ObjectOutputStream oos = null;
    private static ObjectInputStream ois = null;
    private static RetrievedContacts entries;

    private static final String filename = "StoredRetrievedContacts.ser";


    public UpdateStoredRetrievedContacts() {
        try {
            ois = new ObjectInputStream(new FileInputStream(new File(filename)));
            fillEntries();
        } catch (IOException e) {
            e.printStackTrace(); // File is not set up correctly or does not exist. Must recreate it.
            try {
                oos = new ObjectOutputStream(new FileOutputStream(new File(filename)));
                oos.flush();
                oos.close();
                oos = null;
                ois = new ObjectInputStream(new FileInputStream(new File(filename)));
                fillEntries();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private synchronized void fillEntries() {
        if (ois == null) {
            try {
                ois = new ObjectInputStream(new FileInputStream(new File(filename)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Object o = ois.readObject();
            if (o instanceof RetrievedContacts) {
                entries = (RetrievedContacts) o; // This wll be sent to the client
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (EOFException eofe) {
            try {
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ois = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void updateRetrievedContactsFile() {
        if (ois != null) {
            try {
                ois.close();
                ois = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            oos = new ObjectOutputStream(new FileOutputStream(new File(filename)));
            oos.writeObject(entries);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                oos.close();
                oos = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public synchronized void updateEntries(RetrievedContacts rc) {
        entries = rc;  // The mobile sends us the RetrievedContacts with all the updated and non-updated information. No need to look through entries and compare
        new Thread(this::updateRetrievedContactsFile).start();
    }

    public RetrievedContacts getEntries() {
        return entries;
    }
}
