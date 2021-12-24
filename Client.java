import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    final Socket sock;
    final PrintWriter out;
    final BufferedReader in;
    boolean is_logged_in = false;
    Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Hello World! I'm Client");
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
        menu1();
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

    void menu1(){
        String option;
        String username;
        String password;
        System.out.println("Pressione:\n(1) Para se autenticar.\n(2) Para se registar.");
        option = sc.nextLine();
        Request req;
        if(option.equalsIgnoreCase("1")){
            System.out.println("Insira o seu ID.");
            username = sc.nextLine();
            System.out.println("Insira a sua password.");
            password = sc.nextLine();
            req = new RequestLogin(username,password);
            sendRequest(req);
        }
        if(option.equalsIgnoreCase("2")){
            System.out.println("Insira o seu ID.");
            username = sc.nextLine();
            System.out.println("Insira a sua password.");
            password = sc.nextLine();
            req = new RequestRegister(username,password);
            sendRequest(req);
        }

    }


}
