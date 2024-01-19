/**
 * Author: Siddhesh Badhan
 * Andrew ID: sbadhan
 * Last Modified: 03/17/2023
 *This class represents a TCP server that interacts with a client to perform various blockchain operations.
 */

package org.example;


import com.google.gson.Gson;
import java.net.*;
import java.io.*;
import java.sql.Timestamp;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ServerTCP {

    // Stores an array list of blocks in the blockchain
    static BlockChain blockChain = new BlockChain();
    // Stores the JSON response to be sent to the client
    static String json_response = null;
    // Stores the ResponseMessage to be converted to JSON which would be later sent to the client
    static ResponseMessage responseMessage;
    // Stores the response which is a part of the overall response message
    static String response;
    static Gson gson = new Gson();
    public static void main(String[] args) {
        // Define a TCP style Socket
        Socket clientSocket = null;
        // Define a TCP style ServerSocket
        ServerSocket listenSocket;

        try {

            int serverPort = 6789;
            listenSocket = new ServerSocket(serverPort);
            // Create the first Block, called the genesis Block
            Block genesis = new Block(0, blockChain.getTime(), "Genesis", 2);
            // Set the previous hash of the genesis block to be an empty String
            genesis.setPreviousHash("");
            // Compute the hashes per second on this system
            blockChain.computeHashesPerSecond();
            // Update chain hash by the hash of the genesis Block
            blockChain.chainHash = genesis.proofOfWork();
            // add Genesis block to chain
            blockChain.blkChain.add(genesis);

            System.out.println("Blockchain server running");
            System.out.println("We have a visitor");
            boolean flag = true;

            while (flag == true) {

                clientSocket = listenSocket.accept();
                Scanner in;
                in = new Scanner(clientSocket.getInputStream());
                PrintWriter out;
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));

                // Get the input from the client in JSON format
                String userInput = in.nextLine();
                // Convert JSON client request into a RequestMessage format
                RequestMessage requestMessage = gson.fromJson(userInput, RequestMessage.class);

                switch(requestMessage.operation){
                    case 0:{
                        // Form the JSON response by calling viewBlockChainStatus()
                        json_response = viewBlockChainStatus(requestMessage.operation);
                        System.out.println("Response : " + json_response);
                        // Reply the JSON response to the client
                        out.println(json_response);
                        out.flush();
                    }; break;
                    case 1:{
                        System.out.println("Adding a block");
                        // Form a AddRequestMessage from the client request
                        AddRequestMessage message = gson.fromJson(userInput, AddRequestMessage.class);
                        // Form the JSON response by calling addTransaction()
                        json_response = addTransaction(message);
                        System.out.println("..." + json_response);
                        // Reply the JSON response to the client
                        out.println(json_response);
                        out.flush();
                    }; break;
                    case 2:{
                        System.out.println("Verifying entire chain");
                        // Form a NormalRequestMessage from the client request
                        NormalRequestMessage message = gson.fromJson(userInput, NormalRequestMessage.class);
                        // Form the JSON response by calling verifyBlockChain()
                        json_response = verifyBlockChain(message);
                        // Reply the JSON response to the client
                        out.println(json_response);
                        out.flush();
                    }; break;
                    case 3:{
                        System.out.println("View the Blockchain");
                        // Form the JSON response by calling viewBlockChain()
                        json_response = viewBlockChain();
                        System.out.println("Setting response to " + json_response);
                        // Reply the JSON response to the client
                        out.println(json_response);
                        out.flush();
                    }; break;
                    case 4:{
                        System.out.println("Corrupt the Blockchain");
                        // Form a CorruptRequestMessage from the client request
                        CorruptRequestMessage message = gson.fromJson(userInput, CorruptRequestMessage.class);
                        // Form the JSON response by calling corruptBlockChain()
                        json_response = corruptBlockChain(message);
                        System.out.println("Setting response to " + response);
                        // Reply the JSON response to the client
                        out.println(json_response);
                        out.flush();
                    }; break;
                    case 5:{
                        System.out.println("Repairing the entire chain");
                        // Form a NormalRequestMessage from the client request
                        NormalRequestMessage message = gson.fromJson(userInput, NormalRequestMessage.class);
                        // Form the JSON response by calling repairBlockChain()
                        json_response = repairBlockChain(message);
                        System.out.println("Setting response to " + response);
                        // Reply the JSON response to the client
                        out.println(json_response);
                        out.flush();
                    }; break;
                    case 6:{
                        flag = false;
                    }; break;
                    default:{
                        System.out.println("Incorrect submission.");
                    }; break;
                }
                // Flush to client socket
                out.flush();
            }
        }
        // Handle IO exceptions
        catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
        }
        // Always close the socket
        finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                // ignore exception on close
            }
        }
    }

    /**
     Returns a JSON response message containing the current status of the blockchain.
     @param operation An integer representing the operation code.
     @return A String containing the JSON response message.
     */
    public static String viewBlockChainStatus(int operation) {

        // Create a StatusResponseMessage object
        ResponseMessage message = new StatusResponseMessage(
                operation,
                blockChain.getChainSize(),
                blockChain.getChainHash(),
                (int) blockChain.getTotalExpectedHashes(),
                blockChain.getTotalDifficulty(),
                blockChain.getLatestBlock().getNonce(),
                blockChain.getLatestBlock().getDifficulty(),
                blockChain.getHashesPerSecond());

        // Convert the object to JSON
        json_response = gson.toJson(message);
        // JSON response to client
        return json_response;
    }

    /**
     Adds a transaction to the blockchain by creating a new block with the provided transaction data and difficulty,
     setting the previous hash of the new block to be the chain hash, and adding the block to the blockchain.
     Calculates the execution time to add the block and creates a response string with this information.
     @param message An AddRequestMessage object containing the transaction data and difficulty.
     @return A JSON response containing a NormalResponseMessage object with the operation and response string.
     */
    public static String addTransaction(AddRequestMessage message) {

        // Create new Block
        Block newBlock = new Block(blockChain.getChainSize(), blockChain.getTime(),
                message.transactionData, message.difficulty);

        // Set previous hash of the new Block to be the chain hash
        newBlock.setPreviousHash(blockChain.getChainHash());
        
        Timestamp t1 = blockChain.getTime();
        // Add Block to BlockChain
        blockChain.addBlock(newBlock);
        Timestamp t2 = blockChain.getTime();
        double timeDifference = t2.getTime() - t1.getTime();

        // Create response string
        response = "Total execution time to add this block was " + (int)timeDifference + " milliseconds";
        System.out.println("Setting response to " + response);
        // Create a NormalResponseMessage
        responseMessage = new NormalResponseMessage(message.operation, response);
        json_response = gson.toJson(responseMessage);
        return json_response;
    }

    /**

     Verifies the validity of the blockchain and generates a response message indicating the result
     and the time taken to verify.
     @param message A NormalRequestMessage object containing the request operation and message.
     @return A JSON response message containing the operation, the time taken to verify the chain,
     and the verification result.
     */
    public static String verifyBlockChain(NormalRequestMessage message) {

        Timestamp t1 = blockChain.getTime();
        // Compute chain verification result
        String chainVerificationResult = blockChain.isChainValid();
        Timestamp t2 = blockChain.getTime();
        double timeDifference = t2.getTime() - t1.getTime();

        // If the result of chain verification is true
        if (chainVerificationResult.equals("TRUE")) {
            System.out.println("Chain verification: " + chainVerificationResult);
        }
        // If the result of chain verification is false
        else {
            System.out.println("Chain verification: FALSE");
            System.out.println(chainVerificationResult);
        }
        // Define response message
        response = "Total execution time to verify the chain was " + (int)timeDifference + " milliseconds";
        // Display time required to verify to user
        System.out.println("Total execution time required to verify the chain was " + (int)timeDifference + " milliseconds");
        // Display response to user
        System.out.println("Setting response to " + response);
        // Create a VerificationResponseMessage
        responseMessage = new VerificationResponseMessage(message.operation, response, chainVerificationResult);
        json_response = gson.toJson(responseMessage);
        return json_response;
    }

    /***
     * Function to view blockchain
     * @return JSON response
     */
    public static String viewBlockChain() {
        // Convert blockChain object to JSON string format
        json_response = blockChain.toString(); // would be a json message
        return json_response;
    }

    /**
     Corrupts a block in the blockchain by changing its data.
     @param message a CorruptRequestMessage containing the block ID of the block to be corrupted and the new data to be stored in the block.
     @return a JSON response message containing information about the corrupted block.
     */
    public static String corruptBlockChain(CorruptRequestMessage message) {
        // Stores block ID of the Block to corrupt
        int blockID = message.blockID;
        // Stores corrupted data to be stored in the Block
        String newData = message.data;
        // Corrupt block
        blockChain.getBlock(blockID).setData(newData);
        // Define response message
        response = "Block " + blockID + " now holds " + blockChain.getBlock(blockID).getData();
        System.out.println(response);
        // Create a NormalResponseMessage
        responseMessage = new NormalResponseMessage(message.operation, response);
        json_response = gson.toJson(responseMessage);
        return json_response;
    }

    /**

     Repairs the blockchain by removing any blocks that do not have valid previous hashes,
     and re-computing the hashes and difficulties of the remaining blocks.
     @param message a NormalRequestMessage object representing the request message from the client
     @return a JSON string containing a NormalResponseMessage object with the response message to the client
     */
    public static String repairBlockChain(NormalRequestMessage message) {

        Timestamp t1 = blockChain.getTime();
        // Repair block chain
        blockChain.repairChain();
        Timestamp t2 = blockChain.getTime();
        double timeDifference = t2.getTime() - t1.getTime();
        // Define response message
        response = "Total execution time required to repair the chain was " + (int)timeDifference + " milliseconds";
        // Create a NormalResponseMessage
        responseMessage = new NormalResponseMessage(message.operation, response);
        json_response = gson.toJson(responseMessage);
        return json_response;
    }
}
