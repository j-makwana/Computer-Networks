//This parses command line arguments and returns the correct output
public class Iparser {
    private String[] args;
    private boolean format;
    private State status;
    private int server_port;
    private int ERROR_CODE;
    private String hostname;
    private int timeInSeconds;

    public Iparser(String[] args) {
        this.args = args;
        this.format = false;
        this.status = State.UNDEFINED;
        this.server_port = -1;
        this.ERROR_CODE = 0;
        this.timeInSeconds = -1;
    }

    public String[] getResults() {
        this.format = formatChecker();
        // if format is wrong
        if (!this.format) {
            // check what error code is and return that
            if (this.ERROR_CODE == 1) {
                // problem with the port number
                return new String[] { "Error: port number must be in the range 1024 to 65535" };
            } else {
                return new String[] { "Error: missing or additional arguments" };
            }
        }
        if (this.status == State.SERVER) {
            // we return just the port
            return new String[] { "SERVER", Integer.toString(this.server_port) };
        } else {
            return new String[] { "CLIENT", Integer.toString(this.server_port), this.hostname,
                    Integer.toString(this.timeInSeconds) };
        }
    }

    private boolean formatChecker() {
        // check the first two arguments to make sure they are java and I[refer
        if (this.args.length == 3) {
            String firstArg = this.args[0];
            String secondArg = this.args[1];
            String thirdArg = this.args[2];
            if (!firstArg.equals("-s") || !secondArg.equals("-p")) {
                this.ERROR_CODE = 2;
                return false;
            }
            try {
                int server_port = Integer.parseInt(thirdArg);
                if (server_port < 1024 || server_port > 65535) {
                    this.ERROR_CODE = 1;
                    return false;
                } else {
                    this.server_port = server_port;
                    this.status = State.SERVER;
                }

            } catch (NumberFormatException e) {
                this.ERROR_CODE = 2;
                return false;
            }

        }
        if (this.args.length == 7) {
            String firstArg = this.args[0];
            String secondArg = this.args[1];
            String thirdArg = this.args[2];
            String fourthArg = this.args[3];
            String fifthArg = this.args[4];
            String sixthArg = this.args[5];
            String seventhArg = this.args[6];
            if (!firstArg.equals("-c") || !secondArg.equals("-h") || !fourthArg.equals("-p")
                    || !sixthArg.equals("-t")) {
                this.ERROR_CODE = 2;
                return false;
            }
            this.hostname = thirdArg;
            try {
                int server_port = Integer.parseInt(fifthArg);
                if (server_port < 1024 || server_port > 65535) {
                    this.ERROR_CODE = 1;
                    return false;
                }
                this.timeInSeconds = Integer.parseInt(seventhArg);
                this.server_port = server_port;
                this.status = State.CLIENT;

            } catch (NumberFormatException e) {
                this.ERROR_CODE = 2;
                return false;
            }

        } else {
            this.ERROR_CODE = 2;
            return false;
        }

        return true;
    }

}