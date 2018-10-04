package SimpleGetPost;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class PostHandler implements RequestHandler<Request, Response>{

    public Response handleRequest(Request request, Context context) {
        String stringLength = String.valueOf(request.alive.length());
        return new Response(stringLength);
    }
}

