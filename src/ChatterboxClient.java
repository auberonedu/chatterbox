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

import javax.imageio.IIOException;

/**
 * A simple command-line chat client for the Chatterbox server.
 *
 * Protocol summary (what the server expects):
 * 1) Client connects via TCP to host:port.
 * 2) Server sends a prompt asking for "Please enter your username and password, separated by a space".
 * 3) Client sends ONE LINE containing: username + space + password + newline.
 * 4) Server responds with either:
 *      - a line starting with the word "Welcome" (success), or
 *      - an error line (failure), then closes the connection.
 * 5) After success, the client:
 *      - prints any incoming server messages to the user output
 *      - reads user input and sends each line to the server
 *
 * Important design constraint:
 * - Do NOT read/write directly from System.in/System.out inside helper methods.
 *   Always use userInput/userOutput instead.
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
     *   javac src/*.java && java -cp src ChatterboxClient HOST PORT USERNAME PASSWORD
     *
     * Example:
     *   javac src/*.java && java -cp src ChatterboxClient localhost 12345 sharon abc123
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
                System.err.println("Usage: javac src/*.java && java -cp src ChatterboxClient HOST PORT USERNAME PASSWORD");
                System.exit(1);
            } 
            System.out.println("Read options: " + options.toString());

            System.out.println("Creating client...");
            
            ChatterboxClient client = new ChatterboxClient(options, System.in, System.out);
            System.out.println("Client created: " + client.toString());

            System.out.println("Connecting to server...");
            try {
                client.connect();
            } catch(IOException e) {
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
        }
        catch (UnsupportedOperationException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Parse command-line arguments into a ChatterboxOptions object.
     *
     * Required argument order:
     *   HOST
     *   PORT
     *   USERNAME
     *   PASSWORD
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
        // TODO: read args in the required order and return new ChatterboxOptions(host, port, username, password)

        // This if statement checks if the length != 4, if true throw IllegalArgumentException
        if(args.length != 4){
            throw new IllegalArgumentException("Invalid args length, expected 4 arguments");
        }

        // This stores the command-line arguments into their own variables
        String HOST = args[0];
        String PORT = args[1];
        String USERNAME = args[2];
        String PASSWORD = args[3];

        // Variable to store parsed string
        int parsedPort;

        try {
            // Initialize parsedPort by converting it into a INT
            parsedPort = Integer.parseInt(PORT);

            // Check to see if the port is valid, between 1..65535
            if (!(parsedPort >= 1 && parsedPort <= 65535)) {
                throw new IllegalArgumentException("Invaild Port, must be between 1..65535");
            }
            
        } catch (NumberFormatException e){
            throw new IllegalArgumentException("Not a vaild parseable number: " + e);
        }

        // Finally return new ChatterboxOptions(host, port, username, password)
        return new ChatterboxOptions(HOST, parsedPort, USERNAME, PASSWORD);
    }

    /**
     * Construct a ChatterboxClient from already-parsed options and user streams.
     *
     * Responsibilities:
     * - Store userInput and userOutput into fields.
     * - Copy host/port/username/password from options into fields.
     * - Do NOT open sockets or talk to the network here. That's connect().
     *
     * @param options parsed connection/auth settings
     * @param userInput stream to read user-typed data from
     * @param userOutput stream to print data to the user
     */
    public ChatterboxClient(ChatterboxOptions options, InputStream userInput, OutputStream userOutput) {
        this.userInput = new Scanner(userInput, StandardCharsets.UTF_8);
        this.userOutput = userOutput;
        // TODO: copy options.getHost(), getPort(), getUsername(), getPassword() into fields
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
        // Make sure to have this.serverReader and this.serverWriter set by the end of this method!
        // hint: get the streams from the sockets, use those to create the InputStreamReader/OutputStreamWriter and the BufferedReader/BufferedWriter

        //Create the socket
        Socket socket = new Socket(host, port);

        // InputStream(Raw bytes) -> InputStreamReader(decodes bytes to chars) -> BufferedReader(reading groups of chars, provides methods)
        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        this.serverReader = bufferedReader;

        // OutputStream (Raw bytes) -> OutputStreamWriter (decodes bytes to chars) -> BufferedWriter(reading groups of chars, provides methods)
        OutputStream outputStream = socket.getOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, java.nio.charset.StandardCharsets.UTF_8);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
        this.serverWriter = bufferedWriter;
      
   
    }

    /**
     * Authenticate with the server using the simple protocol.
     *
     * Responsibilities:
     * - Read and display the server's initial prompt line (if any)
     *   to userOutput.
     * - Send ONE LINE containing:
     *      username + " " + password + "\n"
     *   using serverOutput.
     * - Read ONE response line from serverReader.
     * - If the response indicates failure, throw IllegalArgumentException
     *   with that response text.
     * - If success, print the welcome line(s) to userOutput and return.
     *
     * Assumption:
     * - The server closes the connection after a failed auth.
     *
     * @throws IOException for network errors
     * @throws IllegalArgumentException for bad credentials / server rejection
     */
    public void authenticate() throws IOException, IllegalArgumentException {
        // throw new UnsupportedOperationException("Authenticate not yet implemented. Implement authenticate() and remove this exception!");
        // Hint: use the username/password instance variables, DO NOT READ FROM userInput
        // send messages using serverWriter (don't forget to flush!)

        // Set up user output
        OutputStream outputStream = userOutput;
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, java.nio.charset.StandardCharsets.UTF_8);
        BufferedWriter outputWriter = new BufferedWriter(outputStreamWriter);

        // Read the first line
        String line = serverReader.readLine();

        // Check to see if its not null then print out the initial line
        if(line != null){
            outputWriter.write(line);
            outputWriter.newLine();
            outputWriter.flush();
        }
        
        // Write the username to serverWriter, could also use serverWriter.write(username + " " + password + "\n");
        serverWriter.write(username + " " + password);
        serverWriter.newLine();
        serverWriter.flush();

        // Read the response from the server after sending the user name and password
        String response = serverReader.readLine();

        // Null checks to see if the response exists and contains the actual welcome greeting
        if (response == null){
            throw new IllegalArgumentException(response);
        }

        if (!response.toLowerCase().contains("welcome")) {
            throw new IllegalArgumentException(response);
        }

        // If checks pass write the response to the client
        outputWriter.write(response);
        outputWriter.newLine();
        outputWriter.flush();
         
    }

    /**
     * Start full-duplex chat streaming. SEE INSTRUCTIONS FOR HOW TO DO THIS PART BY PART
     *
     * Responsibilities:
     * - Run printIncomingChats() and sendOutgoingChats() in separate threads.
     *
     * Tip:
     * - Make printIncomingChats() work (single-threaded) before worrying about
     *   sendOutgoingChats() and threading.
     *
     * @throws IOException
     */
    public void streamChat() throws IOException {

        //Create threads for both incomingChats and outGoingChats
        Thread incomingChats = new Thread(() -> printIncomingChats());
        Thread outGoingChats = new Thread(() -> sendOutgoingChats());

        // Start the threads
        incomingChats.start();
        outGoingChats.start();
       
       
    }

    /**
     * Continuously read messages from the server and print them to the user.
     *
     * Responsibilities:
     * - Loop:
     *      readLine() from server
     *      if null -> server disconnected, exit program
     *      else write that line to userOutput
     *
     * Notes:
     * - Do NOT use System.out directly.
     * - If an IOException happens, treat it as disconnect:
     *   print a message to userOutput and exit.
     */
    public void printIncomingChats() {
        // Listen on serverReader
        // Write to userOutput, NOT System.

        // Create the output stream
        OutputStream outputStream = userOutput;
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, java.nio.charset.StandardCharsets.UTF_8);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        
        /*
         * Another alternative to writing this out is 
         * BufferedWriter bufferedWriter = new BufferedWriter(
         *                      new OutputStreamWriter(userOutput, java.nio.charset.StandardCharsets.UTF_8));
         */

        // Declare a variable called line
        String line;
        try{
            // Keep looping through the serverReader, print out lines example "Heart Beat" or msg
            while((line = serverReader.readLine())!= null){
                bufferedWriter.write(line);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
            // After disconnect print out message.
            bufferedWriter.write("server disconnected");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            System.exit(0);
        } catch (IOException e) {
            try{
                bufferedWriter.write("server disconnected");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch(IOException ignore) {}
            System.exit(0);
        }
    }

    /**
     * Continuously read user-typed messages and send them to the server.
     *
     * Responsibilities:
     * - Loop forever:
     *      if scanner has a next line, read it
     *      write it to serverOutput + newline + flush
     *
     * Notes:
     * - If writing fails (IOException), the connection is gone:
     *   print a message to userOutput and exit.
     */
    public void sendOutgoingChats() {
        // Use the userInput to read, NOT System.in directly
        // loop forever reading user input
        // write to serverOutput

        // Create the output stream
        OutputStream outputStream = userOutput;
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        // Infinite loop using .hasNextLine()
        try {
            while(userInput.hasNextLine()){
                // Store the users message 
                String message = userInput.nextLine();
                serverWriter.write(message);
                serverWriter.newLine();
                serverWriter.flush();
            }
        } catch ( IOException e){
            try{
                bufferedWriter.write("Writing failed");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException ignore){}
            System.exit(0);
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
