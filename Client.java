import java.io.*;
import java.net.*;

public class Client {
    private static PrintWriter out = null;
    private static BufferedReader in = null;
    private static Thread readThread = null;

    public static void main(String[] args) throws IOException {

        Socket echoSocket = null;
        InetAddress localhost = InetAddress.getLocalHost();
        //String serverIp = "172.20.131.114";

        try {
            echoSocket = new Socket(localhost, 666); // ESTABELECER A LIGAÇÃO
            //echoSocket = new Socket(serverIp, 666);
            out = new PrintWriter(echoSocket.getOutputStream(), true); // OBTER 1 OBJETO TIPO PrintWriter

            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream())); // OBTER 1 OBJETO TIPO

        } catch (UnknownHostException e) {
            //System.err.println("Don't know about host " + serverIp);
            System.err.println("Don't know about host " + localhost);
            System.exit(1);
        } catch (IOException e) {
            //System.err.println("Couldn't get I/O for the connection to " + serverIp);
            System.err.println("Couldn't get I/O for the connection to " + localhost);
            System.exit(1);
        }

        readThread = new Thread(new Runnable() {
            public void run() {
                try {
                    String fromServer;
                    while ((fromServer = in.readLine()) != null) {
                        System.out.println(fromServer);
                    }
                } catch (IOException e) {
                    if (e instanceof SocketException) {
                        System.out.println("Conexão fechada.");
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        });
        readThread.start();

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String fromUser;

        while ((fromUser = stdIn.readLine()) != null) {
            out.println(fromUser);
            if (fromUser.equalsIgnoreCase("logout") || fromUser.equalsIgnoreCase("sair")){
                break;
            }
        }

        out.close();
        in.close();
        stdIn.close();
        echoSocket.close();

        if (readThread != null && readThread.isAlive()) {
            readThread.interrupt();
            try {
                readThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
