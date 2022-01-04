import java.net.*;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Client {
    final Socket sock;
    final PrintWriter out;
    final BufferedReader in;
    boolean is_logged_in = false;
    boolean is_registed = false;
    int reservedCode;
    Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Hello World! I'm Client");
        int port = 4545;
        try {
            Client client = new Client("localhost", port);
            client.startClient(port);
        } catch (IOException | ParseException e) {
            System.out.println("Exception caught when trying to connect on port " + port + " or listening for a connection");
        }
    }

    public Client(String host, int port) throws IOException {
        this.sock = new Socket(host, port);
        this.out = new PrintWriter(this.sock.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
    }

    void startClient(int port) throws IOException, ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(x);
        cal.add(Calendar.MONTH,1);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int year = cal.get(Calendar.YEAR);
        String newdate = day + "-" + month + "-" + year;


        List<Integer> reservas = new ArrayList<>();
        boolean exit = false;
        int option;
        Request req;
        String data;
        ResponseOk response;
        ResponseBooking responseBooking;
        while (!is_logged_in) {
            option = showMenu1();
            String username = insertUsername();
            String password = insertPassword();
            if (option == 1) {
                req = new RequestLogin(username, password);
                sendRequest(req);
                data = in.readLine();
                response = ResponseOk.deserialize(data);
                is_logged_in = response.status;
            } else if (option == 2) {
                req = new RequestRegister(username, password);
                sendRequest(req);
                data = in.readLine();
                response = ResponseOk.deserialize(data);
                is_registed = response.status;
            } else if (option == 0) {
                stopClient();
                System.exit(0);
            } else {
                System.out.println("Opcao Inválida");
                stopClient();
                System.exit(1);
            }
        }
        while(!exit){
            option = showMainMenu();
            if(option == 1){
                sendBooking();
                data = in.readLine();
                responseBooking = ResponseBooking.deserialize(data);
                reservedCode = responseBooking.codeReserve;
                if(reservedCode == -1){
                    System.out.println(responseBooking.message);
                }else{
                    reservas.add(reservedCode);
                    System.out.println(responseBooking.message);
                    System.out.println("Codigo da sua reserva: " + responseBooking.codeReserve);
                }
                //reserva
            }else if(option == 2){
                //lista de voos

            }else if(option == 3){
                sendBookingCancel();
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
        String route = insertRoute();
        Date start = insertStart();
        Date end = insertEnd();
        Request req = new RequestBooking(route,start,end);
        sendRequest(req);
    }

    void sendBookingCancel(){

    }


    String insertRoute(){
        System.out.println("Insira o trajeto que deseja realizar.\nExemplo: Se deseja partir em Portugal, passar em Inglaterra e chegar a Franca deve inserir:\n'Portugal-Inglaterra-Franca'.");
        return sc.nextLine();
    }
    Date insertStart() throws ParseException {
        System.out.println("Insira a data inicial.(Exemplo: 21-05-2009)");
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.parse(sc.nextLine());
    }

    Date insertEnd() throws ParseException {
        System.out.println("Insira a data final.(Exemplo: 21-05-2009)");
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.parse(sc.nextLine());
    }





}
