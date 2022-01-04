import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;



public class Flight{
    private String source;
    private String destination;
    private int capacity;

    public Flight(String source, String destination, int capacity) {
        this.source = source;
        this.destination = destination;
        this.capacity = capacity;
    }


    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

}
/*
    public Server.Reserva reservedOnDate(String route, String started, String ended) throws ParseException, IOException {
        Server.Reserva reserva = new Server.Reserva();
        SimpleDateFormat DateFor = new SimpleDateFormat("dd/MM/yyyy");
        Date start = DateFor.parse(started);
        Date end = DateFor.parse(ended);
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
                    reserva.trip.add(f);
                    f.setCapacity(f.getCapacity() - 1);
                }
                reservedCounter++; // WARNING: LOCK
                reserva.codeReserve = reservedCounter;
                reserva.day = start;
                return reserva;
            }
            //em baixo é somar 1 ao dia
            c.setTime(start);
            c.add(Calendar.DATE, 1);
            start = c.getTime();
        }
        System.out.println("chegou aqui");
        return reserva;


        case RequestBooking.REQUEST_NUMBER: {
            RequestBooking req = RequestBooking.deserialize(split_data[1]);
            Server.Reserva reserva = booking_manager.reservedOnDate(req.route, req.start, req.end);
            reserva.user = user;
            System.out.println("Reserved Date: " + reserva.day);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String dateString = dateFormat.format(reserva.day);
            System.out.println("Data parsada: " + dateString);
            Response response;
            if (reserva.day == null) {
                response = new ResponseBooking(false, "Unsuccessful reserved", -1);
            } else {
                reservedBookings.put(reserva.codeReserve, reserva);
                response = new ResponseBooking(true, "Booked successful on day " + dateString, reservedCounter);

            }
            sendResponse(response);
        }

 */