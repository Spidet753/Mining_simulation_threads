import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import static java.lang.Thread.sleep;

public class Worker implements Runnable {
    private final int wNumber;
    private final int timePerX;
    private int inventory = 0;
    private int inventorySumOfMined = 0;
    private static volatile int inventorySum = 0;
    private int miningTime;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
    private static int count = 0;
    public static volatile boolean wasLast = false;

    public static int doneWorkers = 0;

    /**
     * Constructor
     * @param wNumber number of thread
     * @param timePerX how long it takes to mine a source
     */
    public Worker(int wNumber, int timePerX) {
        this.wNumber = wNumber;
        this.timePerX = timePerX;
        count++;
    }

    /**
     * Run thread method for workers to mine blocks
     */
    @Override
    public void run() {
        while (!Foreman.blocks.isEmpty()) {
            pickABlock();
        }
        synchronized (Worker.class) {
            inventorySum += inventorySumOfMined;
        }
    }

    /**
     * Method that gives a worker specific block to work with
     */
    public synchronized void pickABlock() {
        if (!Foreman.blocks.isEmpty()) {
            String block = Foreman.blocks.poll();
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
                    putIntoLorry(this.wNumber,this.inventory);

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
    public void putIntoLorry(int wNumber, int wInventory) throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);
        while(wInventory > 0) {
            semaphore.w();
            //TODO SET LORRY TIME
            sleep(0);
            logPutting(wNumber, 1);
            synchronized (Lorry.class) {
                fullLorry();
                if(Main.readyLorrys.peek() != null)
                    System.out.println("xd " + Main.readyLorrys.peek().inventory);
            }
            wInventory--;
            semaphore.free();
        }
    }

    public synchronized void fullLorry() {

        if (Main.emptyLorrys.peek().inventory >= Main.emptyLorrys.peek().maxCapacity) {
            System.out.println(Main.workerThreadGroup.activeCount());
            Main.emptyLorrys.peek().ready = true;
            long end = System.nanoTime();
            long temp = (end - Main.emptyLorrys.peek().start) / 1000000;
            logFullEvent(Main.emptyLorrys.peek().vNumber, temp);
            Main.readyLorrys.add(Main.emptyLorrys.peek());
            Lorry lorry = new Lorry(Main.capacityOfLorry, Main.timeOfLorry);
            Main.emptyLorrys.add(lorry);
            Thread lorryThread = new Thread(Main.lorryThreadGroup, lorry);
            Main.emptyLorrys.remove();
            lorryThread.start();
            Main.emptyLorrys.peek().setInventory(1);
        }
        else if(doneWorkers == count){
            lastLorry();
        }
        else {
                for (int i = 0; i < count; i++) {
                    if (Main.workers[i].inventory == 1 && Foreman.blocks.isEmpty() ) {
                        doneWorkers++;
                    }
                }
             Main.emptyLorrys.peek().setInventory(1);
        }
    }

    public synchronized void lastLorry(){
            try {
                Main.writer.write("inventory " + Main.emptyLorrys.peek().inventory+":)\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            long end = System.nanoTime();
            long time = (end - Main.emptyLorrys.peek().start) / 1000000;
            logFullEvent(Main.emptyLorrys.peek().vNumber, time);
            Main.emptyLorrys.peek().ready = true;
            Main.readyLorrys.add(Main.emptyLorrys.peek());

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
                                         " zdrojů do Náklaďáku: "+ Main.emptyLorrys.peek().vNumber + "\n");
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
