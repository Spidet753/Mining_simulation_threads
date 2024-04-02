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
    }

    /**
     * Run method for threads
     */
    public void run(){
        start = System.nanoTime();
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long end = System.nanoTime();
        long time = (end - start) / 1000000;
        Thread.currentThread().interrupt();
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
}
