import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import static java.lang.Thread.sleep;

/**
 * Class that works with Workers
 * @author Václav Prokop
 */
public class Worker implements Runnable {
    //==Private Constants
    /**
     * Casts nanoseconds into milliseconds
     */
    private static final int TO_MILLIS = 1000000;
    //== Private attributes
    /**
     * Number of the current instance
     */
    private final int wNumber;
    /**
     * Time, how long it could take to mine a source
     */
    private final int timePerX;
    /**
     * How many sources is worker carrying
     */
    private int inventory = 0;
    /**
     * How many sources has worker already mined
     */
    private int inventorySumOfMined = 0;
    /**
     * Date format used with milliseconds used in the output file
     */
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");

    /**
     * Semaphore used to route traffic with fair order
     */
    private final Semaphore semaphore;
    /**
     * Writer used to print text into file
     */
    private final BufferedWriter writer;
    /**
     * Instance of foreman, to pick blocks from
     */
    private final Foreman foreman;
    /**
     * ferry instance given to lorries
     */
    private final Ferry ferry;
    /**
     * List of empty lorries
     */
    private final LinkedBlockingQueue<Lorry> emptyLorries;
    /**
     * List of ready lorries
     */
    private final LinkedBlockingQueue<Lorry> readyLorries;

    /**
     * Constructor
     * @param wNumber number of thread
     * @param timePerX how long it takes to mine a source
     * @param writer for outputing to a file
     * @param foreman gives workers blocks to work with
     * @param ferry given to a lorry instance
     * @param semaphore semaphore with fair order
     * @param emptyLorries list of empty lorries
     * @param readyLorries list of ready lorries
     */
    public Worker(int wNumber, int timePerX, BufferedWriter writer, Foreman foreman, Ferry ferry, Semaphore semaphore,
                  LinkedBlockingQueue<Lorry> emptyLorries, LinkedBlockingQueue<Lorry> readyLorries) {
        this.wNumber = wNumber;
        this.timePerX = timePerX;
        this.writer = writer;
        this.foreman = foreman;
        this.ferry = ferry;
        this.semaphore = semaphore;
        this.emptyLorries = emptyLorries;
        this.readyLorries = readyLorries;
    }

    /**
     * Run thread method for workers to mine blocks
     */
    @Override
    public void run() {
        while (!foreman.getBlocks().isEmpty()) {
            pickABlock();
        }
    }

    /**
     * Method that gives a worker a specific block to work with
     */
    public void pickABlock() {
        if (!foreman.getBlocks().isEmpty()) {
            String block = foreman.getBlocks().poll();
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
                int miningTime = (int) (timePerX * Math.random());
                sleep(miningTime);
                inventory += 1;
                logMiningEvent(wNumber, miningTime, writer);
                if(i == 1){
                    long temp2 = System.nanoTime();
                    long tBlockMining = (temp2 - temp1) / TO_MILLIS;
                    logBlockMinedEvent(wNumber, tBlockMining, writer);
                    logCarringEvent(wNumber, inventory, writer);

                    //put sources into Lorry
                    putIntoLorry(this.inventory);

                    inventorySumOfMined += this.inventory;
                    inventory = 0;
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
        while (wInventory > 0) {
            fullLorry();
            wInventory--;
        }
    }

    /**
     * Method which creates new lorry, if the current lorry is full,
     * and solves exception problems with filling
     */
    public void fullLorry() {
            try {
                //putting lasts 1 second
                semaphore.acquire();
                sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            emptyLorries.peek().setInventory(1);
            foreman.setSourcesPicked(foreman.getSourcesPicked()-1);
            logPutting(wNumber, 1, writer);

            if (emptyLorries.peek().getInventory() >= emptyLorries.peek().getMaxCapacity()) {

                //time to leave
                long end = System.nanoTime();
                long temp = (end - emptyLorries.peek().getStart()) / TO_MILLIS;

                //log it
                logFullEvent(emptyLorries.peek().getvNumber(), temp, writer);

                //add it to readyLorrys
                readyLorries.add(emptyLorries.peek());

                //and create new lorry to go
                Lorry lorry = new Lorry((emptyLorries.peek().getvNumber() + 1),emptyLorries.peek().getMaxCapacity(),
                                                      emptyLorries.peek().gettLorry(), writer, ferry, readyLorries, emptyLorries.peek().getBarrier());
                emptyLorries.add(lorry);
                Thread lorryThread = new Thread(emptyLorries.peek());
                lorryThread.start();
                emptyLorries.remove();
            }

            if (foreman.getSourcesPicked() == 0 && emptyLorries.peek().getInventory() != 0) {
                //miners mined all sources, time to send last lorry
                //time to leave
                long end = System.nanoTime();
                long temp = (end - emptyLorries.peek().getStart()) / TO_MILLIS;

                //add it to readyLorrys
                Thread lorryThread = new Thread(emptyLorries.peek());
                lorryThread.start();
                readyLorries.add(emptyLorries.peek());

                //log it
                logFullEvent(emptyLorries.peek().getvNumber(), temp, writer);
                emptyLorries.remove();
            }
            semaphore.release();
    }

    /**
     * Mining log which sets up output for output file
     * @param workerNumber Thread number of worker
     * @param miningTime How long it took the worker to mine a source
     */
    private void logMiningEvent(int workerNumber, int miningTime, BufferedWriter writer) {
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format("%s - Dělník %d vytěžil zdroj, trvalo mu to %d ms.\n", timeStamp, workerNumber, miningTime);
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
     * Log message telling that worker is bringing sources to Lorry
     * @param workerNumber (int) number of Thread
     * @param inventoryCount (int) how many sources is he bringing
     */
    public void logCarringEvent(int workerNumber, int inventoryCount, BufferedWriter writer){
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format(timeStamp + " - Dělník " + workerNumber + " nese " + inventoryCount + " zdrojů.\n");
        writeToLogFile(logMessage, writer);
    }

    /**
     * Log message telling that worker is bringing sources to Lorry
     * @param workerNumber (int) number of Thread
     * @param inventoryCount (int) how many sources is he bringing
     */
    public void logPutting(int workerNumber, int inventoryCount, BufferedWriter writer){
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format(timeStamp + " - Dělník " + workerNumber + " nakládá " + inventoryCount +
                                         " zdrojů do Náklaďáku: "+ emptyLorries.peek().getvNumber() + "\n");
        writeToLogFile(logMessage, writer);
    }

    /**
     * Log which sets up output for a mined block
     * @param workerNumber (int) Thread number which completed a block
     * @param time (long) how long did it take in milliseconds
     */
    public void logBlockMinedEvent(int workerNumber, long time, BufferedWriter writer){
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format(timeStamp + " - Dělník " + workerNumber + " vytěžil celý blok, trvalo mu to " + time + " ms.\n");
        writeToLogFile(logMessage, writer);
    }

    /**
     * log outputing situation when lorry is full
     * @param vNumber Thread number of lorry
     * @param time How long it took to workers to full that lorry
     */
    private void logFullEvent(int vNumber, long time, BufferedWriter writer) {
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format("%s - Náklaďák %d je připraven vyrazit, naplnit ho trvalo přibližně " + time + " ms.\n", timeStamp, vNumber);
        writeToLogFile(logMessage, writer);
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
}
