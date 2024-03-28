import org.apache.commons.cli.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Thread.sleep;

public class Main {
    public static int numberOfWorkers;
    public static int timePerWorker;
    public static int capacityOfLorry;
    public static int capacityOfFerry;
    public static int timeOfLorry;
    public static String inputFile;
    public static String outputFile;

        public static void main(String[] args) throws InterruptedException {
            loadInput(args);
            Foreman foreman = new Foreman(inputFile);
            Thread foremanThread = new Thread(foreman);
            foremanThread.start();

            //wait for foreman to search for blocks
            try {
                foremanThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Worker[] workers = new Worker[numberOfWorkers];
            for(int i = 0; i < workers.length; i++){
                workers[i] = new Worker(i, timePerWorker);
                Thread workerThread = new Thread(workers[i]);
                workerThread.setName("Dělník "+ (i+1));
                workerThread.start();
            }



            System.out.println("Program has ended.");
        }

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

//                    try {
//                        FileWriter myWriter = new FileWriter(outputFile);
//                        String timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.ms").format(new Date().getTime());
//                        myWriter.write(timeStamp);
//                        myWriter.close();
//                        System.out.println("Successfully wrote to the file.");
//                    } catch (IOException e) {
//                        System.out.println("An error occurred.");
//                        e.printStackTrace();
//                    }
        }
}