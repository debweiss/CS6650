package cs6650App.app.src.main.java;


import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;

/** Generates threads that make Get and Post requests,
 * based on inputs for maximum # of threads, server location,
 * and number of iterations/times the thread should invoke
 * the requests.
 *
 * There are 4 phases:
 *  -Warmup, which generates 10% of the max threads input
 *  -Loading: Creates 50% of the max threads input
 *  -Peak: Creates 100% of the max threads input
 *  -Cool Down: Creates 25% of the max threads input
 *
 * Statistics are gathered on how much time it takes for all the threads to complete,
 * number of requests,throughput, mean/median/99th/95th percentile latency of the requests
 */
class SimpleGetPostClient {

    private static CountDownLatch countDownLatchStartTotal; // wait until all threads are started
    private static CountDownLatch countDownLatchFinishTotal; // wait until all threads are finished
    private static ArrayList<String> phaseNames = new ArrayList<>(); // list of phases used in the simulation
    private static String firstPhase; // first phase of the simulation
    private static String lastPhase; // last phase of the simulation

    /* Getters and Setters ****************************************/

    public static CountDownLatch getCountDownLatchStartTotal() {

        return countDownLatchStartTotal;
    }

    public static void setCountDownLatchStartTotal
        (CountDownLatch countDownLatchStartTotal) {

        SimpleGetPostClient.countDownLatchStartTotal
           = countDownLatchStartTotal;
    }

    public static CountDownLatch getCountDownLatchFinishTotal() {

        return countDownLatchFinishTotal;
    }

    private static void setCountDownLatchFinishTotal
        (CountDownLatch countDownLatchFinishTotal) {

        SimpleGetPostClient.countDownLatchFinishTotal
           = countDownLatchFinishTotal;
    }

    //***************************************************************//

    /** Generates a list of the phases that are used in this simulation
     * @param phaseNames an empty, non-null list to hold the names
     * @return an arraylist that contains the 4 phases of the simulation
     */
    private static ArrayList<String> createPhaseNames(@NotNull ArrayList<String> phaseNames) {

        phaseNames.add("Warmup Phase");
        phaseNames.add("Loading Phase");
        phaseNames.add("Peak Phase");
        phaseNames.add("Cooldown Phase");

        firstPhase = phaseNames.get(0);
        lastPhase = phaseNames.get(phaseNames.size() - 1); // set last phase based on size of list

        return phaseNames;
    }

    /** Based on the name of the phase, determines how many
     * threads should be generated:
     * -Warmup Phase: 10% of max threads
     * -Loading Phase: 50% of max threads
     * -Peak phase: 100% of max threads
     * -Cooldown phase: 25% of max threads
     *
     * @param phase name of the simulation phase
     * @param maxThreads user input on the max # of threads the simulation can generate
     * @return number of threads that should be generated for the phase
     */
    private static int calculatePhaseThreadNum(String phase, int maxThreads) {

        String phaseLower = phase.toLowerCase();

        int phaseThreads = maxThreads;

        switch (phaseLower) {

            case "warmup phase":
                phaseThreads = maxThreads / 10;
                break;
            case "loading phase":
                phaseThreads = maxThreads / 2;
                break;
            case "peak phase":
                break;
            case "cooldown phase":
                phaseThreads = maxThreads / 4;
                break;
        }

        return phaseThreads;
    }

    /** Calculates the amount of threads to generate based on the specific phase,
     * kicks off each phase, and waits for threads to finish for a given phase
     * @param phaseNames list of phases that the simulation runs through, can't be null
     * @param maxThreads max amt of threads, from which amt of threads for each phase is calculated
     * @param numIterations the number of times each thread should call its Get and Post methods
     * @param ipAddress the ip address of the server to connect to
     * @param serverPort the port of the server to connect to
     */
    private static void launchPhaseThenAwaitThreadStartAndFinish
    (@NotNull ArrayList<String> phaseNames, int maxThreads, int numIterations,
     String ipAddress, String serverPort) {

        for (String phase : phaseNames) { // for each phase

            int phaseThreads = calculatePhaseThreadNum(phase, maxThreads); // calc # of threads needed

            /* Start a count down until all threads are started,
             * and a count down until all threads are finished */
            CountDownLatch countDownLatchStart = new CountDownLatch(phaseThreads);
            CountDownLatch countDownLatchFinish = new CountDownLatch(phaseThreads);

            setCountDownLatchFinishTotal(countDownLatchFinish);

            submitLoadTesterTasksForPhase(phase, phaseThreads, numIterations, // generate threads based on inputs
                    ipAddress, serverPort, countDownLatchStart, countDownLatchFinish);

            long startTime = awaitStart(countDownLatchStart, phase); // get the time when all threads have started

            if(phase.equals(firstPhase)) { // if we're in the first phase
                StatisticsGatherer.updateAllThreadsStartTime(startTime); // start time == start time for all threads
            }

            awaitFinish(countDownLatchFinish, phase, startTime); // wait for threads to finish

        }
    }

    /** Creates and starts the threads for a given phase with all the proper inputs
     * @param phase name of the simulation phase
     * @param phaseThreads number of threads that phase should generate
     * @param numIterations number of times each thread should call its methods
     * @param ipAddress address of the server to be connected to
     * @param serverPort port of the server to be connected to
     * @param countDownLatchStart based on phase threads, determine when all threads have started
     * @param countDownLatchFinish based on phase threads, determine when all threads have finished
     */
    private static void submitLoadTesterTasksForPhase(String phase, int phaseThreads,
         int numIterations, String ipAddress, String serverPort,
         CountDownLatch countDownLatchStart, CountDownLatch countDownLatchFinish) {

        for(int i = 0; i < phaseThreads; i++) {  // for each thread of this phase

            GetPostThread getPostThread = new GetPostThread(numIterations, ipAddress,
               serverPort, countDownLatchStart, countDownLatchFinish); // generate a new thread with these inputs

            getPostThread.start(); // start the thread
            countDownLatchStart.countDown(); // set the countdown
        }
    }

    /** Based on a phase and a count down, wait for all the threads to start
     * for that phase, print notification to console, and return the time
     * when all the threads for the phase have started.
     * @param countDownLatchStart the count down to when all the threads have started
     * @param phase name of the phase that the simulation is in
     * @return the time when all the threads have started
     */
    private static long awaitStart(CountDownLatch countDownLatchStart, String phase) {

        long startTime = 0;

        try {
            countDownLatchStart.await(); // wait for all threads to start
            startTime = System.nanoTime(); // when done, set start time to current sys time
            System.out.println(phase + ": All threads are running "); // print notification to console

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return startTime;
    }

    /** Based on a phase and a countdown, wait for all the tasks to finish,
     * update finish time for the phase. If the phase is the last phase in the
     * simulation, also update the time when all the threads have finished.
     * @param countDownLatchFinish count down to when all threads are finished
     * @param phase name of the phase
     * @param startTime when all threads have started for the phase
     */
    private static void awaitFinish(CountDownLatch countDownLatchFinish,
         String phase, long startTime) {

        long endTime = 0L;
        long duration = 0L;

        try {
            countDownLatchFinish.await(); // wait for all threads to finish
            endTime = System.nanoTime(); // set finish time for the current system time

            if(phase.equals(lastPhase)) { // if this phase is the last phase

                StatisticsGatherer.updateAllThreadsFinishTime(endTime); // end time == total finish time
                duration = endTime - startTime; // calc duration of this phase

                System.out.println(phase + " complete: Time "  // convert to secs and print to console
                  + (duration/(float)1000000000) + " seconds");
                System.out.println("-------------------------------------------------");

                StatisticsGatherer.updateTotalWallTime(StatisticsGatherer
                   .calcTotalWallTimeInSeconds()); // calc total wall time (based on updated finish time)
            }

            else { // if not the last phase

                duration = endTime - startTime; // just determine the duration of the current phase
                System.out.println(phase + " complete: " +    // convert to secs and print to console
                   "Time " + duration/(float)1000000000 + " seconds");
                System.out.println("-------------------------------------------------");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws InterruptedException {

        /* cmd line args:
         * -max number of threads (default 50)
         * -number of iterations per thread (default to 100)
         * -IP address of server
         * -port used on server (default to 8080)
         */

        int maxThreads;
        int numIterations;
        String ipAddress;
        String serverPort;

        LocalDateTime clientStartTime = LocalDateTime.now(); // client starts now
        System.out.println("Client starting... Time: " + clientStartTime);
        System.out.println("Active thread count " + Thread.activeCount());


        if (args.length == 0) { // if there are no command line inputs

            maxThreads = 100; // set the defaults
            numIterations = 100;
            ipAddress = "localhost";
            serverPort = "8080";


        } else { // bring in the command line inputs

            maxThreads = Integer.parseInt(args[0]);
            numIterations = Integer.parseInt(args[1]);
            ipAddress = args[2];
            serverPort = args[3];
        }

        System.out.println("Testing " + maxThreads + " maximum threads " + numIterations + " iterations...");
        System.out.println(" ");

        phaseNames = createPhaseNames(phaseNames); // create the list of phases

        /* Launch the threads in this phase based on inputs then kickoff
         * await start and await finish */
        launchPhaseThenAwaitThreadStartAndFinish(phaseNames, maxThreads,
           numIterations,ipAddress, serverPort);

        StatisticsGatherer.printStatistics(); // threads are finished, print stats to console

        System.exit(0);


    }
}
