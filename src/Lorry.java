import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Thread.sleep;

public class Lorry implements Runnable{

    public int tLorry;
    public int maxCapacity;
    public int inventory = 0;
    public int vNumber;
    public static int LCount = 0;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");

    public boolean ready = false;

    public Lorry(int maxCapacity, int tLorry){
    this.maxCapacity = maxCapacity;
    this.tLorry = tLorry;
    LCount++;
    vNumber = LCount;
    }

    public void run(){
        long start = System.nanoTime();
        while(!ready){
            try {
                if(Main.workerThreadGroup.activeCount() == 0){
                    ready = true;
                }
                sleep(tLorry/2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        long end = System.nanoTime();
        long time = (end - start) / 1000000;
        try {
            Main.writer.write("inventory " + inventory+"\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logFullEvent(this.vNumber, time);

//        //if lorry is full, miner sets up new lorry
//        Lorry lorry = new Lorry(Main.Lorrys.getLast().maxCapacity,Main.Lorrys.getLast().tLorry);
//        Main.Lorrys.add(lorry);
//        Thread lorryThread = new Thread(Main.lorryThreadGroup, lorry);
//        lorryThread.start();
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
     * log outputing situation when lorry is full
     * @param vNumber Thread number of lorry
     * @param time How long it took to workers to full that lorry
     */
    private synchronized void logFullEvent(int vNumber, long time) {
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format("%s - Náklaďák %d je plný, naplnit ho trvalo %d ms.\n", timeStamp, vNumber, time);
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
}
