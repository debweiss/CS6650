package cs6650App.app.src.main.java;


import java.util.concurrent.CountDownLatch;
import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

/**
 * Thread that is created based on below inputs and makes
 * one Get and one Post request per iteration
 */
public class GetPostThread extends Thread {

    private int numIterations;
    private String ipAddress;
    private String serverPort;
    private CountDownLatch countDownLatchStart;
    private CountDownLatch countDownLatchFinish;


    GetPostThread(int numIterations,
                  String ipAddress, String serverPort, CountDownLatch
                          countDownLatchStart, CountDownLatch countDownLatchFinish) {

        this.numIterations = numIterations;
        this.ipAddress = ipAddress;
        this.serverPort = serverPort;
        this.countDownLatchStart = countDownLatchStart;
        this.countDownLatchFinish = countDownLatchFinish;

    }

    /**
     * Makes a get request to a specified URI
     *
     * @param GET_URI specified URI to make the request
     * @throws ClientErrorException when client cannot connect
     */
    public static void getText(String GET_URI) throws ClientErrorException {

        Client client = ClientBuilder.newClient(); // create new client

        WebTarget target = client.target(GET_URI); // and set it up to connect

        Response response;

        long requestStartTime = System.nanoTime(); // mark start time to measure latency

            response = target.request().get(); // send the request

        long requestFinishTime = System.nanoTime(); // mark finish time to measure latency

        StatisticsGatherer.updateLatenciesList((requestFinishTime - requestStartTime)
                / (float) 1000000000); // update the list of request latency measurements

        StatisticsGatherer.updateRequestStatistics(); // update number of requests

        StatisticsGatherer.updateResponseSuccessOrFailure(response); // update success or failure counter
     }

    /**
     * "Posts" some text to a specified URI and the
     * response is the length of the text passed in
     *
     * @param POST_URI
     * @param textToPost
     */
    public static void doPostText(String POST_URI, String textToPost) throws ClientErrorException { // textToPost hardcoded for now

        Client client = ClientBuilder.newClient();  // create new client

        WebTarget baseTarget = client.target(POST_URI); // and set it up to connect

        Response myPostResponse;

        long requestStartTime = System.nanoTime(); // mark start time to measure latency

            myPostResponse = baseTarget // send the request
                    .request()
                    .post(Entity.json(textToPost));


        long requestFinishTime = System.nanoTime(); // mark finish time to measure latency

        StatisticsGatherer.updateLatenciesList((requestFinishTime - requestStartTime)
                / (float) 1000000000); // update the list of request latency measurements

        StatisticsGatherer.updateRequestStatistics(); // update number of requests

        StatisticsGatherer.updateResponseSuccessOrFailure(myPostResponse); // update success or failure counter
    }


    @Override
    public void run() { // some hardcoded stuff for now

         //  String POST_URI = "http://" + ipAddress + ":" + serverPort + "/testing/tests/"; // compose the URI based on the inputs
         //  String GET_URI = POST_URI + "test";

       String POST_URI = "https://ft2eupd7k8.execute-api.us-west-2.amazonaws.com/prod/";

       String GET_URI = "https://ap0vyszy0a.execute-api.us-west-2.amazonaws.com/prod/";

        for (int i = 0; i < numIterations; i++) { // for each iteration based on # of iterations input

            getText(GET_URI); // make get request
            doPostText(POST_URI, "{ 'alive':'alive' }"); // make post request

        }
        if (countDownLatchFinish != null) { // decrement the countdown if it's not down to 0 already
            countDownLatchFinish.countDown();
        }

    }

}

