//This parses command line arguments and returns the correct output
public class Iparser {
    private String[] args;
    private boolean format;
    private State status;
    private int server_port;

    public Iparser(String[] args) {
        this.args = args;
        this.format = false;
        this.status = State.UNDEFINED;
        this.server_port = -1;
    }

    private boolean formatChecker() {
        if (this.args.length > 2) {
            // check the first two arguments to make sure they are java and I[refer
            String firstArg = this.args[0];
            String secondArg = this.args[1];
            if (!firstArg.equals("java") || secondArg.equals("Iprefer")) {
                return false;
            }
            if (this.args.length == 5) {
                String thirdArg = this.args[2];
                String fourthArg = this.args[3];
                String fifthArg = this.args[4];
                if (!thirdArg.equals("-s") || !fourthArg.equals("-p")) {

                    return false;
                }
                try {
                    int server_port = Integer.parseInt(fifthArg);
                    if (server_port < 1024 || server_port > 65535) {
                        return false;
                    } else {
                        this.server_port = server_port;
                        this.status = State.SERVER;
                        return true;
                    }

                } catch (NumberFormatException e) {
                    return false;
                }

            }
            if (this.args.length == 9) {
                String thirdArg = this.args[2];
                String fourthArg = this.args[3];
                String fifthArg = this.args[4];
                String sixthArg = this.args[5];
                String seventhArg = this.args[6];
                String eigthArg = this.args[7];
                String ninthArg = this.args[8];

            } else {
                return false;
            }

        } else {
            return false;
        }
        return true;
    }

}