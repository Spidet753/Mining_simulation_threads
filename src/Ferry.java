import static java.lang.Thread.sleep;

/**
 * Class that works with Ferry
 * @author Václav Prokop
 */
public class Ferry implements Runnable{

    //== Private attributes
    /**
     * Maximum capacity of ferry
     */
    private final int maxCapacity;
    /**
     * Sources that are in the final position
     */
    private volatile int trasferredSources = 0;
    /**
     * capacity of ferry right now
     */
    private volatile int inventory = 0;
    /**
     * Sources, that ferry is carrying right now
     */
    private volatile int sources = 0;
    /**
     * Moment, when ferry is ready to fill
     */
    private long start = 0;
    /**
     * instance of foreman to know about right amount of sources to be transferred
     */
    private final Foreman foreman;

    /**
     * Constructor
     * @param maxCapacity maximum capacity of Ferry
     * @param foreman instance of foreman
     */
    public Ferry(int maxCapacity, Foreman foreman){
        this.maxCapacity = maxCapacity;
        this.foreman = foreman;
    }

    /**
     * Run method for threads
     */
    public void run(){
        while(trasferredSources < foreman.getCountOfsource()){
            try {
                sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
    public void setInventory(int inventory) {
        this.inventory = inventory;
    }

    /**
     * Setter of ferry's instance inventory
     * @param inventory adds setted inventory to value saved in atributte
     */
    public synchronized void sumInventory(int inventory) {
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
    public void setSources(int sources) {
        this.sources = sources;
    }
    /**
     * adds sources to ferry
     */
    public synchronized void sumSources(int sources) {
        this.sources += sources;
    }

    /**
     * Getter of sources that are in the final position
     */
    public int getTrasferredSources() {
        return trasferredSources;
    }

    /**
     * Setter of transfered sources
     * @param trasferredSources int tranfered sources
     */
    public synchronized void setTrasferredSources(int trasferredSources) {
        this.trasferredSources += trasferredSources;
    }

    /**
     * Getter of time, when ferry is ready to go
     * @return long start
     */
    public long getStart() {
        return start;
    }

    /**
     * Setter of time, when ferry is ready to go
     * @return long start
     */
    public void setStart(long start) {
        this.start = start;
    }
}
