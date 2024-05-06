import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class that works with Foreman
 * @author Václav Prokop
 */
public class Foreman implements Runnable{

    //== Private attributes
    /**
     * input file, that foreman has to go through
     */
    private final String file;
    /**
     * blocks, that foreman found in input file
     */
    private LinkedBlockingQueue<String> blocks;
    /**
     * all sources, that foreman found
     */
    private static int countOfsource = 0;
    /**
     * Date format used with milliseconds used in the output file
     */
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
    /**
     * Writer used to output lines
     */
    private final BufferedWriter writer;

    /**
     * Constructor
     * @param file input file
     */
    public Foreman(String file, BufferedWriter writer){

        this.file = file;
        this.writer = writer;
    }

    /**
     * Run method for thread, that will go through
     * the file and analyze it for blocks and sources
     */
    public void run(){
        long start = System.nanoTime();
        File file1 = new File(file);
        blocks = new LinkedBlockingQueue<>();

        try {
            Scanner sc = new Scanner(file1);

            while(sc.hasNext()) {
                String temp = sc.next();
                blocks.add(temp);
                countOfsource += temp.length();

            }
            //log found sources and blocks, and time how long it took
            long end = System.nanoTime();
            long time = (end - start) / 1000000;
            logFoundSources(time);
            logFoundblocks(time);

            System.out.println("předák našel: " + (blocks.size()) + " bloků" +
                               "\npředák našel: " + (countOfsource) + " zdrojů" +
                               "\n---------------------------" +
                               "\nProgram is working...\n"
                               );

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method that returns all found sources by foreman
     * @return (int) sum of sources
     */
    public static int getCountOfsource() {
        return countOfsource;
    }

    /**
     * Getter of block found by Foreman
     * @return block containing sources
     */
    public LinkedBlockingQueue<String> getBlocks() {
        return blocks;
    }

    /**
     * log which sets up output for output file
     * @param time How long it took the foreman to find all sources
     */
    private synchronized void logFoundSources(long time) {
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format("%s - Předák našel %d zdrojů, trvalo mu to "+ time +" ms.\n", timeStamp, countOfsource);
        writeToLogFile(logMessage);
    }

    /**
     * log which sets up output for output file
     * @param time How long it took the foreman to find all blocks
     */
    private synchronized void logFoundblocks(long time) {
        String timeStamp = dateFormatter.format(new Date());
        String logMessage = String.format("%s - Předák našel %d bloků, trvalo mu to "+ time +" ms.\n", timeStamp, blocks.size());
        writeToLogFile(logMessage);
    }

    /**
     * Tells the bufferedWriter from Main to write down a message
     * @param logMessage message to write
     */
    private void writeToLogFile(String logMessage) {
        try  {
            writer.write(logMessage);
        } catch (IOException e) {
            throw new RuntimeException("Chyba při zápisu do souboru.", e);
        }
    }
}
