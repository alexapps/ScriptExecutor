package ua.alexapps.helper.Util;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import java.io.IOException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;


/**
 * Created by OIvanchenko on 10.07.2017.
 */
public class ScripExecutor {
    public static Integer concurrentRunScript(final String command, ExecutorService executor) throws InterruptedException, ExecutionException {

        return (Integer) executor.submit(new Callable() {
            public Integer call() throws Exception {
                return singleRunScript(command);
            }
        }).get();
    }


    public static Integer singleRunScript(String command) {
        final String sCommandString = command;
        int exitCode = 0;
        CommandLine oCmdLine = CommandLine.parse(sCommandString);
        DefaultExecutor oDefaultExecutor = new DefaultExecutor();
        oDefaultExecutor.setExitValue(0);
        try {
            exitCode = oDefaultExecutor.execute(oCmdLine);
        } catch (ExecuteException e) {
            System.err.println("Execution failed.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("permission denied.");
            e.printStackTrace();
        }
        return exitCode;
    }
}
