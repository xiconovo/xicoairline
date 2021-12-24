import java.net.*;
import java.io.*;

public class Client {
    final Socket sock;
    final PrintWriter out;
    final BufferedReader in;
    boolean is_logged_in = false;

    public static void main(String[] args) {
        System.out.println("Hell World! I'm Client");
        int port = 4545;
        try {
            Client client = new Client("localhost", port);
            client.startClient(port);
        } catch (IOException e) {
            System.out.println(
                    "Exception caught when trying to connect on port " + port + " or listening for a connection");
        }
    }

    public Client(String host, int port) throws IOException {
        this.sock = new Socket(host, port);
        this.out = new PrintWriter(this.sock.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
    }

    void startClient(int port) throws IOException {
        Request req = new RequestLogin("joao", "123");
        sendRequest(req);
        String data = in.readLine();
        ResponseOk response = ResponseOk.deserialize(data);
        is_logged_in = response.status;
        System.out.println("Server said: " + response.message);
        sock.close();
    }

    void sendRequest(Request req) {
        System.out.println("Executing: " + req.serialize());
        this.out.println(req.serialize());
    }
}
