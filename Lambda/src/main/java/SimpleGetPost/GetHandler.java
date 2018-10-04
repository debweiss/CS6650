package SimpleGetPost;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class GetHandler implements RequestHandler<Request, Response> {

    public Response handleRequest(Request request, Context context) {

        String echo = request.alive;
        return new Response(echo);
    }
}

