import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Thread.sleep;

public class Lorry implements Runnable{

    public int tLorry;
    public int maxCapacity;
    public volatile int inventory = 0;
    public int vNumber;
    public static int LCount = 0;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");

    public boolean ready = false;

    public long start;

    public Lorry(int maxCapacity, int tLorry){
    this.maxCapacity = maxCapacity;
    this.tLorry = tLorry;
    LCount++;
    vNumber = LCount;
    }

    public void run(){
        start = System.nanoTime();
        while(!ready){
            try {
                if(Main.getWorkerThreadGroup().activeCount() < 2){
                    ready = true;
                }
                sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        long end = System.nanoTime();
        long time = (end - start) / 1000000;
        Thread.currentThread().interrupt();
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getInventory() {
        return inventory;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public void setInventory(int inventory) {
        this.inventory += inventory;
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
}
