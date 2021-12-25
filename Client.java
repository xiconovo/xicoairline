import java.net.*;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    void startClient(int port) throws IOException, ParseException {
        boolean exit = false;
        int option;
        Request req;
        while (!is_logged_in) {
            option = showMenu1();
            String username = insertUsername();
            String password = insertPassword();
            if (option == 1) {
                req = new RequestLogin(username, password);
                sendRequest(req);
            } else if (option == 2) {
                req = new RequestRegister(username, password);
                sendRequest(req);
            } else if (option == 0) {
                stopClient();
                System.exit(0);
            } else {
                System.out.println("Opcao Inválida");
                stopClient();
                System.exit(1);
            }
            String data = in.readLine();
            ResponseOk response = ResponseOk.deserialize(data);
            System.out.println("Server said: " + response.message);
            is_logged_in = response.status;
        }
        while(!exit){
            option = showMainMenu();
            if(option == 1){
                sendBooking();
                //reserva
            }else if(option == 2){
                //lista de voos
            }else if(option == 3){
                //cancelar uma reserva
            }else if(option == 0){
                exit = true;
                //sair
            }else{
                System.out.println("Opcao Inválida");
                stopClient();
                System.exit(1);
            }
        }
        stopClient();
    }

    void sendRequest(Request req) {
        System.out.println("Executing: " + req.serialize());
        this.out.println(req.serialize());
    }

    int showMenu1() {
        System.out.println("Pressione:\n(1) Para se autenticar.\n(2) Para se registar\n(0) Para sair.");
        return Integer.parseInt(sc.nextLine());
    }

    String insertUsername() {
        System.out.println("Insira o seu ID.");
        return sc.nextLine();
    }

    String insertPassword() {
        System.out.println("Insira a sua password.");
        return sc.nextLine();
    }

    void stopClient() throws IOException {
        System.out.println("Cliente desconectou-se.");
        sock.close();
    }

    int showMainMenu() {
            System.out.println("Bem vindo à nossa Companhia! Pressione:\n(1) Para efetuar a reserva de uma viagem.\n(2) Obter a lista de todos os voos disponíveis.\n(3) Cancelar uma reserva.\n(0) Sair.");
            return Integer.parseInt(sc.nextLine());
    }

    void sendBooking() throws ParseException {
        String origin = insertOrigin();
        String destination = insertDestination();
        Date start = insertStart();
        Date end = insertEnd();
        Request req = new RequestBooking(origin,destination,start,end);
        sendRequest(req);
    }

    String insertOrigin(){
        System.out.println("Insira a origem do seu Voo.");
        return sc.nextLine();
    }

    String insertDestination(){
        System.out.println("Insira o Destino do seu Voo.");
        return sc.nextLine();
    }

    Date insertStart() throws ParseException {
        System.out.println("Insira a data inicial.");
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.parse(sc.nextLine());
    }

    Date insertEnd() throws ParseException {
        System.out.println("Insira a data final.");
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.parse(sc.nextLine());
    }





}
