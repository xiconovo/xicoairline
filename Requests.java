import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

interface Request {
    String serialize();

    static Request deserialize(String in_data) {
        return null;
    }

}


interface Response extends Request {

}

class RequestLogin implements Request {
    static final int REQUEST_NUMBER = 1;
    final String username;
    final String password;

    public RequestLogin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String serialize() {
        return String.format("%d;%s;%s", REQUEST_NUMBER, username, password);
    }

    public static RequestLogin deserialize(String in_data) {
        String[] split_data = in_data.split(";");
        return new RequestLogin(split_data[0], split_data[1]);
    }
}

class ResponseOk implements Response {
    final boolean status;
    final String message;

    public ResponseOk(boolean ok, String message) {
        this.status = ok;
        this.message = message;
    }

    public String serialize() {
        return String.format("%b;%s", status, message);
    }

    public static ResponseOk deserialize(String in_data) {
        String[] split_data = in_data.split(";");
        return new ResponseOk(Boolean.parseBoolean(split_data[0]), split_data[1]);
    }
}

class ResponseBooking implements Response {
    final boolean status;
    final String message;
    final int codeReserve;

    public ResponseBooking(boolean ok, String message, int codeReserve) {
        this.status = ok;
        this.message = message;
        this.codeReserve = codeReserve;
    }

    public String serialize() {

        return String.format("%b;%s;%d", status, message,codeReserve);
    }

    public static ResponseBooking deserialize(String in_data) {
        String[] split_data = in_data.split(";");
        return new ResponseBooking(Boolean.parseBoolean(split_data[0]), split_data[1],Integer.parseInt(split_data[2]));
    }
}


class RequestRegister implements Request {
    static final int REQUEST_NUMBER = 2;
    final String username;
    final String password;

    public RequestRegister(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String serialize() {
        return String.format("%d;%s;%s", REQUEST_NUMBER, username, password);
    }

    public static RequestRegister deserialize(String in_data) {
        String[] split_data = in_data.split(";");
        return new RequestRegister(split_data[0], split_data[1]);
    }
}

class RequestAddFlight implements Request {
    static final int REQUEST_NUMBER = 3;
    final String origin;
    final String destination;
    final int capacity;

    public RequestAddFlight(String origin, String destination, int capacicy) {
        this.origin = origin;
        this.destination = destination;
        this.capacity = capacicy;
    }

    public String serialize() {
        return String.format("%d;%s;%s;%d", REQUEST_NUMBER, origin, destination, capacity);
    }
}

class RequestBooking implements Request {
    static final int REQUEST_NUMBER = 4;
    final String route;
    final Date start;
    final Date end;


    public RequestBooking(String route, Date start, Date end) {
        this.route = route;
        this.start = start;
        this.end = end;
    }

    public String serialize(){
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String started = dateFormat.format(start);
        String ended = dateFormat.format(end);
        return String.format("%s;%s;%s", route,start,end);
    }

    public static RequestBooking deserialize(String in_data) throws ParseException {
        String[] split_data = in_data.split(";");
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date start = formatter.parse(split_data[1]);
        Date end = formatter.parse(split_data[2]);
        return new RequestBooking(split_data[0],start,end);
    }
}

