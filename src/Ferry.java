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
        private volatile int inventory = 0;
        private static int FCount = 0;
        private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");

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
            long start = System.nanoTime();

            //ferry is driving

            //Ferry is at the end
            long end = System.nanoTime();
            long time = (end - start) / 1000000;
            logFerryArrival(vNumber, time);
            Thread.currentThread().interrupt();
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
         * Arrival log which sets up output for output file
         * @param LorryNumber Thread number of lorry
         * @param time How long it took the lorry to arrive at ferry
         */
        private synchronized void logFerryArrival(int LorryNumber, long time) {
            String timeStamp = dateFormatter.format(new Date());
            String logMessage = String.format("%s - Náklaďák %d dojel k trajektu, trvalo mu to %d ms.\n", timeStamp, LorryNumber, time);
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
