import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Iprefer {

    public static void main(String[] args) {
        // main method body
        State state = State.UNDEFINED;
        int server_port = -1;
        String hostname = null;
        int timeInSeconds = 0;
        Iparser myCLIparser = new Iparser(args);
        String[] result = myCLIparser.getResults();
        if (result.length == 1) {
            System.out.println(result[0]);
            return;
        }
        if (result[0].equals("SERVER")) {
            state = State.SERVER;
            server_port = Integer.parseInt(result[1]);

        } else {
            state = State.CLIENT;
            server_port = Integer.parseInt(result[1]);
            hostname = result[2];
            timeInSeconds = Integer.parseInt(result[3]);

        }

        /////////////////// SOCKET PROGRAMMING////////////////////////
        // based on whether this is a client prgram/ server program we gotta initalize
        /////////////////// things

        if (state == State.SERVER) {
            // we have the port number
            int totalBytesRead = 0;
            try (
                    ServerSocket serverSocket = new ServerSocket(server_port);
                    Socket clientSocket = serverSocket.accept();
                    InputStream in = clientSocket.getInputStream();
            // lets start a timer here
            ) {
                long startTime = System.currentTimeMillis();
                while (in.read() != -1) {
                    byte[] data = new byte[1000];
                    totalBytesRead += in.read(data);
                }
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                double rateMbps = (totalBytesRead * 8.0) / (duration / 1000.0) / 1000000.0; // Convert to Mbps
                int KBps = totalBytesRead / 1000;
                System.out.println("received=" + KBps + " rate=" + String.format("%.2f", rateMbps) + " Mbps");
            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port "
                        + server_port + " or listening for a connection");
                System.out.println(e.getMessage());
            }

        } else if (state == State.CLIENT) {
            int totalBytesSent = 0;
            try (
                    Socket client = new Socket(hostname, server_port);
                    OutputStream out = client.getOutputStream();

            ) {
                byte[] data = new byte[1000];
                long startTime = System.currentTimeMillis();
                long endTime = startTime + timeInSeconds * 1000;
                while (System.currentTimeMillis() < endTime) {
                    out.write(data);
                    totalBytesSent += 1000;
                }
                long duration = System.currentTimeMillis() - startTime;
                double rateMbps = (totalBytesSent * 8.0) / (duration / 1000.0) / 1000000.0; // Convert to Mbps
                int KBps = totalBytesSent / 1000;
                System.out.println("sent=" + KBps + " rate=" + String.format("%.2f", rateMbps) + " Mbps");

            } catch (IOException e) {
                System.out.println("Exception caught when trying to connect to "
                        + hostname + " on port " + server_port);
                System.out.println(e.getMessage());
            }
        }

    }

}
