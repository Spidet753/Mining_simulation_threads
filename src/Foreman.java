import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class that works with Foreman
 * @author Václav Prokop
 */
public class Foreman implements Runnable{

    //== Private attributes
    private String file;
    private static LinkedBlockingQueue<String> blocks;
    private static int countOfsource = 0;

    /**
     * Constructor
     * @param file input file
     */
    public Foreman(String file){
        this.file = file;
    }

    /**
     * Run method for thread, that will go through
     * the file and analyze it for blocks and sources
     */
    public void run(){

        File file1 = new File(file);
        blocks = new LinkedBlockingQueue<>();

        try {
            Scanner sc = new Scanner(file1);

            while(sc.hasNext()) {
                String temp = sc.next();
                blocks.add(temp);
                countOfsource += temp.length();

            }

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
    public static LinkedBlockingQueue<String> getBlocks() {
        return blocks;
    }
}
