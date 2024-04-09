package hse.manager;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import hse.cli.commands.AbstractCommand;

/**
 * Manager maintains the orchestration of different commands in the current pipeline
 * Also manager redirects the pipes from one command to another
 */
public class Manager {
    private static ThreadPoolExecutor executor;

    public static void shutDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    /**
     * Starts the thread pool
     */
    public static void startThreadPool() {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
    }

    /**
     * Starts the execution of the pipeline in ThreadPool
     * Every command is being executed in separated thread
     * Threads communicate via PipeStream redirections
     *
     * @param commands - list of commands to execute in the pipeline
     * @return resulting InputStream
     */
    public static InputStream startPipeline(List<AbstractCommand> commands) throws IOException {
        PipedInputStream finalStream = new PipedInputStream();
        PipedOutputStream prevOutput = new PipedOutputStream();
        for (Iterator<AbstractCommand> iter = commands.iterator();;) {
            AbstractCommand command = iter.next();

            PipedOutputStream output = new PipedOutputStream();
            PipedInputStream input = new PipedInputStream(prevOutput);

            command.setInputStream(input);
            command.setOutputStream(output);
            prevOutput = output;

            executor.execute(command);

            if (!iter.hasNext()) {
                prevOutput.connect(finalStream);
                return finalStream;
            }
        }
    }
}
