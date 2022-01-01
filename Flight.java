import java.util.ArrayList;
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

