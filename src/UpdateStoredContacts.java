import edu.rit.cs.steven_landau.shiftmobile.RetrievedContacts;
import edu.rit.cs.steven_landau.shiftmobile.SendCard;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.InflaterInputStream;

/**
 * Created by Steven Landau on 10/6/2016.
 *
 * Need to decide if I want to make everything in this class static. I'll leave it instanced for now.
 */
public class UpdateStoredContacts {
    public static ObjectInputStream ois = null;
    public static ObjectOutputStream oos = null;
    private static ArrayList<Contact> conversations = new ArrayList<>();


    public UpdateStoredContacts() {
        try {
            ois = new ObjectInputStream(new FileInputStream(new File("StoredContacts.ser")));
            fillConversation();
        } catch (IOException e) {
            e.printStackTrace(); // File is not set up correctly or does not exist. Must recreate it.
            try {
                oos = new ObjectOutputStream(new FileOutputStream(new File("StoredContacts.ser")));
                oos.flush();
                oos.close();
                oos = null;
                ois = new ObjectInputStream(new FileInputStream(new File("StoredContacts.ser")));
                fillConversation();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private synchronized void fillConversation() {
        if (ois == null) {
            try {
                ois = new ObjectInputStream(new FileInputStream(new File("StoredContacts.ser")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            while(true) {
                Object o = ois.readObject();
                if (o instanceof Contact) {
                    Contact c = (Contact) o;
                    conversations.add(c);
                }
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

    /**
     * This method assumes you have already added/removed whatever you
     * intended to change in conversations. This method does not have to be synchronized but
     * it is possible to have multiple threads running in this function (without the synchronized)
     * if the server is receiving new contacts at a ridiculous rate.
     */
    private synchronized void updateStored() {
        try {
            if (ois != null) {  // Don't want to read and write to files at the same time.
                ois.close();
                ois = null;
            }
            oos = new ObjectOutputStream(new FileOutputStream(new File("StoredContacts.ser")));
            oos.writeObject(conversations);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            oos = null;
        }
    }


    public synchronized void updateConversation(SendCard sc) {  // TODO: rewrite these methods to work with SendCard
        ArrayList<String> tempMsg = new ArrayList<>();
        tempMsg.add(sc.getMsg());
        Contact temp = new Contact(sc.getName(), sc.getNumber(), tempMsg);
        if (conversations.contains(temp)) {
            int index = conversations.indexOf(temp);
            conversations.get(index).addMessage(temp.getMostRecentMessage());
            Contact c = conversations.get(index);
            conversations.remove(index);
            conversations.add(0, c);  // Add the updated contact to the front of the ArrayList
        } else {
            conversations.add(temp);
        }

        new Thread(this::updateStored).start();
    }

    public synchronized void removeConversation(SendCard sc) {  // This should probably stick with a contact
        ArrayList<String> tempMsg = new ArrayList<>();
        Contact c = new Contact(sc.getName(), sc.getNumber(), tempMsg);
        if (conversations.contains(c)) {
            conversations.remove(c);
            new Thread(this::updateStored).start();
        }
    }

    public synchronized void deleteEverything() {
        try {
            oos = new ObjectOutputStream(new FileOutputStream(new File("StoredContacts.ser")));
            oos.flush();
            oos.close();
            oos = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Contact> getConversations() {
        return conversations;
    }
}
