import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * A simple command-line chat client for the Chatterbox server.
 *
 * Protocol summary (what the server expects):
 * 1) Client connects via TCP to host:port.
 * 2) Server sends a prompt asking for "Please enter your username and password,
 * separated by a space".
 * 3) Client sends ONE LINE containing: username + space + password + newline.
 * 4) Server responds with either:
 * - a line starting with the word "Welcome" (success), or
 * - an error line (failure), then closes the connection.
 * 5) After success, the client:
 * - prints any incoming server messages to the user output
 * - reads user input and sends each line to the server
 *
 * Important design constraint:
 * - Do NOT read/write directly from System.in/System.out inside helper methods.
 * Always use userInput/userOutput instead.
 */
public class ChatterboxClient {

    private String host;
    private int port;
    private String username;
    private String password;

    // Streams for user I/O
    private Scanner userInput;
    private OutputStream userOutput;

    // Readers/Writers for server I/O (set up in connect())
    private BufferedReader serverReader;
    private BufferedWriter serverWriter;

    /**
     * Program entry.
     *
     * Expected command-line usage:
     * javac src/*.java && java -cp src ChatterboxClient HOST PORT USERNAME PASSWORD
     *
     * Example:
     * javac src/*.java && java -cp src ChatterboxClient localhost 12345 sharon
     * abc123
     *
     *
     * This method is already complete. Your work is in the TODO methods below.
     */
    public static void main(String[] args) {
        ChatterboxOptions options = null;
        System.out.println("Parsing options...");
        try {
            try {
                options = parseArgs(args);
            } catch (IllegalArgumentException e) {
                System.err.println("Error parsing arguments");
                System.err.println(e.getMessage());
                System.err.println(
                        "Usage: javac src/*.java && java -cp src ChatterboxClient HOST PORT USERNAME PASSWORD");
                System.exit(1);
            }
            System.out.println("Read options: " + options.toString());

            System.out.println("Creating client...");

            ChatterboxClient client = new ChatterboxClient(options, System.in, System.out);
            System.out.println("Client created: " + client.toString());

            System.out.println("Connecting to server...");
            try {
                client.connect();
            } catch (IOException e) {
                System.err.println("Failed to connect to server");
                System.err.println(e.getMessage());
                System.exit(1);
            }
            System.out.println("Connected to server");

            System.out.println("Authenticating...");
            try {
                client.authenticate();
            } catch (IOException e) {
                System.err.println("Error while attempting to authenticate");
                System.err.println(e.getMessage());
                System.exit(1);
            } catch (IllegalArgumentException e) {
                System.err.println("Failed authentication");
                System.err.println(e.getMessage());
                System.exit(1);
            }
            System.out.println("Finished authentication");

            System.out.println("Beginning chat streaming");
            try {
                client.streamChat();
            } catch (IOException e) {
                System.err.println("Error streaming chats");
                System.err.println(e.getMessage());
                System.exit(1);
            }
        } catch (UnsupportedOperationException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Parse command-line arguments into a ChatterboxOptions object.
     *
     * Required argument order:
     * HOST
     * PORT
     * USERNAME
     * PASSWORD
     *
     * Rules:
     * - If args.length != 4, throw IllegalArgumentException.
     * - PORT must parse as an integer in the range 1..65535, else throw.
     *
     * @param args raw command-line arguments
     * @return a fully populated ChatterboxOptions
     * @throws IllegalArgumentException on any bad/missing input
     */
    public static ChatterboxOptions parseArgs(String[] args) throws IllegalArgumentException {
        // TODO: read args in the required order and return new ChatterboxOptions(host,
        // port, username, password)
        // Remove this exception

        // first we check if the number of arguments is correct
        if (args.length != 4) {
            throw new IllegalArgumentException(
                    "Error Incorrect number of arguments. expected 4 arguments host, port, username, password");
        }

        // assign the arguments into variables
        String host = args[0];
        String port = args[1];
        String username = args[2];
        String password = args[3];

        // since the port is a string we need to parse it into an integer
        int portNumber;
        try {
            portNumber = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            // if its not a number we throw an exception
            throw new IllegalArgumentException("Error port must be a number");
        }

        // here we check if the port is in the valid range
        if (portNumber < 1 || portNumber > 65535) {
            throw new IllegalArgumentException("Error port number must be in the range 1-65535");
        }
        // if after all the checks we are good we return the options
        return new ChatterboxOptions(host, portNumber, username, password);

        // throw new UnsupportedOperationException("Argument parsing not yet
        // implemented. Implement parseArgs and remove this exception");
    }

    /**
     * Construct a ChatterboxClient from already-parsed options and user streams.
     *
     * Responsibilities:
     * - Store userInput and userOutput into fields.
     * - Copy host/port/username/password from options into fields.
     * - Do NOT open sockets or talk to the network here. That's connect().
     *
     * @param options    parsed connection/auth settings
     * @param userInput  stream to read user-typed data from
     * @param userOutput stream to print data to the user
     */
    public ChatterboxClient(ChatterboxOptions options, InputStream userInput, OutputStream userOutput) {
        this.userInput = new Scanner(userInput, StandardCharsets.UTF_8);
        this.userOutput = userOutput;

        // throw new UnsupportedOperationException(
        // "Constructor not yet implemented. Implement ChatterboxClient constructor and
        // remove this exception");
        // TODO: copy options.getHost(), getPort(), getUsername(), getPassword() into
        // fields

        // what we did here is store the values from the options object into the fields
        this.host = options.getHost();
        this.port = options.getPort();
        this.username = options.getUsername();
        this.password = options.getPassword();

    }

    /**
     * Open a TCP connection to the server.
     *
     * Responsibilities:
     * - Create a Socket to host:port.
     * - Populate the serverReader and serverWriter from that socket
     * - If connection fails, let IOException propagate.
     *
     * After this method finishes successfully, serverReader/serverWriter
     * must be non-null and ready for I/O.
     *
     * @throws IOException if the socket cannot be opened
     */
    public void connect() throws IOException {
        // throw new UnsupportedOperationException(
        // "Connect not yet implemented. Implement connect() and remove this
        // exception!");

        // Make sure to have this.serverReader and this.serverWriter set by the end of
        // this method!
        // hint: get the streams from the sockets, use those to create the
        // InputStreamReader/OutputStreamWriter and the BufferedReader/BufferedWriter

        // here we create the socket to connect to the server
        Socket socket = new Socket(this.host, this.port);

        // now we set up the reader and writer for the server communication
        // this one is for reading from the server
        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                java.nio.charset.StandardCharsets.UTF_8);
        this.serverReader = new BufferedReader(inputStreamReader);

        // this one is for writing to the server
        OutputStream outputStream = socket.getOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream,
                java.nio.charset.StandardCharsets.UTF_8);
        this.serverWriter = new BufferedWriter(outputStreamWriter);
    }

    /**
     * Authenticate with the server using the simple protocol.
     *
     * Responsibilities:
     * - Read and display the server's initial prompt line (if any)
     * to userOutput.
     * - Send ONE LINE containing:
     * username + " " + password + "\n"
     * using serverOutput.
     * - Read ONE response line from serverReader.
     * - If the response indicates failure, throw IllegalArgumentException
     * with that response text.
     * - If success, print the welcome line(s) to userOutput and return.
     *
     * Assumption:
     * - The server closes the connection after a failed auth.
     *
     * @throws IOException              for network errors
     * @throws IllegalArgumentException for bad credentials / server rejection
     */
    public void authenticate() throws IOException, IllegalArgumentException {
        // throw new UnsupportedOperationException(
        // "Authenticate not yet implemented. Implement authenticate() and remove this
        // exception!");
        // Hint: use the username/password instance variables, DO NOT READ FROM
        // userInput
        // send messages using serverWriter (don't forget to flush!)

        // print the servers first line if it has one
        System.out.println(serverReader.readLine());

        // send the username and password to the server
        serverWriter.write(this.username + " " + this.password);
        serverWriter.newLine();
        // the flush is important to make sure the data is sent
        serverWriter.flush();

        // read the server reply
        String reply = serverReader.readLine();

        // check if the reply starts with welcome if not we throw an exception
        if (!reply.startsWith("Welcome")) {
            throw new IllegalArgumentException(reply);
        }

        // if we are here it means we are authenticated so we print the welcome message
        System.out.println(reply);

    }

    /**
     * Start full-duplex chat streaming. SEE INSTRUCTIONS FOR HOW TO DO THIS PART BY
     * PART
     *
     * Responsibilities:
     * - Run printIncomingChats() and sendOutgoingChats() in separate threads.
     *
     * Tip:
     * - Make printIncomingChats() work (single-threaded) before worrying about
     * sendOutgoingChats() and threading.
     *
     * @throws IOException
     */
    public void streamChat() throws IOException {
        // throw new UnsupportedOperationException(
        // "Chat streaming not yet implemented. Implement streamChat() and remove this
        // exception!");

        // Start two threads:
        // - one that runs printIncomingChats()
        Thread incomingThread = new Thread(() -> printIncomingChats());
        incomingThread.start();

        // - one that runs sendOutgoingChats()
        Thread outgoingThread = new Thread(() -> sendOutgoingChats());
        outgoingThread.start();
    }

    /**
     * Continuously read messages from the server and print them to the user.
     *
     * Responsibilities:
     * - Loop:
     * readLine() from server
     * if null -> server disconnected, exit program
     * else write that line to userOutput
     *
     * Notes:
     * - Do NOT use System.out directly.
     * - If an IOException happens, treat it as disconnect:
     * print a message to userOutput and exit.
     */
    public void printIncomingChats() {
        // Listen on serverReader
        // Write to userOutput, NOT System.out

        // here i will read from the server and write to the user output
        try {
            // loop forever reading from the server
            while (true) {
                // read a line from the server
                String line = serverReader.readLine();

                // check if the line is null meaning the server disconnected
                if (line == null) {
                    // if so we notify the user and exit
                    // the get bytes is to convert the string to bytes
                    userOutput.write("Server disconnected. \n".getBytes());
                    // flush to make sure the data is sent
                    userOutput.flush();
                    // exit the program
                    System.exit(0);
                }
                // if we are here it means we got a valid line so we write it to the user output
                userOutput.write((line + "\n").getBytes());
                userOutput.flush();
            }
        } catch (IOException e) { // if we get an exception we treat it as a disconnect
            try { // this is just in case writing to user output fails
                userOutput.write("Connection lost. Exiting.\n".getBytes());
                userOutput.flush();
            } catch (IOException ex) { // we have the catch
                // here we intentionally do nothing since we are already exiting anyway
            }
            System.exit(0);
        }
    }

    /**
     * Continuously read user-typed messages and send them to the server.
     *
     * Responsibilities:
     * - Loop forever:
     * if scanner has a next line, read it
     * write it to serverOutput + newline + flush
     *
     * Notes:
     * - If writing fails (IOException), the connection is gone:
     * print a message to userOutput and exit.
     */
    public void sendOutgoingChats() {
        // Use the userInput to read, NOT System.in directly
        // loop forever reading user input
        // write to serverOutput
        // flush after each write
        while (true) {
            // check if there is a next line from the user
            if (userInput.hasNextLine()) {
                // read the line
                String msg = userInput.nextLine();
                // try to send it to the server
                try {
                    // write the message to the server
                    serverWriter.write(msg);
                    // add a new line
                    serverWriter.newLine();
                    // flush to make sure the data is sent
                    serverWriter.flush();
                } catch (IOException e) { // if we get an exception we treat it as a disconnect
                    try {
                        // notify the user that the connection is lost and we are exiting
                        userOutput.write("Connection lost. Exiting.\n".getBytes());
                        userOutput.flush();
                    } catch (IOException ex) {
                        // do nothing since we are exiting anyway
                    }
                    System.exit(0);
                }
            }
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "ChatterboxClient [host=" + host + ", port=" + port + ", username=" + username + ", password=" + password
                + "]";
    }
}
