# chatterbox

An assignment to explore sockets and multithreading. MAKE SURE TO COMMIT AND PUSH AS YOU GO. YOU SHOULD BE MAKING MULTIPLE COMMITS PER WAVE.

DO NOT USE AI FOR THIS PROJECT

## Wave 1: Understand-------------Completed
1. Take a moment to read through the ChatterboxClient.java file. Note what is currently implemented, and what you will be expected to implement.
1. Try running ChatterboxServer using the example command in ChatterboxServer.java. You do not need to understand everything in this file, but you will be using the server to validate that your client is working! If working, it will talk about setting up, then every 10 seconds show a heartbeat message. It will take about 10 seconds for the first heartbeat to show up.
1. Try running ChatterboxClient using the command in the file. It will tell you that argument parsing is not yet implemented (because you haven't written it yet!) This is expected. You will be resolving this in the next wave.

## Wave 2: Argument Parsing
1. Implement `parseArgs` according to the instructions.
1. Validate your solution by re-running the client. It should print out the arguments it parsed, then an error about the constructor. Make sure everything is parsed correctly! You will handle the constructor in the next wave.
1. Make sure you've committed and pushed!

## Wave 3: Client Constructor
1. Implement the ChatterboxClient constructor. You will note it is already partially created for you.
1. Validate your solution by re-running the client. It should now give you an error about connect()
1. Make sure you've committed and pushed!

## Wave 4: Connect
1. Implement the connect method. This will consist of making a socket on the correct port and using its streams to make buffered readers/writers.
1. Hint: for the purposes of this assignment don't worry about the warnings of not closing resources. There are ways to nicely handle this, but it's also very easy to accidentally close things you don't want to. Come by office hours if you want to talk about the right way to handle this.
1. MAKE SURE THE SERVER IS RUNNING.
1. Validate your solution by re-running the client. Make sure to use localhost and the same port the server is on. It should now give you an error about authenticating.
1. BE CAREFUL TO MAKE SURE YOU'RE DOING THIS PROPERLY! Make sure to set up this.serverReader and this.serverWriter in connect()!
1. Make sure you've committed and pushed!

## Wave 5: Authenticate
1. Implement the authenticate method. Your client will need to send the username and password, separated by a space and ended with a newline.
1. MAKE SURE THE SERVER IS RUNNING.
1. Validate your solution by re-running the client. Make sure to use localhost and the same port the server is on. If working, you should see a welcome message in the client and the server notify when the client disconnects. The client should then give an error about chat streaming.
1. Make sure you've committed and pushed!

## Wave 6: Print Incoming
1. Implement `printIncomingChats` and call it in `streamChat`. Do not yet worry about threading or outgoing chats. YOU DO NOT NEED TO MAKE ANY NEW THREADS FOR THIS WAVE.
1. MAKE SURE THE SERVER IS RUNNING.
1. Validate your solution by re-running the client. Make sure to use localhost and the same port the server is on.
1. You should see a repeated heartbeat message from the server when it is working. The heartbeats should come every 10 seconds or so
1. Make sure you've committed and pushed!

## Wave 7: Send Outgoing
1. Implement `sendOutgoingChats`. Modify `streamChat` so that it properly multithreads between printing incoming and sending outgoing.
1. MAKE SURE THE SERVER IS RUNNING.
1. Validate your solution by re-running the client. Make sure to use localhost and the same port the server is on. You should be able to type in the client and see your message echoed back to you after hitting enter.
1. Try connecting multiple clients! You'll need to use different usernames/passwords from sample_users.txt
1. Make sure you've committed and pushed!

## Wave 8: Connecting over the internet
1. Soon you will be emailed an IP address, port, and a set of usernames and passwords to use for connecting to our live class server. DO NOT SHARE THESE ADDRESSES/PORTS/PASSWORDS WITH ANYONE AND DO NOT COMMIT THEM TO YOUR REPOSITORY. You can place them in my_passwords.txt for easy access. That file is in the .gitignore, so it should not get committed
1. Attempt connecting your client! Make sure to use the actual IP address, NOT localhost.
1. The server should have the same basic behavior, try chatting with your classmates and making sure everything works! I may put a few extra anti-spam measures on the server. DO NOT ATTACK THE MAIN CLASS SERVER. DO NOT SPAM IT. I will provide a separate server for you to try to attack.

## Wave 9: (Optional) Attack
1. You will be emailed a separate server and set of usernames / passwords that you are allowed to attack. If you are able to take it down, you will receive extra credit. You are allowed to use AI to help you with your attack, but ONLY AFTER YOU HAVE ALREADY MADE A PR for your project. Do any attack commits for a malicious client in a separate branch.

 