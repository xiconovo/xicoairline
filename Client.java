import java.net.*;
import java.io.*;
import java.text.ParseException;
import java.util.*;


public class Client {
    final Socket sock;
    final DataOutputStream out;
    final DataInputStream in;
    boolean is_logged_in = false;
    boolean is_registed = false;
    int reservedCode;
    Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        int port = 4545;
        try {
            Client client = new Client("localhost", port);
            client.startClient(port);
        } catch (IOException | ParseException e) {
            System.out.println("Já não existe conexão. Fechar cliente.");
        }
    }

    public Client(String host, int port) throws IOException {
        this.sock = new Socket(host, port);
        this.out = new DataOutputStream(this.sock.getOutputStream());
        this.in = new DataInputStream(this.sock.getInputStream());
    }

    void startClient(int port) throws IOException, ParseException {
        List<Integer> reservas = new ArrayList<>();
        boolean exit = false;
        int option;
        Request req;
        String data;
        String username = "";
        String password = "";
        ResponseOk response;
        while (!is_logged_in) {
            option = showMenu1();
            if (option == 1) {
                username = insertUsername();
                password = insertPassword();
                req = new RequestLogin(username, password);
                sendRequest(req);
                data = in.readUTF();
                response = ResponseOk.deserialize(data);
                is_logged_in = response.status;
                System.out.println(response.message + "\n");
            } else if (option == 2) {
                username = insertUsername();
                password = insertPassword();
                req = new RequestRegister(username, password);
                sendRequest(req);
                data = in.readUTF();
                response = ResponseOk.deserialize(data);
                System.out.println(response.message + "\n");
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
        while (!exit) {
            if(username.equalsIgnoreCase("admin") && password.equalsIgnoreCase("admin123")){
                option = showAdminMenu();
                if(option == 1){
                    ResponseAddFlight responseAddFlight;
                    // inserir novos voos
                    sendFlights();
                    data = in.readUTF();
                    responseAddFlight = ResponseAddFlight.deserialize(data);
                    System.out.println(responseAddFlight.message + "\n");

                } else if (option == 2){
                    ResponseCloseDay responseCloseDay;
                    // encerrar um dia
                    sendDayClose();
                    data = in.readUTF();
                    responseCloseDay = ResponseCloseDay.deserialize(data);
                    System.out.println(responseCloseDay.message + "\n");


                } else if (option == 0){
                    exit = true;
                } else {
                    System.out.println("Opcão Inválida");
                    stopClient();
                    System.exit(1);
                }
            }
            else {
                option = showMainMenu();
                if (option == 1) {
                    ResponseBooking responseBooking;
                    sendBooking();
                    data = in.readUTF();
                    responseBooking = ResponseBooking.deserialize(data);
                    reservedCode = responseBooking.codeReserve;
                    if (reservedCode == -1) {
                        System.out.println(responseBooking.message+ "\n");
                    } else {
                        reservas.add(reservedCode);
                        System.out.println(responseBooking.message);
                        System.out.println("Codigo da sua reserva: " + responseBooking.codeReserve + "\n");
                    }
                } else if (option == 2) {
                    ResponseFlightList responseFlightList;
                    sendFlightListRequest();
                    data = in.readUTF();
                    responseFlightList = ResponseFlightList.deserialize(data);
                    if(!responseFlightList.status){
                        System.out.println(responseFlightList.allFlights);
                    }else{
                        showFlightList(responseFlightList.allFlights);
                    }

                } else if (option == 3) {
                    sendCodeListRequest();
                    data = in.readUTF();
                    ResponseBookedList bookedList = ResponseBookedList.deserialize(data);
                    if (!bookedList.status) {
                        System.out.println("Ainda não tem nenhuma reserva feita.\n");
                    } else {
                        showBookedList(bookedList.listOfCodes);
                        sendBookingCancel();
                        data = in.readUTF();
                        ResponseBookingCancel responseBookingCancel = ResponseBookingCancel.deserialize(data);
                        System.out.println(responseBookingCancel.message + "\n");
                    }
                } else if (option == 0) {
                    exit = true;
                } else {
                    System.out.println("Opcão Inválida");
                    stopClient();
                    System.exit(1);
                }
            }
        }
        stopClient();
    }

    void sendRequest(Request req) throws IOException {
        this.out.writeUTF(req.serialize());
    }

    void sendBooking() throws IOException{
        String route = insertRoute();
        String start = insertStart();
        String end = insertEnd();
        Request req = new RequestBooking(route, start, end);
        sendRequest(req);
    }

    void sendBookingCancel() throws IOException {
        int code = Integer.parseInt(insertReserveCode());
        Request req = new RequestBookingCancel(code);
        sendRequest(req);
    }

    void sendCodeListRequest() throws IOException{
        Request req = new RequestBookedList();
        sendRequest(req);
    }

    void sendFlights() throws IOException{
        String source = insertSource();
        String destination = insertDestination();
        int capacity = Integer.parseInt(insertCapacity());
        Request req = new RequestAddFlight(source, destination, capacity);
        sendRequest(req);

    }

    void sendFlightListRequest() throws IOException{
        Request req = new RequestFlightList();
        sendRequest(req);
    }

    void sendDayClose() throws IOException{
        String day = insertDayClose();
        Request req = new RequestDayClose(day);
        sendRequest(req);
    }

    int showMenu1() {
        System.out.println("Pressione:\n(1) Para se autenticar.\n(2) Para se registar\n(0) Para sair.");
        return Integer.parseInt(sc.nextLine());
    }

    int showMainMenu() {
        System.out.println("Bem vindo à nossa Companhia! Pressione:\n(1) Para efetuar a reserva de uma viagem.\n(2) Obter a lista de todos os voos disponíveis.\n(3) Cancelar uma reserva.\n(0) Sair.");
        return Integer.parseInt(sc.nextLine());
    }

    int showAdminMenu() {
        System.out.println("Bem vindo Administrador! Pressione:\n(1) Para inserir novos voos.\n(2) Para encerrar um dado dia.\n(0) Sair.");
        return Integer.parseInt(sc.nextLine());
    }

    void showBookedList(String bookedList){
        System.out.println("Selecione um dos codigos que pretende cancelar.");
        System.out.println("[" + bookedList + "]\n");
    }

    void showFlightList(String allFlights){
        System.out.println("A lista de voos disponiveis é dada por:\n[" + allFlights + "]\n");

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


    String insertReserveCode(){
        System.out.println("Insira o codigo da reserva que pretende cancelar.");
        return sc.nextLine();
    }


    String insertRoute() {
        System.out.println("Insira o trajeto que deseja realizar.\nExemplo: Se deseja partir em Portugal, passar em Inglaterra e chegar a Franca deve inserir:\n'Portugal-Inglaterra-Franca'.");
        return sc.nextLine();
    }

    String insertStart() {
        System.out.println("Insira a data inicial.(Exemplo: 21/05/2009)");
        return sc.nextLine();
    }

    String insertEnd() {
        System.out.println("Insira a data final.(Exemplo: 21/05/2009)");
        return sc.nextLine();
    }

    String insertDayClose() {
        System.out.println("Insira o dia que pretende encerrar.(Exemplo: 21/05/2009)");
        return sc.nextLine();
    }

    String insertSource(){
        System.out.println("Insira a origem do voo.(Exemplo: Portugal)");
        return sc.nextLine();
    }

    String insertDestination(){
        System.out.println("Insira o destino do voo.(Exemplo: Portugal)");
        return sc.nextLine();
    }

    String insertCapacity(){
        System.out.println("Insira a capacidade do aviao.");
        return sc.nextLine();
    }
}
