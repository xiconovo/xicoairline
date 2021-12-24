
interface Request {
    String serialize();
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
        return String.format("%d;%s;%s;%d", REQUEST_NUMBER, origin, destination, destination);
    }
}