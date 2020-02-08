import java.io.*;
import java.net.*;

public class Proxy {
    public static void main(String[] args) throws IOException {

        ServerSocket proxy = new ServerSocket(9001);

        while (true) {
            try {
                Socket client = proxy.accept();
                if (client.isConnected()) {

                    Runnable r = new TunnelingHandler(client);
                    Thread t = new Thread(r);
                    t.start();
                }
            } catch (IOException se) {
                System.out.println("Nie nawiazano polaczenia....");
            }
        }
    }
}
