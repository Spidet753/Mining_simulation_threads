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

    /**
     * Max capacity of lorry from input file
     */
    private static int capacityOfLorry;
    /**
     * time to drive to ferry from input file
     */
    private static int timeOfLorry;

    /**
     * array of workers working in the simulation
     */
    private static Worker[] workers;

    //== Public attributes
    /**
     * Queue of empty lorries
     */
    public static LinkedBlockingQueue<Lorry> emptyLorrys;
    /**
     * Queue of full lorries
     */
    public static LinkedBlockingQueue<Lorry> readyLorrys;
    /**
     * Queue of ferries
     */
    public static LinkedBlockingQueue<Ferry> ferries;

    /**
     * Main method of the program
     * @param args arguments that are needed for the application to run
     * @throws IOException while writing to output file
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        //group to know if all the threads are dead or alive
        ThreadGroup workerThreadGroup = new ThreadGroup("workers");
        String inputFile;
        String outputFile;
        int numberOfWorkers;
        int timePerWorker;
        int capacityOfLorry1;
        int timeOfLorry1;
        int capacityOfFerry;

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
            capacityOfLorry1 = Integer.parseInt(cmd.getOptionValue("capLorry"));
            capacityOfLorry  = capacityOfLorry1;
            timeOfLorry1 = Integer.parseInt(cmd.getOptionValue("tLorry"));
            timeOfLorry = timeOfLorry1;
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

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, false));

        //creating a foreman thread that will go through the input file
        Foreman foreman = new Foreman(inputFile, writer);
        Thread foremanThread = new Thread(foreman);
        foremanThread.start();

        //wait for foreman to search for blocks
        try {
            foremanThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Ferry instance
        Ferry ferry = new Ferry(capacityOfFerry);
        ferry.start = System.nanoTime();
        ferries = new LinkedBlockingQueue<>();
        ferries.add(ferry);
        Thread ferryThread = new Thread(ferry);
        ferryThread.start();

        //creating worker Threads
        workers = new Worker[numberOfWorkers];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker((i+1), timePerWorker, writer);
            Thread workerThread = new Thread(workerThreadGroup, workers[i]);
            workerThread.start();
        }

        //first lorry instance
        emptyLorrys = new LinkedBlockingQueue<>();
        readyLorrys = new LinkedBlockingQueue<>();
        Lorry lorry = new Lorry(capacityOfLorry, timeOfLorry, writer);
        emptyLorrys.add(lorry);

        //we don't want to end the Main thread until other threads are done
        while (workerThreadGroup.activeCount() > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        ferryThread.join();
        //end of Main method, writing out important stats into console
        writer.write("///////////////////////////////\n");
        writer.write("\nPočet vytěžených zdrojů: " + Worker.getInventorySum()+".\n");
        for (int i = 0; i <workers.length; i++){
            System.out.println("Dělník " + workers[i].getwNumber() +" vytěžil "+ workers[i].getInventorySumOfMined() + " zdrojů.\n");
        }
        System.out.println("Celkový počet zdrojů dovezených do cíle: " + ferries.peek().getTrasferedSources()+ "\n");
        writer.close();
        System.out.println("Program is ending.");
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