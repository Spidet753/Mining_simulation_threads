import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Foreman implements Runnable{
    public String file;
    public ArrayList<String> blocks;
    public int countOfsource = 0;
    public Foreman(String file){
        this.file = file;
    }

    public void run(){
        File file1 = new File(file);
        blocks = new ArrayList<>();
        try {
            Scanner sc = new Scanner(file1);
            int temp = 0;
            while(sc.hasNext()) {
                blocks.add(sc.next());
                String Xs = blocks.get(temp);
                countOfsource += Xs.length();
                temp++;
            }

            System.out.println("předák našel: " + (blocks.size()) + " bloků" +
                               "\npředák našel: " + (countOfsource) + " zdrojů" +
                               "\n---------------------------"
                               );

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
