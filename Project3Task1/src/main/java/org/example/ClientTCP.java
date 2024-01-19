/**
* Author: Siddhesh Badhan
 * Andrew ID: sbadhan
 * Last Modified: 03/17/2023
 * This class represents a TCP client that interacts with a server to perform various blockchain operations.
*/
package org.example;

import com.google.gson.Gson;
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class ClientTCP {

    public static void main(String[] args) throws IOException {
        Scanner s = new Scanner(System.in);
        BufferedReader typed = new BufferedReader(new InputStreamReader(System.in));

        while (true) {

            // Prompt the user for operation
            System.out.println("0. View basic blockchain status.\n" +
                    "1. Add a transaction to the blockchain.\n" +
                    "2. Verify the blockchain.\n" +
                    "3. View the blockchain.\n" +
                    "4. Corrupt the chain.\n" +
                    "5. Hide the corruption by repairing the chain.\n" +
                    "6. Exit");

            int userInput = s.nextInt();
            // request message to be sent to the server
            RequestMessage message;
            int difficulty;
            int blockID;
            // stores transaction data of block
            String data;
            // Create a Gson object
            Gson gson = new Gson();
            // Switch case for user input
            switch (userInput) {
                // If user requested to view the blockchain status
                case 0:{
                    message = new NormalRequestMessage(0);
                    // Request the blockchain operation from server and store the value of response
                    String response = blockchain_operations(message.toString());
                    // Parse JSON response from server into StatusResponseMessage
                    StatusResponseMessage responseMessage = gson.fromJson(response, StatusResponseMessage.class);
                    // Display details to the user
                    System.out.println("Current size of chain: " + responseMessage.chainSize);
                    System.out.println("Difficulty of most recent block: " + responseMessage.difficulty);
                    System.out.println("Total difficulty for all blocks: " + responseMessage.totalDifficulty);
                    System.out.println("Approximate hashes per second on this machine: " + responseMessage.hashesPerSecond);
                    System.out.println("Expected total hashes required for the whole chain: " + String.format("%.6f", (double) responseMessage.totalHashes));
                    System.out.println("Nonce for most recent block: " + responseMessage.recentNonce);
                    System.out.println("Chain hash: " + responseMessage.chainHash);
                }; break;

                // If user requested to add a transaction to the blockchain
                case 1:{
                    System.out.println("Enter difficulty > 0");
                    difficulty = Integer.parseInt(typed.readLine());
                    System.out.println("Enter transaction");
                    data = typed.readLine();
                    message = new AddRequestMessage(1, difficulty, data);
                    // Request the blockchain operation from server and store the value of response
                    String response = blockchain_operations(message.toString());
                    // Parse JSON response from server into NormalResponseMessage
                    NormalResponseMessage responseMessage = gson.fromJson(response, NormalResponseMessage.class);
                    System.out.println(responseMessage.response);
                }; break;

                // If user requested to verify the blockchain
                case 2:{
                    message = new NormalRequestMessage(2);
                    // Request the blockchain operation from server and store the value of response
                    String response = blockchain_operations(message.toString());
                    // Parse JSON response from server into VerificationResponseMessage
                    VerificationResponseMessage verificationResponseMessage = gson.fromJson(response, VerificationResponseMessage.class);
                    // If chain verification was successful
                    if (verificationResponseMessage.verificationOp.equals("TRUE")) {
                        System.out.println("Chain verification: " + verificationResponseMessage.verificationOp);
                    }
                    // If chain verification was not successful
                    else {
                        System.out.println("Chain verification: FALSE");
                        System.out.println(verificationResponseMessage.verificationOp);
                    }
                    System.out.println(verificationResponseMessage.response);
                }; break;

                // If user requested to view the blockchain
                case 3:{
                    System.out.println("View the Blockchain");
                    message = new NormalRequestMessage(3);
                    // Request the blockchain operation from server and store the value of response
                    String response = blockchain_operations(message.toString());
                    // Server would return a JSON message here
                    System.out.println(response);
                }; break;

                // If user requested to corrupt the blockchain
                case 4:{
                    System.out.println("corrupt the Blockchain");
                    // Prompt the user for difficulty
                    System.out.println("Enter block ID of block to corrupt");
                    blockID = Integer.parseInt(typed.readLine());
                    // Prompt the user for transaction
                    System.out.println("Enter new data for block " + blockID);
                    data = typed.readLine();
                    message = new CorruptRequestMessage(4, blockID, data);
                    // Request the blockchain operation from server and store the value of response
                    String response = blockchain_operations(message.toString());
                    // Parse JSON response from server into NormalResponseMessage
                    NormalResponseMessage responseMessage = gson.fromJson(response, NormalResponseMessage.class);
                    System.out.println(responseMessage.response);
                }; break;

                // If user requested to hide the corruption by repairing the chain
                case 5:{
                    message = new NormalRequestMessage(5);
                    // Request the blockchain operation from server and store the value of response
                    String response = blockchain_operations(message.toString());
                    // Parse JSON response from server into NormalResponseMessage
                    NormalResponseMessage responseMessage = gson.fromJson(response, NormalResponseMessage.class);
                    System.out.println(responseMessage.response);
                }; break;
                // If user requested to exit
                case 6:{
                    // Halt client execution
                    System.exit(0);
                }; break;
            }
        }
    }

/**

 This method sends a given message to a server and receives a JSON response from the server.
 It sets up a TCP style socket with the server, writes the message to the socket,
 and receives a JSON response from the server. It returns the JSON response as a string.
 @param message the message to send to the server in JSON format
 @return a string representing the JSON response from the server
 */

 public static String blockchain_operations(String message) {
        // Define a TCP style Socket
        Socket clientSocket = null;
        // Stores the JSON response from the server
        String response = "";
        try {
            clientSocket = new Socket("localhost", 6789);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
            // Request to the server with the JSON request message
            out.println(message);
            // Flush to server socket
            out.flush();
            // Store the JSON response from the server
            response = in.readLine(); // read a line of data from the stream
        }
        // Handle general I/O exceptions
        catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
        }
        // Return the JSON response from server
        return response;
    }
}

