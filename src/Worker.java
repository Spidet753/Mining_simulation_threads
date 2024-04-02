import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Semaphore;
import static java.lang.Thread.sleep;

/**
 * Class that works with Workers
 * @author Václav Prokop
 */
public class Worker implements Runnable {
    private final int wNumber;
    private final int timePerX;
    private int inventory = 0;
    private int inventorySumOfMined = 0;
    private static volatile int inventorySum = 0;
    private int miningTime;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
    private static int sources = 0;

    /**
     * Constructor
     * @param wNumber number of thread
     * @param timePerX how long it takes to mine a source
     */
    public Worker(int wNumber, int timePerX) {
        this.wNumber = wNumber;
        this.timePerX = timePerX;
    }

    /**
     * Run thread method for workers to mine blocks
     */
    @Override
    public void run() {
        while (!Foreman.getBlocks().isEmpty()) {
            pickABlock();
        }
        synchronized (Worker.class) {
            inventorySum += inventorySumOfMined;
        }
        Thread.currentThread().interrupt();
    }

    /**
     * Method that gives a worker specific block to work with
     */
    public synchronized void pickABlock() {
        if (!Foreman.getBlocks().isEmpty()) {
            String block = Foreman.getBlocks().poll();
            if (block != null) {
                mine(block);
            }
        }
    }

    /**
     * Method that ensures that the block's sources are processed
     * @param block block given by Foreman
     */
    private void mine(String block) {
        long temp1 = System.nanoTime();
        int blockLength = block.length();
        for (int i = blockLength; i > 0; i--) {
            try {
                //TODO CHANGE MININGTIME
                miningTime = (int)(0 * Math.random());
                sleep(miningTime);
                this.inventory += 1;
                logMiningEvent(wNumber, miningTime);
                if(i == 1){
                    long temp2 = System.nanoTime();
                    long tBlockMining = (temp2 - temp1) / 1000000;
                    logCarringEvent(wNumber, inventory);
                    logBlockMinedEvent(wNumber, tBlockMining);

                    //put sources into Lorry
                    putIntoLorry(this.inventory);

                    this.inventorySumOfMined += this.inventory;
                    this.inventory = 0;
                    break;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Method, where worker is putting his inventory into lorry, one source per tLorry
     * @param wInventory inventory of worker
     */
    public void putIntoLorry(int wInventory) throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);
        while (wInventory > 0) {
            semaphore.acquire();
            //TODO SET LORRY TIME
            sleep(1);
            fullLorry();

            wInventory--;
            semaphore.release();
        }
    }

    /**
     * Method which creates new lorry, if the current lorry is full,
     * and solves exception problems with filling
     */
    public synchronized void fullLorry() {
        synchronized (Worker.class) {
            Main.getEmptyLorrys().peek().setInventory(1);
            sources += 1;
            logPutting(wNumber, 1);

            if (Main.getEmptyLorrys().peek().getInventory() >= Main.getEmptyLorrys().peek().getMaxCapacity()) {

                //time to leave
                long end = System.nanoTime();
                long temp = (end - Main.getEmptyLorrys().peek().start) / 1000000;

                //add it to readyLorrys
                Main.getReadyLorrys().add(Main.getEmptyLorrys().peek());

                //log it
                logFullEvent(Main.getEmptyLorrys().peek().vNumber, temp);

                //and create new lorry to go
                Lorry lorry = new Lorry(Main.getCapacityOfLorry(), Main.getTimeOfLorry());
                Main.getEmptyLorrys().add(lorry);
                Thread lorryThread = new Thread(Main.lorryThreadGroup, lorry);
                lorryThread.start();
                Main.getEmptyLorrys().remove();
            }

            if (sources >= Foreman.getCountOfsource() && Main.getEmptyLorrys().peek().getInventory() != 0) {
                //miners mined all sources, time to send last lorry
                //time to leave
                long end = System.nanoTime();
                long temp = (end - Main.getEmptyLorrys().peek().start) / 1000000;

                //add it to readyLorrys
                Main.getReadyLorrys().add(Main.getEmptyLorrys().peek());

                //log it
                logFullEvent(Main.getEmptyLorrys().peek().vNumber, temp);
            }
        }
    }

    /**
     * Mining log which sets up output for output file
     * @param workerNumber Thread number of worker
     * @param miningTime How long it took the worker to mine a source
     */
    private synchronized void logMiningEvent(int workerNumber, int miningTime) {
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format("%s - Dělník %d vytěžil zdroj, trvalo mu to %d ms.\n", timeStamp, workerNumber, miningTime);
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

    /**
     * Log message telling that worker is bringing sources to Lorry
     * @param workerNumber (int) number of Thread
     * @param inventoryCount (int) how many sources is he bringing
     */
    public synchronized void logCarringEvent(int workerNumber, int inventoryCount){
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format(timeStamp + " - Dělník " + workerNumber + " nese " + inventoryCount + " zdrojů.\n");
        writeToLogFile(logMessage);
    }

    /**
     * Log message telling that worker is bringing sources to Lorry
     * @param workerNumber (int) number of Thread
     * @param inventoryCount (int) how many sources is he bringing
     */
    public synchronized void logPutting(int workerNumber, int inventoryCount){
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format(timeStamp + " - Dělník " + workerNumber + " nakládá " + inventoryCount +
                                         " zdrojů do Náklaďáku: "+ Main.getEmptyLorrys().peek().vNumber + "\n");
        writeToLogFile(logMessage);
    }

    /**
     * Log which sets up output for a mined block
     * @param workerNumber (int) Thread number which completed a block
     * @param time (long) how long did it take in milliseconds
     */
    public synchronized void logBlockMinedEvent(int workerNumber, long time){
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format(timeStamp + " - Dělník " + workerNumber + " vytěžil celý blok, trvalo mu to " + time + " ms.\n");
        writeToLogFile(logMessage);
    }

    /**
     * log outputing situation when lorry is full
     * @param vNumber Thread number of lorry
     * @param time How long it took to workers to full that lorry
     */
    private synchronized void logFullEvent(int vNumber, long time) {
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format("%s - Náklaďák %d je připraven vyrazit, naplnit ho trvalo " + time + " ms.\n", timeStamp, vNumber);
        writeToLogFile(logMessage);
    }

    /**
     * Getter of a private atrbitut wNumber
     * @return (int) Thread number
     */
    public int getwNumber() {
        return wNumber;
    }

    /**
     * Getter of all mined sources by an instance of worker
     * @return (int) mined sources by specific worker
     */
    public int getInventorySumOfMined() {
        return inventorySumOfMined;
    }

    /**
     * Getter of all mined sources by all workers
     * @return (int) mined sources
     */
    public static int getInventorySum() {
        return inventorySum;
    }
}
