import org.apache.commons.cli.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Main class
 * @author Václav Prokop
 */
public class Main {

    //== Private attributes
    private static int numberOfWorkers;
    private static int timePerWorker;
    private static int capacityOfLorry;
    private static int capacityOfFerry;
    private static int timeOfLorry;
    private static String inputFile;
    private static String outputFile;
    private static Worker[] workers;
    private static ThreadGroup workerThreadGroup;

    //== Public attributes
    public static BufferedWriter writer;
    public static ThreadGroup lorryThreadGroup;

    public static LinkedBlockingQueue<Lorry> emptyLorrys;
    public static LinkedBlockingQueue<Lorry> readyLorrys;

    public static LinkedBlockingQueue<Lorry> drivingLorrys;

    /**
     * Main method of the program
     * @param args arguments that are needed for the application to run
     * @throws IOException while writing to output file
     */
    public static void main(String[] args) throws IOException {
        //group to know if all the threads are dead or alive
        workerThreadGroup = new ThreadGroup("workers");
        loadInput(args);
        writer = new BufferedWriter(new FileWriter(outputFile, false));

        //creating a foreman thread that will go through the input file
        Foreman foreman = new Foreman(inputFile);
        Thread foremanThread = new Thread(foreman);
        foremanThread.start();

        //wait for foreman to search for blocks
        try {
            foremanThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //creating worker Threads
        workers = new Worker[numberOfWorkers];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker((i+1), timePerWorker);
            Thread workerThread = new Thread(workerThreadGroup, workers[i]);
            workerThread.start();
        }

        //first lorry instance
        emptyLorrys = new LinkedBlockingQueue<>();
        readyLorrys = new LinkedBlockingQueue<>();
        drivingLorrys = new LinkedBlockingQueue<>();
        Lorry lorry = new Lorry(capacityOfLorry, timeOfLorry);
        emptyLorrys.add(lorry);
        Thread lorryThread = new Thread(lorryThreadGroup, lorry);
        lorryThread.start();

        //we don't want to end the Main thread until other threads are done
        while (workerThreadGroup.activeCount() > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        //end of Main method, writing out important stats into console
        writer.write("///////////////////////////////\n");
        writer.write("\npočet vytěžených zdrojů: " + Worker.getInventorySum()+".\n");
        for (int i = 0; i <workers.length; i++){
            System.out.println("Dělník " + workers[i].getwNumber() +" vytěžil "+ workers[i].getInventorySumOfMined() + " zdrojů.\n");
        }
        writer.close();
        System.out.println("Program has ended.");
    }

    /**
     * Method which allows to run the Jar file in required state
     * @param args arguments that are needed for the application to run
     */
    public static void loadInput(String[] args){

        Options options = new Options();

        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output file path");
        output.setRequired(true);
        options.addOption(output);

        Option capacityWorker = new Option("cWorker", true, "capacity of worker");
        capacityWorker.setRequired(true);
        options.addOption(capacityWorker);

        Option timeWorker = new Option("tWorker", true, "time of worker");
        timeWorker.setRequired(true);
        options.addOption(timeWorker);

        Option capacityLorry = new Option("capLorry", true, "capacity of lorry");
        capacityLorry.setRequired(true);
        options.addOption(capacityLorry);

        Option timeLorry = new Option("tLorry", true, "time of lorry");
        timeLorry.setRequired(true);
        options.addOption(timeLorry);

        Option capacityFerry = new Option("capFerry", true, "capacity of ferry");
        capacityFerry.setRequired(true);
        options.addOption(capacityFerry);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            inputFile = cmd.getOptionValue("input");
            outputFile = cmd.getOptionValue("output");
            numberOfWorkers = Integer.parseInt(cmd.getOptionValue("cWorker"));
            timePerWorker = Integer.parseInt(cmd.getOptionValue("tWorker"));
            capacityOfLorry = Integer.parseInt(cmd.getOptionValue("capLorry"));
            timeOfLorry = Integer.parseInt(cmd.getOptionValue("tLorry"));
            capacityOfFerry = Integer.parseInt(cmd.getOptionValue("capFerry"));
            System.out.println(
                             "---------------------------"+
                             "\nvstupní soubor: " +inputFile+
                             "\nvýstupní soubor: " + outputFile+
                             "\npočet pracovníků: " +numberOfWorkers+
                             "\nmaximální čas práce jednoho pracovníka: " + timePerWorker+ " ms" +
                             "\nkapacita náklaďáku: " +capacityOfLorry+ " ks" +
                             "\nmaximální čas jízdy náklaďáku: " + timeOfLorry+ " ms" +
                             "\nkapacita přívozu: " + capacityOfFerry + " ks"
                            );
        } catch (ParseException e) {
            System.out.println("Wrong amount of arguments, program is ending.");
            System.exit(1);
            throw new RuntimeException(e);
        }
    }

    /**
     * Getter of group of workers thread
     * @return ThreadGroup of workers
     */
    public static ThreadGroup getWorkerThreadGroup() {
        return workerThreadGroup;
    }

    /**
     * Getter of workers
     * @return array of workers
     */
    public static Worker[] getWorkers() {
        return workers;
    }

    /**
     * Getter of lorrys, that are not full
     * @return LinkedBlockingQueue (for consistency)
     */
    public static LinkedBlockingQueue<Lorry> getEmptyLorrys() {
        return emptyLorrys;
    }

    /**
     * Getter of lorrys, that are full and ready to go
     * @return LinkedBlockingQueue (for consistency)
     */
    public static LinkedBlockingQueue<Lorry> getReadyLorrys() {
        return readyLorrys;
    }

    /**
     * Getter of maximum capacity of lorry
     * @return (int) capacity
     */
    public static int getCapacityOfLorry() {
        return capacityOfLorry;
    }

    /**
     * Getter of lorry's time drive to ferries
     * @return (int) time
     */
    public static int getTimeOfLorry() {
        return timeOfLorry;
    }
}