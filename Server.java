import java.net.*;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

class Server implements Serializable{
    static int PORT = 4545;
    static boolean running = true;
    UserManager user_manager = new UserManager();
    BookingManager booking_manager = new BookingManager();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File fich = new File("estadoApp");
        Server server;
        if (!fich.exists()) {
            server = new Server();
            server.startServer(PORT);
        }
        else {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream("estadoApp"));
            server = (Server) is.readObject();
            server.startServer(PORT);
        }
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("estadoApp"));
        os.writeObject(server);
    }

    void startServer(int port) throws IOException {
        List<ClientHandler> threads = new ArrayList<>();
        try {
            ServerSocket server_socket = new ServerSocket(port);
            System.out.println("Server started, waiting for client connections");
            Thread s = new ServerHandler(server_socket);
            s.start();
            while (running) {
                Socket client_socket = server_socket.accept();
                ClientHandler t = new ClientHandler(client_socket);
                threads.add(t);
                t.start();

            }
        } catch (IOException e) {
            System.out.println("Server closing.");
            for(ClientHandler t : threads){
                t.getSocket().close();
            }
        }

    }

    private static class ServerHandler extends Thread {
        Scanner sc;
        ServerSocket server_socket;

        public ServerHandler(ServerSocket server_socket){
            this.server_socket = server_socket;
            sc = new Scanner(System.in);
        }
        public void run(){
            while(running){
                if(sc.nextLine().equalsIgnoreCase("sair")){
                    running = false;
                    try {
                        server_socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    System.out.println("RUNNING: " + running);
                }
            }
        }

    }
    private class ClientHandler extends Thread {
        final Socket sock;
        final DataOutputStream out;
        final DataInputStream in;
        boolean client_connected = true;
        User user;

        public ClientHandler(Socket s) throws IOException {
            this.sock = s;
            this.out = new DataOutputStream(this.sock.getOutputStream());
            this.in = new DataInputStream(this.sock.getInputStream());
        }

        public Socket getSocket() {
            return sock;
        }


        public void run() {
            System.out.println("New Client");

            while (client_connected && running) {
                try {
                    String in_data = in.readUTF();
                    System.out.println("Received: '" + in_data + "'");
                    executeRequest(in_data);
                } catch (EOFException e) {
                    client_connected = false;
                } catch (Exception e) {
                    System.out.println("Error " + e);
                }
            }

            try {
                System.out.println("Goodbye client, closing socket");
                this.sock.close();
            } catch (Exception e) {
                System.out.println("Failed to close socket");
            }
        }

        void executeRequest(String request_data) throws ParseException, IOException {
            String[] split_data = request_data.split(";", 2);
            int request_number = Integer.parseInt(split_data[0]);
            Response response;

            switch (request_number) {
                case RequestLogin.REQUEST_NUMBER: {
                    RequestLogin req = RequestLogin.deserialize(split_data[1]);
                    if(req.username.equalsIgnoreCase("admin") && req.password.equalsIgnoreCase("admin123")){
                        response = new ResponseOk(true, "Login success");
                    }
                    else {
                        boolean ok = user_manager.verifyUser(req.username, req.password);
                        if (ok) {
                            user = user_manager.getUserByName(req.username);
                            response = new ResponseOk(true, "Login success");
                        } else {
                            response = new ResponseOk(false, "Login failed");
                        }
                    }
                    sendResponse(response);
                }
                break;

                case RequestRegister.REQUEST_NUMBER: {
                    RequestRegister req = RequestRegister.deserialize(split_data[1]);
                    boolean ok = user_manager.addUser(req.username, req.password);
                    if (ok) {
                        //user_manager.saveUsers();
                        response = new ResponseOk(true, "Registed successful.");
                    } else {
                        response = new ResponseOk(false, "Regist failed.");
                    }
                    sendResponse(response);
                }
                break;

                case RequestBooking.REQUEST_NUMBER: {
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    RequestBooking req = RequestBooking.deserialize(split_data[1]);
                    Reserva reserve = booking_manager.reservedOnDate(req.route, req.start, req.end, user);
                    if (reserve == null) {
                        response = new ResponseBooking(false, "Unsuccessful reserved", -1);
                    } else {
                        String dateString = dateFormat.format(reserve.day);
                        System.out.println("Reserved Date: " + dateString);
                        response = new ResponseBooking(true, "Booked successful on day " + dateString, reserve.codeReserve);
                    }
                    sendResponse(response);
                }
                break;

                case RequestBookedList.REQUEST_NUMBER: {
                    String listOfCodes = booking_manager.userReservedCodes(user);
                    if(listOfCodes.equalsIgnoreCase(" ")){
                        response = new ResponseBookedList(false,listOfCodes);
                    }else{
                        response = new ResponseBookedList(true,listOfCodes);
                    }
                    sendResponse(response);


                }
                break;

                case RequestBookingCancel.REQUEST_NUMBER: {
                    RequestBookingCancel req = RequestBookingCancel.deserialize(split_data[1]);
                    boolean canceled = booking_manager.cancelReserve(req.codeReserve,user);
                    if(canceled){
                        response = new ResponseBookingCancel(true, "Booked successfuly canceled.");
                    } else{
                        response = new ResponseBookingCancel(false, "Booked unsuccessfuly canceled.");
                    }
                    sendResponse(response);
                }
                break;

                case RequestAddFlight.REQUEST_NUMBER: {
                    RequestAddFlight req = RequestAddFlight.deserialize(split_data[1]);
                    boolean addFlight = booking_manager.addFlight(req.source,req.destination,req.capacity);
                    if(addFlight){
                        response = new ResponseAddFlight(true, "Flight added.");
                    } else {
                        response = new ResponseAddFlight(false, "Flight not available to add.");
                    }
                    sendResponse(response);
                }
                break;

                case RequestDayClose.REQUEST_NUMBER: {
                    RequestDayClose req = RequestDayClose.deserialize(split_data[1]);
                    Date day = new SimpleDateFormat("dd/MM/yyyy").parse(req.day);
                    boolean closeDay = booking_manager.closeDay(day);
                    if(closeDay){
                        response = new ResponseCloseDay(true, "Successful. Day closed.");
                    } else{
                        response = new ResponseCloseDay(false, "Unsuccessful. Day is already close.");
                    }
                    sendResponse(response);
                }
                break;

                case RequestFlightList.REQUEST_NUMBER: {
                    String flights = booking_manager.getAllFlights();
                    if(flights.equalsIgnoreCase("")){
                        response = new ResponseFlightList(false, "Ainda não existem voos disponíveis.");

                    }else{
                        response = new ResponseFlightList(true,flights);
                    }
                    sendResponse(response);
                }
                break;

            }
        }

        void sendResponse(Response resp) throws IOException {
            this.out.writeUTF(resp.serialize());
        }
    }

    class User implements Serializable{
        int id;
        String name;
        String hash;


        public User(int id, String name, String hash) {
            this.id = id;
            this.name = name;
            this.hash = hash;
        }
    }


    private class UserManager implements Serializable{
        List<User> users = new ArrayList<>();

        public boolean addUser(String name, String hash) {
            User user_found = getUserByName(name);
            if (user_found != null) {
                return false;
            }

            users.add(new User(users.size() + 1, name, hash));
            return true;
        }

        public boolean verifyUser(String name, String hash) {
            User user_found = getUserByName(name);
            if (user_found != null) {
                return user_found.hash.equals(hash);
            }
            return false;
        }

        private User getUserByName(String name) {
            for (User user : users) {
                if (user.name.equals(name)) {
                    return user;
                }
            }
            return null;
        }
    }


    private class BookingManager implements Serializable{
        public List<Flight> flights = new ArrayList<>(); // lista de voos geral
        public Map<Date, List<Flight>> flightsPerDay = new HashMap<>(); // Lista de voos para cada dia geral
        Map<Integer, Reserva> reservedBookings = new HashMap<>();
        List<Date> daysClosed = new ArrayList<>();



        public boolean closeDay(Date day) throws IOException {
            for(Date d : daysClosed){
                if(d.equals(day)){
                    return false;
                }
            }
            cancelDayReserves(day);
            cancelAllReservesOnDay(day);
            daysClosed.add(day);
            return true;
        }

        public String getAllFlights(){
            String ret = "";
            int size = flights.size();
            for(int i = 0; i < size; i++){
                if(i == size-1){
                    ret += flights.get(i).getSource() + "-" + flights.get(i).getDestination();
                } else{
                    ret += flights.get(i).getSource() + "-" + flights.get(i).getDestination() + ",";
                }
            }
            return ret;
        }
        public void cancelAllReservesOnDay(Date day){
            Iterator<Map.Entry<Integer,Reserva>> itr = reservedBookings.entrySet().iterator();
            while(itr.hasNext()){
                Map.Entry<Integer,Reserva> entry = itr.next();
                if(entry.getValue().day.equals(day)){
                    itr.remove();
                }
            }
        }
        public void cancelDayReserves(Date day) throws IOException {
            flightsPerDay.remove(day);
        }


        public boolean addFlight(String source, String destination, int capacity) throws IOException {
            Flight f = new Flight(source,destination,capacity);
            String origin;
            String destin;
            for(Flight flight : flights){
                origin = flight.getSource();
                destin = flight.getDestination();
                if(f.getSource().equalsIgnoreCase(origin) && f.getDestination().equalsIgnoreCase(destin)){
                    return false;
                }
            }
            this.flights.add(f);
            return true;
        }


        public Reserva reservedOnDate(String route, String started, String ended, User user) throws ParseException, IOException {
            SimpleDateFormat DateFor = new SimpleDateFormat("dd/MM/yyyy");
            Date start = DateFor.parse(started);
            Date end = DateFor.parse(ended);
            Calendar c = Calendar.getInstance();
            String[] countrys = route.split("-");
            int size = countrys.length - 1;
            Flight flightToAdd;
            while (start.before(end) || start.equals(end)) {
                boolean dayClosed = false;
                for(Date d : daysClosed){
                    if(d.equals(start)){
                        dayClosed = true;
                        break;
                    }
                }
                if(!dayClosed){
                    List<Flight> toReserve = new ArrayList<>();
                    int thereIsFlight = 0;
                    for (int i = 0; i < size; i++) {
                        flightToAdd = isThereFlight(countrys[i], countrys[i + 1], start);
                        if (flightToAdd == null) {
                            thereIsFlight = -1;
                            break;
                        }
                        toReserve.add(flightToAdd);
                    }
                    if (thereIsFlight == 0) {
                        for (Flight f : toReserve) { // WARNING: LOCKS
                            f.setCapacity(f.getCapacity() - 1);
                        }
                        Reserva reserva = new Reserva(this.reservedBookings.size() + 1, start, user, toReserve);
                        reservedBookings.put(reserva.codeReserve, reserva);
                        return reserva;
                    }
                }
                //em baixo é somar 1 ao dia
                c.setTime(start);
                c.add(Calendar.DATE, 1);
                start = c.getTime();
            }
            return null;
        }

        public Flight isThereFlight(String source, String destination, Date day) throws IOException {
            Flight ret = null;
            List<Flight> day_flights = flightsPerDay.get(day);
            if (day_flights != null) {
                for (Flight f : day_flights) {
                    if (f.getSource().equalsIgnoreCase(source) && f.getDestination().equalsIgnoreCase(destination)) {
                        int capacity = f.getCapacity(); // WARNING: LOCK
                        if (capacity > 0) {
                            return f;
                        } else {
                            System.out.println("Full flight.");
                            return null;
                        }
                    }
                }
            }

            for (Flight f : this.flights) {
                if (f.getSource().equalsIgnoreCase(source) && f.getDestination().equalsIgnoreCase(destination)) {
                    if (day_flights != null) {
                        day_flights.add(f);
                    } else {
                        List<Flight> new_flight_list = new ArrayList<>();
                        new_flight_list.add(f);
                        flightsPerDay.put(day, new_flight_list);
                    }
                    ret = f;
                }
            }
            return ret;
        }

        public boolean cancelReserve(int code, User user) {
            if(reservedBookings.get(code)!= null){
                if(reservedBookings.get(code).user.equals(user)){
                    for(Flight f : reservedBookings.get(code).trip){
                        f.setCapacity(f.getCapacity()+1);
                    }
                    reservedBookings.remove(code);
                    return true;
                }
            }
            return false;
        }

        String userReservedCodes(User user){
            String ret = " ";
            List<Integer> codes = new ArrayList<>();
            for(Reserva r : reservedBookings.values()){
                if(r.user.equals(user)){
                    codes.add(r.codeReserve);
                }
            }
            int size = codes.size();

            for(int i = 0; i < size; i++){
                if(i == size-1){
                    ret += codes.get(i) + " ";
                } else{
                    ret += codes.get(i) + "-";
                }

            }
            return ret;
        }
    }

    private class Reserva implements Serializable{
        public int codeReserve;
        public Date day;
        public User user;
        public List<Flight> trip;

        public Reserva(int codeReserve, Date day, User user, List<Flight> trip) {
            this.codeReserve = codeReserve;
            this.day = day;
            this.user = user;
            this.trip = trip;
        }
    }
}


// locks