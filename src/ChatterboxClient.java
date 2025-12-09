import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import java.net.Socket;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/*
Worked on WAVE  1 - 7
MOST OF MY COMMENTS BEGIN WITH NOTE, some dont have but the main ones
have NOTE
 */


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
    private Scanner userInput; // for reading user input
    private OutputStream userOutput; // for printing to user

    // Readers/Writers for server I/O (set up in connect())
    private BufferedReader serverReader; // for reading from the network socket
    private BufferedWriter serverWriter; // for writing to the network socket

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


    //  This is Wave 1 --> for the command arg lines
    public static ChatterboxOptions parseArgs(String[] args) throws IllegalArgumentException {
        // TODO: read args in the required order and return new ChatterboxOptions(host, port, username, password)
        // Remove this exception
        // throw new UnsupportedOperationException("Argument parsing not yet implemented. Implement parseArgs and remove this exception");

        // NOTE: We need 4 arguments of validation for the program
        if ( args.length != 4) {
                throw new IllegalArgumentException("Expected 4 arguments: HOST PORT USERNAME PASSWORD");

        }

        //NOTE: The expeced order based on the required argument order
    String host = args[0];
    String portString = args[1];
    String username = args[2];
    String password = args[3];

    int port;
    try {
        // NOTE: This is for converting the portString to Integer
        //Its required coz the PORT is numerical
        port = Integer.parseInt(portString);
    } catch (NumberFormatException e) {
        // NOTE: It rhrows when the string isnt a valid numnber 
        throw new IllegalArgumentException("The port has to be a valid integer.");
    }

    if (port < 1 || port > 65535) {
        throw new IllegalArgumentException("Port must be between 1 and 65535.");
    }

    return new ChatterboxOptions(host, port, username, password);
}

// this is WAVE 2 --> Adding constructors    

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
        // NOTE: THE utf 8 is for the character encoding
        // NOTE: the userinput gets the input then the chat starts
        this.userInput = new Scanner(userInput, StandardCharsets.UTF_8);
       
        this.userOutput = userOutput;

        // NOTE: add // on line 196-> so it removes the red scuiggle line in line 188
        // throw new UnsupportedOperationException("Constructor not yet implemented. Implement ChatterboxClient constructor and remove this exception");
        // TODO: copy options.getHost(), getPort(), getUsername(), getPassword() into fields
    
        // NOTE: In the this.host (line 202 - 205) the constructor just saves the data
        // into the client's memory for them to be used with other methods
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

     //THis is wave 4 --> Connect

    public void connect() throws IOException {
        // throw new UnsupportedOperationException("Connect not yet implemented. Implement connect() and remove this exception!");

        // Make sure to have this.serverReader and this.serverWriter set by the end of this method!
        // hint: get the streams from the sockets, use those to create the InputStreamReader/OutputStreamWriter and the BufferedReader/BufferedWriter

        //FRED: --> WAVE4 PART
        // NOTE: Added socket connection
        // NOTE: The socket line makes the call --> then establish connection
        // if server's not there rhe exception is throen 
        // NOTE: The socket initializes the connection to the specified host and port

        Socket socket = new Socket(this.host, this.port);         

    // NOTE: Create UTF-8 input reader
    // NOTE: the socketgetinputstream --> gets the byte stram from the netword
    // NOTE: the inputstramreader changes the raw bytes to readable UTF 8 Texts 
    // that we can read
    InputStreamReader inputStreamReader =
        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
    this.serverReader = new BufferedReader(inputStreamReader);

    // Create UTF-8 output writer
    OutputStreamWriter outputStreamWriter =
        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
    // NOTE: The buffered writer helps send the text to the server --> 
    // kinda like an outbox in gmail
        this.serverWriter = new BufferedWriter(outputStreamWriter);
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

     //THis is wave 5 --> Authenticate
    public void authenticate() throws IOException, IllegalArgumentException {
        // throw new UnsupportedOperationException("Authenticate not yet implemented. Implement authenticate() and remove this exception!");
        // Hint: use the username/password instance variables, DO NOT READ FROM userInput
        // send messages using serverWriter (don't forget to flush!)

        
        // NOTE: Read users prompt if there is     
    String prompt = serverReader.readLine();
    if (prompt == null) {
          throw new IOException("Server disconnected before authentication prompt.");
    }

    // NOTE: Prompt sent to the user
    // NOTE: The getBytes() --> changes tect into bytes to be sent 
    // to the outstream
    userOutput.write((prompt + "\n").getBytes(StandardCharsets.UTF_8));
    userOutput.flush();

    // NOTE: Send username and password as required by the server
    String loginLine = username + " " + password;
    serverWriter.write(loginLine + "\n");
    // NOTE: The flush() sends it to the server directly for the login attempt
    serverWriter.flush();

    // Read server response
    String response = serverReader.readLine();
    // NOTE: If response is null, server disconnects
    if (response == null) {
        throw new IOException("Server disconnected during authentication.");
    }
     
    //NOTE: server's response to the user
    userOutput.write((response + "\n").getBytes(StandardCharsets.UTF_8));
    userOutput.flush();

    //NOTE: if successful, you get welcome, if not an exception is thrown
        if (response.startsWith("Welcome")) {
    } else {
    // NOTE: Failed authentication throws an exception
    // if the main method catches the exception it directly exits the client
        throw new IllegalArgumentException(response);
    }



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

    //  this is for wave 6 & 7 --> Chat streaming
    public void streamChat() throws IOException {
        // throw new UnsupportedOperationException("Chat streaming not yet implemented. Implement streamChat() and remove this exception!");
        
        // NOTE: in this wave, the incoming thread runs the thread printincomingchats everytime
        //thus helps the client can then keep reading the messages of the server  
        // The lambda makes the thread  shorter and cleaner. (eg line 351 with ->)


        //ADDED THE PRINT CHATS FOR WAVE 6
        // printIncomingChats();

        // updated the printincoming chats and added the try method
   Thread incomingThread = new Thread(() -> {
        try {
            printIncomingChats();
        } catch (IOException e) {
            // When server disconnects, this will throw the exception and 
            // the thread ends there
        }
    });

    Thread outgoingThread = new Thread(() -> {
        try {
            sendOutgoingChats();
        } catch (IOException e) {
            // Writing failed (server likely disconnected)
        }
    });

    // NOTE: Both of these start at the same time
    incomingThread.start();
    outgoingThread.start();

    try {
        // this tells the main thread to wait until its done reading messages
        incomingThread.join();
        outgoingThread.join();
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }


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
    public void printIncomingChats() throws IOException{
        // Listen on serverReader
        // Write to userOutput, NOT System.out

        // WAVE 6 continuation:  
        String line;
        // NOTE: As long as the readline is not null, the loop continues
        // as long as there are readlines or texts added. It will continue reading
    while ((line = serverReader.readLine()) != null) {
        // 
        userOutput.write((line + "\n").getBytes(StandardCharsets.UTF_8));
        // NOTE: Flush so the user sees the message ASAP
        userOutput.flush();
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
    public void sendOutgoingChats() throws IOException{
        // Use the userInput to read, NOT System.in directly
        // loop forever reading user input
        // write to serverOutput
        
        // WAVE 7: 
        while (true) {
            // NOTE: This is to check if there is anothe rline to read
        if (!userInput.hasNextLine()) {
            break; // if not found break and exit
        }
        String message = userInput.nextLine();
        serverWriter.write(message + "\n");
        serverWriter.flush();
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
