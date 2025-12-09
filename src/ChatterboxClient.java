import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
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
        // Remove this exception
        if (args.length != 4) {
            throw new IllegalArgumentException("should be 4 arguments");
        }
        // the args values comes from:
        // javac src/*.java && java -cp src ChatterboxClient localhost 12345 sharon abc123
        // at the very end of the command line we listed: HOST, PORT, USERNAME, PASSWORD
        String host = args[0];

        int port;
        //check, if args[1] value is parsable into an integer, if not throw
        try {
            Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("not a number");
        }

        port = Integer.parseInt(args[1]);

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("port not in range");
        }

        String username = args[2];
        String password = args[3];

        return new ChatterboxOptions(host, port, username, password);
        //throw new UnsupportedOperationException("Argument parsing not yet implemented. Implement parseArgs and remove this exception");
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

        // throw new UnsupportedOperationException("Constructor not yet implemented. Implement ChatterboxClient constructor and remove this exception");
        // TODO: copy options.getHost(), getPort(), getUsername(), getPassword() into fields
        
        //copy all of these into existing fields at the very top (instances).
        try {
            this.host = options.getHost();
            this.port = options.getPort();
            this.username = options.getUsername();
            this.password = options.getPassword();
        } catch (UnsupportedOperationException e) {
            System.err.println("Constructor not yet implemented. Implement ChatterboxClient constructor and remove this exception");
        }
        
       
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
        // throw new UnsupportedOperationException("Connect not yet implemented. Implement connect() and remove this exception!");

        // Make sure to have this.serverReader and this.serverWriter set by the end of this method!
        // hint: get the streams from the sockets, use those to create the InputStreamReader/OutputStreamWriter and the BufferedReader/BufferedWriter

        Socket socket = new Socket(host, port);

        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8);
        this.serverReader = new BufferedReader(inputStreamReader);
        // InputStream inputStream = socket.getInputStream();
        // InputStreamReader inputStreamReader = new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8);

        OutputStream outputStream = socket.getOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, java.nio.charset.StandardCharsets.UTF_8);
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
    public void authenticate() throws IOException, IllegalArgumentException {
        // throw new UnsupportedOperationException("Authenticate not yet implemented. Implement authenticate() and remove this exception!");
        // Hint: use the username/password instance variables, DO NOT READ FROM userInput
        // send messages using serverWriter (don't forget to flush!)

        // step 1 Read and display the server's initial prompt line (if any)
        // to userOutput.
        String line;
        if ((line = serverReader.readLine()) != null) 
        {
            userOutput.write((line + "\n").getBytes(StandardCharsets.UTF_8));
            userOutput.flush();
        }

        //step 2 Send ONE LINE containing:
        //      username + " " + password + "\n"
        serverWriter.write(username + " " + password + "\n");
        serverWriter.flush();
        
        // String response;
        // try ((response = serverReader.readLine()) != null) 
        // {
            
        // } catch (IOException e) {
        //     throw new IOException("hi");
        // }
        //step 3: Read ONE response line from serverReader
        String response = serverReader.readLine();

        // Step 4: If the response indicates failure, throw IllegalArgumentException with that response text.
        String expected = "Welcome to the server, " + username + "!";
        // String expected = "hello";
        if (!response.equals(expected)) {
            // userOutput.write(("this is the response that does not match the thinggy: " + response + "\n").getBytes(StandardCharsets.UTF_8));
            // userOutput.flush();
            throw new IllegalArgumentException("this is the response that does not match the thinggy:" + response);
        // Step 5: If success, print the welcome line(s) to userOutput and return.
        } else if (response.equals(expected)) {
            userOutput.write((response + "\n").getBytes(StandardCharsets.UTF_8));
            userOutput.flush();
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
    public void streamChat() throws IOException {
        // throw new UnsupportedOperationException("Chat streaming not yet implemented. Implement streamChat() and remove this exception!");

        Thread thread1 = new Thread(() -> printIncomingChats());
        Thread thread2 = new Thread(() -> sendOutgoingChats());

        thread1.start();
        thread2.start();

        // try (ServerSocket server = new ServerSocket(port)) {
        //     while (true) {
        //         Socket socket = server.accept();
        //         Thread clientThread = new Thread(() ->  handleClient(socket));
        //         clientThread.start();
        //     }
        // }
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
        // Write to userOutput, NOT System.out
        String line;
        try {
            while (true) {
                // readLine() from server
                line = serverReader.readLine();
                // if null -> server disconnected, exit program
                if (line == null) {
                    System.exit(1);
                // else write that line to userOutput
                } else if (line != null) {
                    userOutput.write((line + "\n").getBytes(StandardCharsets.UTF_8));
                    userOutput.flush();
                }
            }
        } catch (IOException e) {
            try {
                userOutput.write(("disconnect: " + e.getMessage() + "\n").getBytes(StandardCharsets.UTF_8));
                userOutput.flush();
            } catch (IOException i) {
                // return;
                System.exit(1);
            }
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

        String line;
        while (true) {
            try {
                if (userInput.hasNextLine()) {
                    line = userInput.nextLine();
                    serverWriter.write(line);
                    serverWriter.newLine();
                    serverWriter.flush();
                }
            } catch (IOException e) {
                Thread.currentThread().interrupt();
                return;
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
