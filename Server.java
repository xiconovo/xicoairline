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
    int reservedCounter = 0;

    public static void main(String[] args) {
        System.out.println("Hell World! I'm Server");
        Server server = new Server();
        server.startServer(PORT);
    }

    void startServer(int port) {
        try {
            user_manager.restoreUsers();
        } catch (IOException e) {
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

        void executeRequest(String request_data) throws ParseException {
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
                    int codeReserve = booking_manager.reserveTrip(req.route, req.start, req.end);
                    Response response;
                    if (codeReserve >= 0) {
                        response = new ResponseBooking(true, "Booked successful", reservedCounter);
                    } else {
                        response = new ResponseBooking(true, "Unsuccessful reserved", -1);
                    }
                    sendResponse(response);
                }
                break;

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
    static String BACKUP_FILE_TRIPS = "trips.txt";

    private class UserManager {
        List<User> users = new ArrayList<>();

        public boolean addUser(String name, String hash) {
            User user_found = getUserByName(name);
            if (user_found != null) {
                System.out.println("User already exsists");
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
    /*
    [Portugal,Espanha,Franca,Suica,Belgica]


    private class BookingManager {
        Map<Date,List<Flight>> flightsPerDay = new HashMap<>();

        public int reserveTrip(String route, Date start, Date end) {

            String[] countrys = route.split("-");
            int size = countrys.length - 1;
            if()
            for(int i = 0; i<countrys.length; i++){
                hasCapacity(countrys[i],countrys[i+1]);
            }



        }

        public Date compareDate(Date start, Date end, Flight f) {
            Date tmpDate = f.getDay();
            if ((tmpDate.after(start) && tmpDate.before(end)) || tmpDate.equals(start) || tmpDate.equals(end)) {
                return tmpDate;
            }
            return null;
        }

        public boolean compareCountrys(String source, String destination, Flight f) { // true caso um voo tenho a origem e o destino desejados
            return f.getSource().equalsIgnoreCase(source) && f.getDestination().equalsIgnoreCase(destination);
        }


        public Flight getFlightByID(int id) {
            for (Flight f : flights) {
                if (f.getId() == id) {
                    return f;
                }
            }
            return null;
        }


    }

     */

    private class AdminManager{

    }



}