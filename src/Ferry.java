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
    private int maxCapacity;

    //== Public attributes
    /**
     * Sources that are in the final position
     */
    public volatile int trasferedSources = 0;
    /**
     * capacity of ferry right now
     */
    public volatile int inventory = 0;
    /**
     * Sources, that ferry is carrying right now
     */
    public volatile int sources = 0;
    /**
     * Moment, when ferry is ready to fill
     */
    public long start = 0;

    /**
     * Constructor
     * @param maxCapacity maximum capacity of Ferry
     */
    public Ferry(int maxCapacity){
        this.maxCapacity = maxCapacity;
    }

    /**
     * Run method for threads
     */
    public void run(){
        while(trasferedSources >= Foreman.getCountOfsource()){
            while(inventory != maxCapacity){
                try {
                    sleep(0);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            start = System.nanoTime();
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
    public  void setInventory(int inventory) {
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
    public int getTrasferedSources() {
        return trasferedSources;
    }

    public synchronized void setTrasferedSources(int trasferedSources) {
        this.trasferedSources += trasferedSources;
    }
}
