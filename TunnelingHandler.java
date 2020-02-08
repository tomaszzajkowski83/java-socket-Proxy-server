import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;

public class TunnelingHandler implements Runnable {
    private Socket incoming;
    private URL url;
    private int port;
    private String filePath = "D:\\TMP\\";

    public TunnelingHandler(Socket soc) {
        incoming = soc;
    }

    @Override
    public void run() {
        System.out.println("Nowy watek " + Thread.currentThread().getName());

        try (OutputStream os = incoming.getOutputStream();
             InputStream is = incoming.getInputStream()
        ) {
            byte[] zPrzeg = new byte[1024];
            is.read(zPrzeg);
            String request = new String(zPrzeg);
            System.out.println(request.split("\n")[0]);
            String[] hed = request.split("\n");
            String[] zapyt = hed[0].split(" ");
            if (zapyt.length < 2)
                return;
            String fileCach = zapyt[1].substring(7, zapyt[1].length() - 1);
            fileCach = fileCach.replaceAll("/{1,}|\\.|\\?|>|<|\\|\\*|:|\"", "#");
            String path = String.join("", filePath, fileCach, ".txt");
            if (zapyt[0].equals("GET")) {
                url = new URL(zapyt[1]);
                port = 80;

            }

            File file = new File(filePath + fileCach + ".txt");
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(filePath + fileCach + ".txt");
                byte[] fromHd = new byte[8192];
                int ile;
                while ((ile = fis.read(fromHd)) != -1) {
                    os.write(fromHd, 0, ile);
                    os.flush();
                }
                fis.close();
            } else {
                System.out.println(path);
                FileOutputStream fos = new FileOutputStream(path);
                String nowy = request.replaceFirst("keep-alive", "close");
                request = "HEAD " + url.getPath() + " HTTP/1.1\r\nHost: " + url.getHost() + "\r\nConnection: close\r\n";
                //nowy = request.replaceFirst("Accept-Encoding.*\r\n","");
                byte[] requestArr = nowy.getBytes();

                InetAddress adr = InetAddress.getByName(url.getHost());
                System.out.println("Adres -> " + adr.getHostAddress());

                Socket toServer = new Socket(adr.getHostAddress(), port);
                BufferedOutputStream bos = new BufferedOutputStream(toServer.getOutputStream());
                BufferedInputStream bis = new BufferedInputStream(toServer.getInputStream());

                bos.write(requestArr);
                bos.flush();

                byte[] bufor = new byte[4096];
                int count;
                StringBuilder sb = new StringBuilder();
                while ((count = bis.read(bufor)) != -1) {
                    os.write(bufor, 0, count);
                    fos.write(bufor, 0, count);
                    fos.flush();
                    os.flush();
                }
                fos.close();
                //----------------------------------------
/*          LOGIKA DO FILTROWANIA

            String temp = sb.toString();
            //temp.replaceAll("[Bb]omba]", "<span style = \"bacground-color:yellow;\">bomba</span>");
            String[] przesyl = temp.split("\n");
            for(String s : przesyl) {
                byte[] send = s.getBytes();
                os.write(send);
                os.write((byte)'\n');
                os.flush();
            }

 */


                toServer.close();
                System.out.println("Watek " + Thread.currentThread().getName() + " zamkniety");
                Thread.currentThread().interrupt();
            }
        } catch (
                SocketException se) {
            System.out.println("Watek " + Thread.currentThread().getName() + " zamkniety");
            Thread.currentThread().interrupt();
        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }
}

