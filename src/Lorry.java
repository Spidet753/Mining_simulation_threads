import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Thread.sleep;

/**
 * Class that works with Lorry
 * @author Václav Prokop
 */
public class Lorry implements Runnable{
    //== Private attributes
    private int tLorry;
    private int maxCapacity;
    private volatile int inventory = 0;
    private static int LCount = 0;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
    private LinkedBlockingQueue<Lorry> listOfLorries = new LinkedBlockingQueue<>();

    //== Public attributes
    public int vNumber;
    public long start;

    /**
     * Constructor
     * @param maxCapacity maximum capacity of Lorry
     * @param tLorry how long it takes to lorry get to ferry
     */
    public Lorry(int maxCapacity, int tLorry){
    this.maxCapacity = maxCapacity;
    this.tLorry = tLorry;
    LCount++;
    vNumber = LCount;
    start = System.nanoTime();
    }

    /**
     * Run method for threads
     */
    public void run(){
        long start = System.nanoTime();

        //lorry is driving
        try {
            //TODO SET TIME FOR tLorry
            sleep((int)(2*Math.random()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Lorry is at ferry
        long end = System.nanoTime();
        long time = (end - start) / 1000000;
        logFerryArrival(vNumber, time);

        fillFerry();

        Thread.currentThread().interrupt();
    }

    /**
     * Method that ensures that ferry is filled by lorries, when ferry's
     * capacity is reached, lorries will wait for ferry to come back
     */
    public synchronized void fillFerry(){
        synchronized (Lorry.class) {
            long start = System.nanoTime();
            if (Main.ferries.peek().getInventory() == maxCapacity - 1) {

                //last lorry to count
                Main.ferries.peek().setSources(this.inventory);
                listOfLorries.add(this);

                //log of departuring
                long end = System.nanoTime();
                long temp = (end - start) / 1000000;
                logFerryDeparture(temp);
                long start2 = System.nanoTime();

                //set value to default, and add trasfered sources
                Main.ferries.peek().setTrasferedSources(Main.ferries.peek().getSources());
                Main.ferries.peek().sources = 0;
                Main.ferries.peek().inventory = 0;

                //lorries are leaving
                for (int i = 0; i < maxCapacity; i++) {
                    long end2 = System.nanoTime();
                    long temp2 = (end2 - start2) / 1000000;

                    //time to travel
                    try {
                        //TODO SET RIGHT TIME
                        sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    logLorryEnds(listOfLorries.poll().vNumber, temp2);
                }
            } else {
                Main.ferries.peek().setInventory(1);
                Main.ferries.peek().setSources(this.inventory);
                listOfLorries.add(this);
            }
        }
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
    public int getInventory() {
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
    private synchronized void logFerryArrival(int LorryNumber, long time) {
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format("%s - Náklaďák %d dojel k trajektu, trvalo mu to %d ms.\n", timeStamp, LorryNumber, time);
        System.out.println(timeStamp + " - Vyjíždí přívoz.\n");
        writeToLogFile(logMessage);
    }

    /**
     * Tells the bufferedWriter from Main to write down a message
     * @param logMessage message to write
     */
    private static synchronized void writeToLogFile(String logMessage) {
        try  {
            Main.writer.write(logMessage);
        } catch (IOException e) {
            throw new RuntimeException("Chyba při zápisu do souboru.", e);
        }
    }

    public int gettLorry() {
        return tLorry;
    }

    /**
     * Log of ferry departure
     * @param time How long it took lorries to fill ferry
     */
    public void logFerryDeparture(long time){
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format("%s - Trajekt odjíždí, trvalo ho naplnit %d ms.\n", timeStamp, time);
        writeToLogFile(logMessage);
    }

    public void logLorryEnds(int vNumber, long time){
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format("%s - Náklaďák %d přijel na místo určení, trvalo mu to %d ms.\n", timeStamp, vNumber, time);
        writeToLogFile(logMessage);
    }
}
