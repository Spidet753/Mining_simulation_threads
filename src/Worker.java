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

    public Worker(int wNumber, int timePerX) {
        this.wNumber = wNumber;
        this.timePerX = timePerX;
    }

    @Override
    public void run() {
        while (!Foreman.blocks.isEmpty()) {
            pickABlock();
        }
        synchronized (Worker.class) { // Synchronizace pro kritickou sekci
            inventorySum += inventorySumOfMined;
        }
    }
    public synchronized void pickABlock() {
        // Synchronizace pro přístup k sdílenému seznamu bloků
        if (!Foreman.blocks.isEmpty()) {
            String block = Foreman.blocks.poll();
            if (block != null) {
                notify();
                mine(block);
            }
        }
    }

    private void mine(String block) {
        notifyAll();
        int blockLength = block.length();
        for (int i = blockLength; i > 0; i--) {
            try {
                //TODO CHANGE MININGTIME
                miningTime = (int)(2 * Math.random());
                sleep(miningTime);
                this.inventory += 1;
                logMiningEvent(wNumber, miningTime);
                if(i == 1){
                    logCarringEvent(wNumber, inventory);
                    this.inventorySumOfMined += this.inventory;
                    this.inventory = 0;
                    break;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private synchronized void logMiningEvent(int workerNumber, int miningTime) {
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format("%s - Dělník %d vytěžil zdroj, trvalo mu to %d ms.\n", timeStamp, workerNumber, miningTime);
        writeToLogFile(logMessage);
    }

    private static synchronized void writeToLogFile(String logMessage) {
        try  {
            Main.writer.write(logMessage);
        } catch (IOException e) {
            throw new RuntimeException("Chyba při zápisu do souboru.", e);
        }
    }

    public synchronized void logCarringEvent(int workerNumber, int inventoryCount){
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format(timeStamp + " - Dělník " + workerNumber + " nese " + inventoryCount + " zdrojů.\n");
        writeToLogFile(logMessage);
    }

    public int getwNumber() {
        return wNumber;
    }

    public int getInventory() {
        return inventory;
    }

    public int getInventorySumOfMined() {
        return inventorySumOfMined;
    }

    public static int getInventorySum() {
        return inventorySum;
    }
}
