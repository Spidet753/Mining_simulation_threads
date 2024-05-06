import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Semaphore;
import static java.lang.Thread.sleep;

/**
 * Class that works with Lorry
 * @author Václav Prokop
 */
public class Lorry implements Runnable{
    //== Private attributes
    /**
     * Time, how long is lorry driving
     */
    private final int tLorry;
    /**
     * Maximum capacity of lorry
     */
    private final int maxCapacity;
    /**
     * Capacity that is used right now
     */
    private volatile int inventory = 0;
    /**
     * Number of created instances
     */
    private static int LCount = 0;
    /**
     * Date format used with milliseconds used in the output file
     */
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");

    //== Public attributes
    /**
     * Number of the current instance
     */
    private final int vNumber;
    /**
     * Time when lorry instance was created
     */
    private final long start;
    /**
     * Semaphore used to route traffic
     */
    private static final Semaphore semaphore = new Semaphore(1);

    private final BufferedWriter writer;

    private final Ferry ferry;

    /**
     * Constructor
     * @param maxCapacity maximum capacity of Lorry
     * @param tLorry how long it takes to lorry get to ferry
     */
    public Lorry(int maxCapacity, int tLorry, BufferedWriter writer, Ferry ferry){
    this.maxCapacity = maxCapacity;
    this.tLorry = tLorry;
    LCount++;
    vNumber = LCount;
    start = System.nanoTime();
    this.writer = writer;
    this.ferry = ferry;
    }

    /**
     * Run method for threads
     */
    public void run(){
        long start = System.nanoTime();

        //lorry is driving
        try {
            sleep((int)(tLorry*Math.random()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Lorry is at ferry
        long end = System.nanoTime();
        long time = (end - start) / 1000000;
        logLorryArrival(vNumber, time, writer);
            fillFerry();

    }

    /**
     * Method that ensures that ferry is filled by lorries, when ferry's
     * capacity is reached, lorries will wait for ferry to come back
     */
    public void fillFerry() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
         throw new RuntimeException(e);
        }

        if (ferry.getInventory() == ferry.getMaxCapacity() - 1) {
            //log of departuring
            long end = System.nanoTime();
            long temp = (end - ferry.getStart()) / 1000000;
            logFerryDeparture(temp, writer);
            long start2 = System.nanoTime();

            //last lorry to count
            ferry.sumInventory(1);
            ferry.sumSources(this.inventory);

            //set value to default, and add trasfered sources
            ferry.setTrasferedSources(ferry.getSources());
            ferry.setInventory(0);
            ferry.setSources(0);

            //time to travel tLorry time
            try {
                sleep((int)(tLorry*Math.random()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            long end2 = System.nanoTime();
            long temp2 = (end2 - start2) / 1000000;

            //lorries are leaving
            for (int i = 0; i < ferry.getMaxCapacity(); i++) {
                logLorryEnds(Main.getReadyLorrys().peek().getvNumber(), temp2, writer);
                Main.getReadyLorrys().remove();
            }

        } else {
            ferry.sumInventory(1);
            ferry.setTrasferedSources(Main.getReadyLorrys().peek().inventory);
        }
        semaphore.release();
    }

    /**
     * Getter of maximum capacity of lorry
     * @return max capacity
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Getter of Lorry's instance inventory
     * @return inventory value (int)
     */
    public synchronized int getInventory() {
        return inventory;
    }

    /**
     * Setter of Lorry's instance inventory
     * @param inventory adds setted inventory to value saved in atributte
     */
    public synchronized void setInventory(int inventory) {
        this.inventory += inventory;
    }

    /**
     * Arrival log which sets up output for output file
     * @param LorryNumber Thread number of lorry
     * @param time How long it took the lorry to arrive at ferry
     */
    private void logLorryArrival(int LorryNumber, long time, BufferedWriter writer) {
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format("%s - Náklaďák %d dojel k trajektu, trvalo mu to %d ms.\n", timeStamp, LorryNumber, time);
        writeToLogFile(logMessage, writer);
    }

    /**
     * Tells the bufferedWriter from Main to write down a message
     * @param logMessage message to write
     */
    private void writeToLogFile(String logMessage, BufferedWriter writer) {
        try  {
            writer.write(logMessage);
        } catch (IOException e) {
            throw new RuntimeException("Chyba při zápisu do souboru.", e);
        }
    }

    /**
     * Log of ferry departure
     * @param time How long it took lorries to fill ferry
     */
    public void logFerryDeparture(long time, BufferedWriter writer){
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format("%s - Trajekt odjíždí, trvalo ho naplnit %d ms.\n", timeStamp, time);
        System.out.println(timeStamp + " - Vyjíždí trajekt.\n");
        writeToLogFile(logMessage, writer);
    }

    /**
     * Log of lorries driving to the end
     * @param vNumber (int) number of lorry
     * @param time (int) how long it took
     */
    public void logLorryEnds(int vNumber, long time, BufferedWriter writer){
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format("%s - Náklaďák %d přijel na místo určení, trvalo mu to %d ms.\n", timeStamp, vNumber, time);
        writeToLogFile(logMessage, writer);
    }

    public int getvNumber() {
        return vNumber;
    }

    public long getStart() {
        return start;
    }
}
