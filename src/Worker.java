import java.util.Random;

public class Worker implements Runnable {
    public double timePerX;
    public int wNumber;
    public int inventory = 0;
    public static int inventorySum = 0;

    public Worker(int wNumber, double timePerX) {
        this.wNumber = wNumber;
        this.timePerX = timePerX;
    }

    @Override
    public void run() {
      String block = Foreman.blocks.getFirst();
      Foreman.blocks.removeFirst();
      int blockLength = block.length();
      for(int i = blockLength; i > 0; i--){
          try {
              Thread.sleep((int)(timePerX*Math.random()));
              inventorySum+=1;
              inventory+=1;

              //System.out.println("pridano x do inventare " + Thread.currentThread().getName());
          } catch (InterruptedException e) {
              throw new RuntimeException(e);
          }
      }
        System.out.println(inventory + "of " + Thread.currentThread().getName());
        System.out.println("pocet vsech:" + inventorySum);
    }
}
