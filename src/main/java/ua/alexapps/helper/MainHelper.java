package ua.alexapps.helper;

import ua.alexapps.helper.Util.ScripExecutor;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


;

/**
 * Created by OIvanchenko on 10.07.2017.
 */
public class MainHelper {
    private static ExecutorService executor = null;

    public static void main(String[] args) {
        // Get the properties
        Properties prop = getProperties();





        executor = Executors.newFixedThreadPool(Integer.parseInt(prop.getProperty("threads")));

        // Read input script name and path for scenarios
        // host value
        final String host = args[0];
        // script name/path
        final String scriptName = args[1];
        // scenario path
        final String scenarioPath = args[2];


        double concurrentThreadSum = 0.;
        Integer runsCount = Integer.parseInt(prop.getProperty("runsCount"));
        try {
            ArrayList<String> commandsPool = new ArrayList<String>();
            for (int i = 1; i <= runsCount; i++) {

                List<String> fileNames = listFilesMatching(new File(scenarioPath), "^" + i + "_.*.dat$");
                int iterationSize = fileNames.size();

                String command = scriptName + " " + host + " ";
                //String command ="/usr/bin/dhcpperf --clients 1 -s "+ host +" --test-duration 1 --test-mode \"once\" --nthreads 1 eth1 ";
                switch (iterationSize) {
                    case 0: System.exit(1);
                    case 2:
                        command = command +
                                         scenarioPath + "" + getStringFromListByRegex(fileNames, ".*discover.dat$") + " " +
                                         scenarioPath + "" + getStringFromListByRegex(fileNames, ".*request.dat$") ;
                        break;
                    case 3:
                        command = command +
                                scenarioPath + "" + getStringFromListByRegex(fileNames, ".*discover.dat$") + " " +
                                scenarioPath + "" + getStringFromListByRegex(fileNames, ".*request.dat$")  + " " +
                                scenarioPath + "" + getStringFromListByRegex(fileNames, ".*release.dat$");
                        break;
                    default:
                        System.out.println("Check the scenario dir");
                }
                //System.out.println(command);

                commandsPool.add(command);

            }
            long start = System.currentTimeMillis();
            for (String item : commandsPool) {
                ScripExecutor.concurrentRunScript(item, executor);
            }
            double duration = (System.currentTimeMillis() - start) / 1000.;
            out("Execution time, sec: %.3f", duration);
            concurrentThreadSum += duration;
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        finally {
            executor.shutdown();
            out("Average  thread time, sec: %.3f", concurrentThreadSum / runsCount);
        }




/*
int reqNumber = 0;
        // Get List of scenaro list files
        try {
            List<String> names = listFilesMatching(new File(scenarioPath), "[0-9]+_(discover.dat|request.dat)");
            System.out.println("Total scenario files " +  names.size());
            reqNumber = names.size()/2;
            // Sort list

            Collections.sort(names, new Comparator<String>() {
                public int compare(String o1, String o2) {
                    String[] o1_values = o1.split("_");
                    String[] o2_values = o2.split("_");

                    Integer o1_number = Integer.parseInt(o1_values[0]);
                    Integer o2_number = Integer.parseInt(o2_values[0]);

                    return o1_number.compareTo(o2_number);
                }
            });
            // Go through of sorted pairs

            long start = System.currentTimeMillis();
            for (int i = 0; i < names.size(); i+=2) {
                //System.out.println(names.get(i) + " ; " + names.get(i+1) );
                //
                String command = scriptName + " " + host + " " + scenarioPath + "" +  names.get(i) + " " + scenarioPath + "" + names.get(i+1);
                System.out.println(ScripExecutor.concurrentRunScript(command, executor));
            }
            double duration = (System.currentTimeMillis() - start) / 1000.;
            out("Execution time, sec: %.3f", duration);
            concurrentThreadSum += duration;
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        finally {
            executor.shutdown();
            out("Average  thread time, sec: %.3f", concurrentThreadSum / reqNumber);
        }


        //

        */
    }


    /***
     * Function returns list of file names from root dir that matches to regex
     */
    public static List<String> listFilesMatching(File root, String regex) throws NullPointerException {
        List<String> returnValue = new ArrayList<String>();
        File[]  listOfFiles = root.listFiles();
        Pattern uName = Pattern.compile(regex);

        for (File file: listOfFiles) {
            String fileName = file.getName();
            Matcher mUname = uName.matcher(fileName);
            if (mUname.matches()) {
                returnValue.add(fileName);
            }
        }
        return returnValue;
    }

    private static void out(String format, double ms) {
        System.out.println(String.format(format, ms));
    }

    private static Properties getProperties() {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("config.properties");
            // load a properties file
            prop.load(input);
            System.out.println(prop.getProperty("runsCount"));

        }
        catch(IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return prop;
    }

    private static String getStringFromListByRegex(List<String> list, String regex) {
        String returnValue = "";
        Pattern pattern = Pattern.compile(regex);

        for (String item: list) {
            Matcher matcher = pattern.matcher(item);
            if (matcher.matches()) {
                returnValue = item;
            }
        }
        return returnValue;
    }

}
