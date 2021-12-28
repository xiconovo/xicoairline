import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Trip{
    private List<String> flights; // lista com os IDS dos flights
}


class Flight{
    private int id;
    private String source;
    private String destination;
    private int capacity;
    private Date day;
    private List<Integer> reserves = new ArrayList<>(capacity);


    public int getId() {
        return id;
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

    public Date getDay() {
        return day;
    }

    public List<Integer> getReserves() {
        return reserves;
    }
}