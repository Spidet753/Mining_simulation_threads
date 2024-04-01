import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

public class Foreman implements Runnable{
    public String file;
    public static LinkedBlockingQueue<String> blocks;
    public int countOfsource = 0;

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
}
