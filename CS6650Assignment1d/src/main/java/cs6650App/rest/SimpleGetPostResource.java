package cs6650App.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/tests")
public class SimpleGetPostResource {

    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public String getAlive() {
        return "alive";
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String postText(String content) {

        return(String.valueOf(content.length()));
    }



}
