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
    private ObjectInputStream in;
    private ObjectOutputStream out;
    public static ArrayList<PCDevice> PCThreads = new ArrayList<>();
    public static ArrayList<MobileDevice> MobileThreads = new ArrayList<>();


    public ShiftServer() {
        try {
            server = new ServerSocket(PORT);
            while (true) {
                try {
                    Socket client = server.accept();
                    out = new ObjectOutputStream(client.getOutputStream());
                    out.flush();
                    in = new ObjectInputStream(client.getInputStream());
                    Object device = in.readObject();
                    if (device instanceof PC) {

                        new Thread(() -> {
                            PCThreads.add(new PCDevice(client, in, out));  // How will rewriting out and in affect this class once it is initiated?
                        }).start();

                    } else if (device instanceof Mobile) {
                        new Thread(() -> {
                            MobileThreads.add(new MobileDevice(client, in, out));
                        }).start();
                    }

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();  // Client configured incorrectly
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ShiftServer();
    }
}
