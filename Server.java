import java.net.*;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

class Server {
    static int PORT = 4545;
    boolean running = true;
    UserManager user_manager = new UserManager();
    BookingManager booking_manager = new BookingManager();
    Map<Integer, Reserva> reservedBookings = new HashMap<>();
    int reservedCounter = 0;

    public static void main(String[] args) throws ParseException {
        System.out.println("Hello World! I'm Server");
        Server server = new Server();
        server.startServer(PORT);
    }

    void startServer(int port) throws ParseException {

        /* BookingManager mng = new BookingManager();
        try {
            mng.restoreFlights();
            mng.restoreFlightsPerDay();
            SimpleDateFormat DateFor = new SimpleDateFormat("dd/MM/yyyy");
            Date date = DateFor.parse("06/01/2022");
            Date reserva = mng.reservedOnDate("braga-asdasd",date, date);
            System.out.println("UM " + DateFor.format(reserva));
            reserva = mng.reservedOnDate("braga-faro", date, date);
            System.out.println("DOIS " +   DateFor.format(reserva));
            mng.saveFlightsPerDay();


        } catch (Exception e) {
            System.out.println("fodeu" + e);
        }
        System.exit(1);

         */


        try {
            user_manager.restoreUsers();
            booking_manager.restoreFlights(); // *** VERIFICAR ***
            booking_manager.restoreFlightsPerDay(); // *** VERIFICAR ***
        } catch (Exception e) {
            System.out.println("Failed to restore users: " + e);
        }

        System.out.println("Starting server on port: " + port);
        try (
                ServerSocket server_socket = new ServerSocket(port);) {
            System.out.println("Server started, waiting for client connections");
            while (running) {
                Socket client_socket = server_socket.accept();
                Thread t = new ClientHandler(client_socket);
                t.start();
            }
        } catch (IOException e) {
            System.out.println(
                    "Exception caught when trying to listen on port " + port + " or listening for a connection");
        }

    }

    private class ClientHandler extends Thread {
        final Socket sock;
        final PrintWriter out;
        final BufferedReader in;
        boolean client_connected = true;
        boolean is_logged_in = false;
        User user;

        public ClientHandler(Socket s) throws IOException {
            this.sock = s;
            this.out = new PrintWriter(this.sock.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
        }

        public void run() {
            System.out.println("New Client");

            while (client_connected) {
                try {
                    String in_data = in.readLine();
                    if (in_data != null) {
                        System.out.println("Received: '" + in_data + "'");
                        executeRequest(in_data);
                    } else {
                        client_connected = false;
                    }
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

            switch (request_number) {
                case RequestLogin.REQUEST_NUMBER: {
                    RequestLogin req = RequestLogin.deserialize(split_data[1]);
                    boolean ok = user_manager.verifyUser(req.username, req.password);
                    Response response;
                    if (ok) {
                        is_logged_in = true;
                        user = user_manager.getUserByName(req.username);
                        response = new ResponseOk(true, "Login success");
                    } else {
                        response = new ResponseOk(false, "Login failed");
                    }
                    sendResponse(response);
                }
                break;

                case RequestRegister.REQUEST_NUMBER: {
                    RequestRegister req = RequestRegister.deserialize(split_data[1]);
                    boolean ok = user_manager.addUser(req.username, req.password);
                    Response response;
                    if (ok) {
                        response = new ResponseOk(true, "Register success");
                    } else {
                        response = new ResponseOk(false, "Regist failed");
                    }
                    sendResponse(response);
                }
                break;

                case RequestBooking.REQUEST_NUMBER: {
                    RequestBooking req = RequestBooking.deserialize(split_data[1]);
                    Date date = booking_manager.reservedOnDate(req.route, req.start, req.end);
                    System.out.println("Reserved Date: " + date);
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    String dateString = dateFormat.format(date);
                    System.out.println("Data parsada: " + dateString);
                    Response response;
                    if (dateString.equalsIgnoreCase("30/11/0002")) {
                        response = new ResponseBooking(false, "Unsuccessful reserved", -1);
                    } else {
                        reservedCounter++; // WARNING: LOCK
                        Reserva newBooking = new Reserva(reservedCounter, date, user, booking_manager.trip);
                        reservedBookings.put(reservedCounter, newBooking);
                        response = new ResponseBooking(true, "Booked successful on day " + dateString, reservedCounter);

                    }
                    sendResponse(response);
                }
                break;

                case RequestBookingCancel.REQUEST_NUMBER: {
                    RequestBookingCancel req = RequestBookingCancel.deserialize(split_data[1]);
                }

            }
        }

        void sendResponse(Response resp) {
            this.out.println(resp.serialize());
        }
    }

    class User {
        int id;
        String name;
        String hash;


        public User(int id, String name, String hash) {
            this.id = id;
            this.name = name;
            this.hash = hash;
        }
    }

    static String BACKUP_FILE_USERS = "users.txt";
    static String BACKUP_FILE_FLIGHTS = "flights.txt";
    static String BACKUP_FILE_FLIGHTS_PER_DAY = "flightsPerDay.txt";

    private class UserManager {
        List<User> users = new ArrayList<>();

        public boolean addUser(String name, String hash) {
            User user_found = getUserByName(name);
            if (user_found != null) {
                System.out.println("User already exists");
                return false;
            }

            users.add(new User(users.size() + 1, name, hash));
            return true;
        }

        public void saveUsers() throws IOException {
            FileWriter file_writer = new FileWriter(BACKUP_FILE_USERS);
            PrintWriter print_writer = new PrintWriter(file_writer);
            for (User user : users) {
                print_writer.printf("%d,%s,%s\n", user.id, user.name, user.hash);
            }
            print_writer.close();
        }

        public void restoreUsers() throws IOException {
            FileReader file_reader = new FileReader(BACKUP_FILE_USERS);
            BufferedReader buf_reader = new BufferedReader(file_reader);

            String line = buf_reader.readLine();
            while (line != null) {
                String[] split = line.split(",");
                // todo: verify split is ok
                users.add(new User(Integer.parseInt(split[0]), split[1], split[2]));
                line = buf_reader.readLine();
            }
            buf_reader.close();
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


    private class BookingManager {
        public List<Flight> flights = new ArrayList<>(); // lista de voos geral
        public Map<Date, List<Flight>> flightsPerDay = new HashMap<>(); // Lista de voos para cada dia geral
        public List<Flight> trip = new ArrayList<>(); // lista com os voos de uma dada reserva


        public void anotherDay(Date data) {
            if (!flightsPerDay.containsKey(data)) {
                flightsPerDay.put(data, flights);
            }
        }


        public Date reservedOnDate(String route, String started, String ended) throws ParseException, IOException {
            SimpleDateFormat DateFor = new SimpleDateFormat("dd/MM/yyyy");
            Date start = DateFor.parse(started);
            Date end = DateFor.parse(ended);
            Date bookedOn = DateFor.parse("00/00/0000");
            Calendar c = Calendar.getInstance();
            String[] countrys = route.split("-");
            int size = countrys.length - 1;
            Flight flightToAdd;
            while (start.before(end) || start.equals(end)) {
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
                    System.out.println("fodase");
                    return start;
                }
                //em baixo é somar 1 ao dia
                c.setTime(start);
                c.add(Calendar.DATE, 1);
                start = c.getTime();
            }
            System.out.println("chegou aqui");
            return bookedOn;


        }

        public Flight isThereFlight(String source, String destination, Date day) throws IOException {
            Flight ret = null;
            List<Flight> day_flights = flightsPerDay.get(day);
            if (day_flights != null) {
                for (Flight f : day_flights) {
                    if (f.getSource().equalsIgnoreCase(source) && f.getDestination().equalsIgnoreCase(destination)) {
                        int capacity = f.getCapacity(); // WARNING: LOCK
                        if (capacity > 0) {
                            System.out.println("found flight " + f);
                            return f;
                        } else {
                            System.out.println("flight full " + f);
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
                        saveFlightsPerDay();
                    }
                    ret = f;
                }
            }
            return ret;
        }


        public void saveFlights() throws IOException {
            FileWriter file_writer = new FileWriter(BACKUP_FILE_FLIGHTS);
            PrintWriter print_writer = new PrintWriter(file_writer);
            for (Flight flight : flights) {
                print_writer.printf("%s,%s,%d\n", flight.getSource(), flight.getDestination(), flight.getCapacity());
            }
            print_writer.close();
        }

        public void restoreFlights() throws IOException {
            FileReader file_reader = new FileReader(BACKUP_FILE_FLIGHTS);
            BufferedReader buf_reader = new BufferedReader(file_reader);

            String line = buf_reader.readLine();
            while (line != null) {
                String[] split = line.split(",");
                // todo: verify split is ok
                flights.add(new Flight(split[0], split[1], Integer.parseInt(split[2])));
                line = buf_reader.readLine();
            }
            buf_reader.close();
        }

        public void saveFlightsPerDay() throws IOException {
            SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
            FileWriter file_writer = new FileWriter(BACKUP_FILE_FLIGHTS_PER_DAY);
            PrintWriter print_writer = new PrintWriter(file_writer);

            for (Map.Entry<Date, List<Flight>> entry : flightsPerDay.entrySet()) {
                String line = date_format.format(entry.getKey()) + ";";
                for (Flight flight : entry.getValue()) {
                    line += String.format("%s,%s,%d;", flight.getSource(), flight.getDestination(), flight.getCapacity());
                }
                line += "\n";
                print_writer.write(line);
            }
            print_writer.close();
        }

        public void restoreFlightsPerDay() throws IOException, ParseException {
            SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
            FileReader file_reader = new FileReader(BACKUP_FILE_FLIGHTS_PER_DAY);
            BufferedReader buf_reader = new BufferedReader(file_reader);

            String line = buf_reader.readLine();
            while (line != null) {
                String[] split = line.split(";");
                Date date = date_format.parse(split[0]);
                List<Flight> day_flights = new ArrayList<>();
                for (int i = 1; i < split.length; i++) {
                    String[] flight_split = split[i].split(",");
                    day_flights.add(new Flight(flight_split[0], flight_split[1], Integer.parseInt(flight_split[2])));
                }

                flightsPerDay.put(date, day_flights);
                line = buf_reader.readLine();
            }
            buf_reader.close();
        }


    }

    private class Reserva {
        private int codeReserve;
        private Date day;
        private User user;
        private List<Flight> trip;

        public Reserva(int codeReserve, Date day, User user, List<Flight> trip) {
            this.codeReserve = codeReserve;
            this.day = day;
            this.user = user;
            this.trip = trip;
        }

        public int getCodeReserve() {
            return codeReserve;
        }
    }


}