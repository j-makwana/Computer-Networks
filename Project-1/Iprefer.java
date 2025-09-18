
public class Iprefer {

    public static void main(String[] args) {
        // main method body
        State state = State.UNDEFINED;
        int server_port = -1;
        String hostname = null;
        int timeInSeconds = 0;
        Iparser myCLIparser = new Iparser(args);
        String[] result = myCLIparser.getResults();
        if (result.length == 1) {
            System.out.println(result[0]);
            return;
        }
        if (result[0].equals("SERVER")) {
            state = State.SERVER;
            server_port = Integer.parseInt(result[1]);

        } else {
            state = State.CLIENT;
            server_port = Integer.parseInt(result[1]);
            hostname = result[2];
            timeInSeconds = Integer.parseInt(result[3]);

        }

        /////////////////// SOCKET PROGRAMMING////////////////////////
        // based on whether this is a client prgram/ server program we gotta initalize
        /////////////////// things

    }

}
