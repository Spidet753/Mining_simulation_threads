import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Thread.sleep;

/**
 * Class that works with Ferry
 * @author Václav Prokop
 */
public class Ferry implements Runnable{

    //== Private attributes
    private int maxCapacity;
    public volatile int inventory = 0;
    private static int FCount = 0;
    public volatile int sources = 0;
    private volatile int trasferedSources = 0;

    //== Public attributes
    public int vNumber;
    public long start;

    /**
     * Constructor
     * @param maxCapacity maximum capacity of Ferry
     */
    public Ferry(int maxCapacity){
        this.maxCapacity = maxCapacity;
        FCount++;
        vNumber = FCount;
        start = System.nanoTime();
    }

    /**
     * Run method for threads
     */
    public void run(){
        while(trasferedSources != Foreman.getCountOfsource()){
            while(inventory != maxCapacity){
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            long start = System.nanoTime();

            //ferry is driving
            long end = System.nanoTime();
            long time = (end - start) / 1000000;

            //set default values
            inventory = 0;
            sources = 0;
        }
    }

    /**
     * Getter of maximum capacity of ferry
     * @return max capacity
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Getter of ferry's instance inventory
     * @return inventory value (int)
     */
    public int getInventory() {
        return inventory;
    }

    /**
     * Setter of ferry's instance inventory
     * @param inventory adds setted inventory to value saved in atributte
     */
    public synchronized void setInventory(int inventory) {
        this.inventory += inventory;
    }

    /**
     * Getter of sources on the ferry
     * @return (int) sources
     */
    public int getSources() {
        return sources;
    }

    /**
     * Setter of sources on the ferry
     */
    public synchronized void setSources(int sources) {
        this.sources += sources;
    }

    /**
     * Setter of sources that are in the final position
     */
    public synchronized void setTrasferedSources(int trasferedSources) {
        this.trasferedSources += trasferedSources;
    }

    public int getTrasferedSources() {
        return trasferedSources;
    }
}
