package SimpleGetPost;

public class Response {

        String echo;

        public String getEcho() {
            return echo;
        }

        public void setEcho(String echo) {

            this.echo = echo;
        }

        public Response(String echo) {

            this.echo = echo;
        }


        public Response() {
        }
    }
