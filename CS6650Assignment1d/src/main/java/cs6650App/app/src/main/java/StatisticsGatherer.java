package cs6650App.app.src.main.java;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.ws.rs.core.Response;


/* Statistics Gatherer gets updated statistics on number of requests and latency
 * from each thread. It then calculates mean, median, and 99/95 percentile for
 * latency, overall throughput, total wall time.
 */
class StatisticsGatherer {

    private static AtomicInteger threadRequests = new AtomicInteger();
    private static AtomicInteger threadSuccesses = new AtomicInteger(0);
    private static AtomicInteger threadFailures = new AtomicInteger(0);
    private static AtomicLong allThreadsStartTime = new AtomicLong(0L);
    private static AtomicLong allThreadsFinishTime = new AtomicLong(0L);
    private static float totalWallTime = 0;
    private static CopyOnWriteArrayList<Float> latenciesList = new CopyOnWriteArrayList<>();

    /*========================GETTERS & SETTERS ====================================*/

    static void updateRequestStatistics() {

        threadRequests.getAndIncrement();
    }

    private static int getThreadRequests() {

        return threadRequests.get();
    }

    private static void updateSuccessStatistics() {

        threadSuccesses.getAndIncrement();
    }

    private static void updateFailStatistics() {

        threadFailures.getAndIncrement();
    }

    private static int getThreadSuccesses() {

        return threadSuccesses.get();
    }

    private static int getThreadFailures() {

        return threadFailures.get();
    }

    static void updateAllThreadsStartTime(long time) {

        allThreadsStartTime.getAndSet(time);
    }

    private static long getAllThreadsStartTime() {

        return allThreadsStartTime.get();
    }

    static void updateAllThreadsFinishTime(long time) {

        allThreadsFinishTime.getAndSet(time);
    }

    private static long getAllThreadsFinishTime() {

        return allThreadsFinishTime.get();
    }

    static void updateLatenciesList(float time) {

        latenciesList.add(time);
    }

    private static CopyOnWriteArrayList<Float> getLatenciesList() {

        return latenciesList;
    }

    static void updateTotalWallTime(float time) {

        totalWallTime = time;
    }

    private static float getTotalWallTime() {

        return totalWallTime;
    }

    static void updateResponseSuccessOrFailure(Response response) {

        if (response.getStatus() == 200) {

            updateSuccessStatistics();

        } else {

            updateFailStatistics();
        }
    }
    /*=================================================================================*/

    private static synchronized void sortLatenciesList() {

        getLatenciesList().sort(Comparator.naturalOrder());

    }
    /*=================================== CALCULATIONS ==================================*/

    /** Sum the list of latency measurements for use in calculating the mean latency
     * @param latenciesList list of latency measurements
     * @return the sum of all the latency measurements
     */
    private static float sumLatenciesList(CopyOnWriteArrayList<Float> latenciesList) {

        float sum = 0f;

        for (float latency : latenciesList) {
            sum += latency;
        }
        return sum;
    }

    /** Calculates the mean latency of the simulation's Get and Post requests
     * @return the mean latency
     */
    private static float calcMeanLatency() {

        int size = getLatenciesList().size();
        return sumLatenciesList(getLatenciesList()) / size;
    }

    /** Calculate the total wall time and convert to seconds
     * @return total wall time in seconds
     */
    static float calcTotalWallTimeInSeconds() {

        return ((getAllThreadsFinishTime() - getAllThreadsStartTime())
           / (float) 1000000000);

    }


    /** Calculate the median latency of the Get and Post requests
     * @return median latency
     */
    private static float calcMedianLatency() {

        sortLatenciesList(); // sort the list so the median can be calculated

        int latencyListSize = getLatenciesList().size();

        if (latencyListSize % 2 == 0) {  // if the size of the list is even

            return getLatenciesList().get(latencyListSize / 2); // the median is at size/2 index

        } else { // otherwise the median is the element in the middle of the two equal halves

            return getLatenciesList().get((latencyListSize - 1) / 2)
               + (getLatenciesList().get(latencyListSize / 2)) / 2;
        }

    }

    /** Calculates the percentile of latencies (e.g., 'X% of the latency data points are < this number')
     * @param percentile what percentile to calculate (e.g., .90 for 90th percentile)
     * @return the latency data point that is the point which X% are less than that data point*/
    private static float calcPercentile(double percentile) {

        sortLatenciesList(); //sort the list in ascending order

        int index = (int) (percentile * getLatenciesList().size()); // the index of the number is pct * list size

        return getLatenciesList().get(index); // return the number at that index
    }


    /** When all the threads are finished (after await finish), prints out all the
     * statistics for the simulation.
     * @throws InterruptedException if thread is interrupted
     */
    static void printStatistics() throws InterruptedException {

        System.out.println("==============================================================");
        System.out.println("Total number of requests sent: " + getThreadRequests());
        System.out.println("Total number of successful responses: " + getThreadSuccesses());
        System.out.println("Total number of unsuccessful requests: " + getThreadFailures());
        System.out.println("--------------------------------------------------------------");
        System.out.println("Test wall time: " + getTotalWallTime() + " seconds");
        System.out.println("Overall throughput across all phases: " + getThreadRequests() / getTotalWallTime());
        System.out.println("Total number of latency data points: " + getLatenciesList().size());
        System.out.println("--------------------------------------------------------------");
        System.out.println("Median Latency: " + calcMedianLatency());
        System.out.println("Mean Latency: " + calcMeanLatency());
        System.out.println("99th Percentile (99% of latency entries are < this number: " + calcPercentile(.99) +")");
        System.out.println("95th Percentile (95% of latency entries are < this number: " + calcPercentile(.95) + ")");

    }
}