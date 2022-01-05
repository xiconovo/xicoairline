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


class RequestBooking implements Request {
    static final int REQUEST_NUMBER = 4;
    final String route;
    final String start;
    final String end;


    public RequestBooking(String route, String start, String end) {
        this.route = route;
        this.start = start;
        this.end = end;
    }

    public String serialize(){
        return String.format("%d;%s;%s;%s", REQUEST_NUMBER,route,start,end);
    }

    public static RequestBooking deserialize(String in_data) {
        String[] split_data = in_data.split(";");
        return new RequestBooking(split_data[0],split_data[1],split_data[2]);
    }
}

class RequestBookingCancel implements Request{
    static final int REQUEST_NUMBER = 5;
    final int codeReserve;


    public RequestBookingCancel(int codeReserve){
        this.codeReserve = codeReserve;
    }

    public String serialize(){
        return String.format("%d;%d",REQUEST_NUMBER, codeReserve);
    }

    public static RequestBookingCancel deserialize(String in_data) {
        String[] split_data = in_data.split(";");
        return new RequestBookingCancel(Integer.parseInt(split_data[0]));
    }
}

class ResponseBookingCancel implements Response {
    final boolean status;
    final String message;

    public ResponseBookingCancel(boolean ok, String message) {
        this.status = ok;
        this.message = message;
    }

    public String serialize() {
        return String.format("%b;%s", status, message);
    }

    public static ResponseBookingCancel deserialize(String in_data) {
        String[] split_data = in_data.split(";");
        return new ResponseBookingCancel(Boolean.parseBoolean(split_data[0]), split_data[1]);
    }
}


class RequestBookedList implements Request{
    static final int REQUEST_NUMBER = 6;

    public String serialize(){
        return String.format("%d",REQUEST_NUMBER);
    }
}

class ResponseBookedList implements Response{
    final boolean status;
    final String listOfCodes;

    public ResponseBookedList(boolean status,String listOfCodes){
        this.status = status;
        this.listOfCodes = listOfCodes;
    }

    public String serialize() {
        return String.format("%b;%s", status, listOfCodes);
    }


    public static ResponseBookedList deserialize(String in_data) {
        String[] split_data = in_data.split(";");
        return new ResponseBookedList(Boolean.parseBoolean(split_data[0]), split_data[1]);
    }
}

class RequestAddFlight implements Request{
    static final int REQUEST_NUMBER = 7;
    final String source;
    final String destination;
    final int capacity;

    public RequestAddFlight(String source, String destination, int capacity){
        this.source = source;
        this.destination = destination;
        this.capacity = capacity;
    }

    public String serialize(){
        return String.format("%d;%s;%s;%d", REQUEST_NUMBER,source,destination,capacity);
    }

    public static RequestAddFlight deserialize(String in_data) {
        String[] split_data = in_data.split(";");
        return new RequestAddFlight(split_data[0],split_data[1],Integer.parseInt(split_data[2]));
    }


}

class ResponseAddFlight implements Response{
    final boolean status;
    final String message;

    public ResponseAddFlight(boolean status, String message){
        this.status = status;
        this.message = message;
    }

    public String serialize() {
        return String.format("%b;%s", status, message);
    }


    public static ResponseAddFlight deserialize(String in_data) {
        String[] split_data = in_data.split(";");
        return new ResponseAddFlight(Boolean.parseBoolean(split_data[0]), split_data[1]);
    }


}

class RequestDayClose implements Request{
    static final int REQUEST_NUMBER = 3;
    final String day;

    public RequestDayClose(String day){
        this.day = day;
    }

    public String serialize(){
        return String.format("%d;%s", REQUEST_NUMBER,day);
    }

    public static RequestDayClose deserialize(String in_data) {
        String[] split_data = in_data.split(";");
        return new RequestDayClose(split_data[0]);
    }
}


class ResponseCloseDay implements Response{
    final boolean status;
    final String message;

    public ResponseCloseDay(boolean status, String message){
        this.status = status;
        this.message = message;
    }

    public String serialize() {
        return String.format("%b;%s", status, message);
    }


    public static ResponseCloseDay deserialize(String in_data) {
        String[] split_data = in_data.split(";");
        return new ResponseCloseDay(Boolean.parseBoolean(split_data[0]), split_data[1]);
    }


}