/**
 * Author: Siddhesh Badhan
 * Andrew ID: sbadhan
 * Last Modified: 03/17/2023
 *
 This Java file implements a BlockChain using Block objects and provides various functions to manage and
 verify the blockchain. The BlockChain constructor allows creation of a new blockchain, and functions to add
 a new block to the chain and compute the number of hashes per second are also available. Additionally,
 this file provides methods to retrieve blocks by index, get the latest block, get the time of a block,
 get the total difficulty and expected hashes for the blockchain. The file also includes functions to verify
 the integrity of the blockchain and to repair it if it becomes corrupted. Finally, it provides a method to
 convert the blockchain object into a JSON string.
 */

package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class BlockChain {

    // Stores the blocks of the BlockChain
    final ArrayList<Block> blkChain;
    // Stores the SHA256 hash of the most recently added Block
    String chainHash;
    // Stores the approximate number of hashes per second on this computer
    transient int hashesPerSecond;

    /**
     Constructor for the BlockChain class. Initializes an empty ArrayList of Block objects
     and sets the chain hash to an empty string, and the hashes per second to 0.
     */
    BlockChain() {
        blkChain = new ArrayList<>();
        chainHash = "";
        hashesPerSecond = 0;
    }

    /***
     * Function to add a new Block to the BlockChain
     * @param newBlock Block to be added to the BlockChain
     */
    public void addBlock(Block newBlock) {
        // Update chainHash to be the hash of the new block that is being added
        chainHash = newBlock.proofOfWork();
        // Add new block to the array list
        blkChain.add(newBlock);
    }

    /**

     Computes the number of hashes per second that can be generated by the system.
     The method calculates the time it takes to compute 2 million SHA-256 hashes of the string "00000000",
     and computes the hashes per second based on that time.
     */
    public void computeHashesPerSecond() {
        Timestamp t1 = getTime();
        // Compute hashes for the string "00000000" for 2 million times
        for (int i = 1; i <= 2000000; i++) {
            computeSHA256("00000000");
        }
        Timestamp t2 = getTime();
        double timeDifference = t2.getTime() - t1.getTime();
        hashesPerSecond = (int) (2000000/timeDifference);
    }

    /**

     Returns the Block at the specified index in the blockchain.
     @param i the index of the Block to retrieve
     @return the Block at the specified index
     */
    public Block getBlock(int i) {
        return blkChain.get(i);
    }

    /**

     Returns the current hash value of the blockchain.
     @return a string representing the current hash value of the blockchain
     */
    public String getChainHash() {
        return chainHash;
    }

    /***
     * Function to get the size of the chain in blocks
     * @return The size of the chain in blocks
     */
    public int getChainSize() {
        return blkChain.size();
    }

    /***
     * Function to get hashes per second
     * @return The instance variable approximating the number of hashes per second
     */
    public int getHashesPerSecond() {
        return hashesPerSecond;
    }

    /***
     * Function to get a reference to the most recently added Block
     * @return A reference to the most recently added Block
     */
    public Block getLatestBlock() {
        return blkChain.get(blkChain.size() - 1);
    }

    /***
     * Function to get the current system time
     * @return The current system time
     */
    public static java.sql.Timestamp getTime() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**

     Calculates the total difficulty of the blockchain by summing up the difficulty of each block in the blockchain.
     @return the total difficulty of the blockchain.
     */
    public int getTotalDifficulty() {

        int total = 0;
        for (int i = 0; i < blkChain.size(); i++) {
            total = total + blkChain.get(i).getDifficulty();
        }
        return total;
    }

    /***
     * Function to compute and return the expected number of hashes required for the entire chain
     * @return The total expected hashes for the blockchain
     */
    public double getTotalExpectedHashes() {

        // Stores the total expected hashes for the chain
        double totalHashes = 0;
        // Loop over all the blocks in the blockchain and increment the
        // count of the total expected hashes
        for (int i = 0; i < blkChain.size(); i++) {
            totalHashes = totalHashes + Math.pow(16, blkChain.get(i).getDifficulty());
        }
        // The total expected hashes for the chain
        return totalHashes;
    }

    /**

     Checks the validity of the blockchain by verifying the hash of each block and its link to the previous block.
     @return a string indicating whether the chain is valid or not. Returns "TRUE" if the chain is valid, otherwise
     returns an error message detailing the issue found in the chain.
     */
    public String isChainValid() {
        for (int i = 0; i < blkChain.size(); i++) {
            Block currentBlock = blkChain.get(i);
            String hashInput = currentBlock.getIndex() + currentBlock.getTimestamp().toString() +
                    currentBlock.getData() + currentBlock.getPreviousHash() +
                    currentBlock.getNonce() + currentBlock.getDifficulty();
            String hash = computeSHA256(hashInput);
            String leadingZeros = "0".repeat(currentBlock.getDifficulty());

            if (!hash.startsWith(leadingZeros)) {
                return "Improper hash on node " + i + ". Does not begin with " + leadingZeros;
            }

            if (i < blkChain.size() - 1 && !hash.equals(blkChain.get(i + 1).getPreviousHash())) {
                return "Hash of Block " + i + " does not match with previous hash of Block " + (i + 1);
            }

            if (i == blkChain.size() - 1 && !hash.equals(chainHash)) {
                return "Hash of the last Block (Block " + i + ") does not match with Chain Hash!";
            }
        }
        return "TRUE";
    }


    /**

     Repairs the blockchain by recomputing the hash values and nonce of each block
     and ensuring that the chain is properly connected by updating previous hash values.
     If a block's hash value does not meet the required leading zeros determined by its difficulty,
     the block's nonce will be incremented until a hash value with the required leading zeros is found.
     After the chain is repaired, the method updates the chainHash field to the hash value of the last block.
     */

    public void repairChain() {
        for (Block block : blkChain) {
            String hashInput = block.getIndex() + block.getTimestamp().toString()
                    + block.getData() + block.getPreviousHash() + block.getNonce() + block.getDifficulty();

            String hash = computeSHA256(hashInput);
            String leadingZeros = "0".repeat(block.getDifficulty());

            if (blkChain.size() == 1 && !block.getPreviousHash().equals("")) {
                block.setPreviousHash("");
            }

            if (!hash.startsWith(leadingZeros)) {
                block.setNonce(BigInteger.ZERO);
                while (true) {
                    String newHashInput = block.getIndex() + block.getTimestamp().toString()
                            + block.getData() + block.getPreviousHash() + block.getNonce() + block.getDifficulty();

                    hash = computeSHA256(newHashInput);
                    if (hash.startsWith(leadingZeros)) {
                        break;
                    }
                    block.setNonce(block.getNonce().add(BigInteger.ONE));
                }
            }

            hashInput = block.getIndex() + block.getTimestamp().toString()
                    + block.getData() + block.getPreviousHash() + block.getNonce() + block.getDifficulty();

            hash = computeSHA256(hashInput);

            int index = blkChain.indexOf(block);
            if (blkChain.size() > 1) {
                if (index < blkChain.size() - 1 && !hash.equals(blkChain.get(index + 1).getPreviousHash())) {
                    blkChain.get(index + 1).setPreviousHash(hash);
                } else if (index == blkChain.size() - 1 && !hash.equals(chainHash)) {
                    chainHash = hash;
                }
            } else if (!hash.equals(chainHash)) {
                chainHash = hash;
            }
        }
    }

    /***
     * This method uses the toString method defined on each individual block
     * to convert the BlockChain object to a JSON string
     * @return A String representation of the entire chain
     */
    public java.lang.String toString() {
        // Source to format date in Gson:
        // http://www.java2s.com/example/java-api/com/google/gson/gsonbuilder/setdateformat-1-18.html
        // Create a Gson object
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
        // Serialize to JSON
        return gson.toJson(this);
    }

    /**

     This method computes the SHA256 hash of a given input string and returns the hash in String form.
     @param input The input string to be hashed
     @return The SHA256 hash of the input string in String form
     */

    public String computeSHA256(String input) {
        byte[] hashedBytes = new byte[0];
        try {
            // Access MessageDigest class for SHA256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Compute the digest
            byte[] inputBytes = input.getBytes();
            // Hash the input byte array and store the result in another byte array
            hashedBytes = md.digest(inputBytes);
        }
        // Handles No SHA-256 Algorithm exceptions
        catch (NoSuchAlgorithmException e) {
            // Print error message in console
            System.out.println("No SHA-256 available" + e);
        }
        // Return the SHA256 hash in String form
        return bytesToHex(hashedBytes);
    }

    // Code to convert from byte array to hexadecimal String
    // Source: https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**

     Converts a byte array to a hexadecimal string representation.
     @param bytes the byte array to be converted
     @return the hexadecimal string representation of the byte array
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int v = b & 0xFF;
            sb.append(HEX_ARRAY[v >>> 4]);
            sb.append(HEX_ARRAY[v & 0x0F]);
        }
        return sb.toString();
    }


    public static void main(String[] args) {

        // Create a new object of the BlockChain
        BlockChain blockChain = new BlockChain();
        // Create the first Block, called the genesis Block
        Block genesis = new Block(0, getTime(), "Genesis", 2);
        // Set the previous hash of the genesis block to be an empty String
        genesis.setPreviousHash("");
        // Compute the hashes per second on this system
        blockChain.computeHashesPerSecond();
        // Update chain hash by the hash of the genesis Block
        blockChain.chainHash = genesis.proofOfWork();
        // add Genesis block to chain
        blockChain.blkChain.add(genesis);

        // Prompt to the user
        System.out.println("0. View basic blockchain status.\n" +
                "1. Add a transaction to the blockchain.\n" +
                "2. Verify the blockchain.\n" +
                "3. View the blockchain.\n" +
                "4. Corrupt the chain.\n" +
                "5. Hide the corruption by repairing the chain.\n" +
                "6. Exit");

        // Create a Scanner object
        Scanner s = new Scanner(System.in);
        // Get first input from the user
        int userInput = s.nextInt();
        // Until the user does not input 6
        while (userInput != 6) {

            switch(userInput){
                case 0:{
                    // Displays the necessary details
                    System.out.println("Current size of chain: " + blockChain.getChainSize());
                    System.out.println("Difficulty of most recent block: " + blockChain.getLatestBlock().getDifficulty());
                    System.out.println("Total difficulty for all blocks: " + blockChain.getTotalDifficulty());
                    System.out.println("Approximate hashes per second on this machine: " + blockChain.getHashesPerSecond());
                    System.out.println("Expected total hashes required for the whole chain: " + String.format("%.6f", blockChain.getTotalExpectedHashes()));
                    System.out.println("Nonce for most recent block: " + blockChain.getLatestBlock().getNonce());
                    System.out.println("Chain hash: " + blockChain.getChainHash());
                }; break;
                case 1:{
                    System.out.println("Enter difficulty > 0");
                    int difficulty = s.nextInt();
                    System.out.println("Enter transaction");
                    s.nextLine();
                    String transaction = s.nextLine();
                    Block newBlock = new Block(blockChain.getChainSize(), getTime(), transaction, difficulty);
                    // Set previous hash of the new Block to be the chain hash
                    newBlock.setPreviousHash(blockChain.getChainHash());
                    Timestamp t1 = getTime();
                    // Add Block to BlockChain
                    blockChain.addBlock(newBlock);
                    Timestamp t2 = getTime();
                    double timeDifference = t2.getTime() - t1.getTime();
                    System.out.println("Total execution time to add this block was " + (int)timeDifference + " milliseconds");
                }; break;
                case 2:{
                    Timestamp t1 = getTime();
                    // Compute chain verification result
                    String chainVerificationResult = blockChain.isChainValid();
                    Timestamp t2 = getTime();
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
                    System.out.println("Total execution time to verify the chain was " + (int)timeDifference + " milliseconds");
                }; break;
                case 3:{
                    System.out.println("View the Blockchain");
                    System.out.println(blockChain);
                }; break;
                case 4:{
                    System.out.println("Corrupt the Blockchain");
                    System.out.println("Enter block ID of block to corrupt");
                    int blockID = s.nextInt();
                    System.out.println("Enter new data for block " + blockID);
                    s.nextLine();
                    String newData = s.nextLine();
                    // Update new data of Block in the chain
                    blockChain.getBlock(blockID).setData(newData);
                    // Update user about the corruption
                    System.out.println("Block " + blockID + " now holds " + blockChain.getBlock(blockID).getData());
                }; break;
                case 5:{
                    Timestamp t1 = getTime();
                    // Repair block chain
                    blockChain.repairChain();
                    Timestamp t2 = getTime();
                    double timeDifference = t2.getTime() - t1.getTime();
                    System.out.println("Total execution time required to repair the chain was " + (int)timeDifference + " milliseconds");
                }; break;
                default:{
                    System.out.println("Incorrect submission. Client closing.");
                };break;
            }

            // Prompt to user for next input
            System.out.println("0. View basic blockchain status.\n" +
                    "1. Add a transaction to the blockchain.\n" +
                    "2. Verify the blockchain.\n" +
                    "3. View the blockchain.\n" +
                    "4. Corrupt the chain.\n" +
                    "5. Hide the corruption by repairing the chain.\n" +
                    "6. Exit");

            // Get next input from user
            userInput = s.nextInt();
        }


        /*
        Analysis

        Increasing the difficulty of new blocks results in a significant increase in the time required
        to add blocks using the addBlock() method, with an exponential increase observed in the approximate
        time taken as the difficulty increases. However, the time required to verify the chain using the
        isChainValid() method remains almost constant, regardless of the maximum difficulty present in the blockchain.
        Conversely, the time required for repairing the chain using chainRepair() increases substantially with
        increasing difficulties.


        1. For addBlock():

        - As the difficulty of adding new blocks to the blockchain increases, the time required for this process
        also increases exponentially. This is due to the increasing difficulty of finding the proof of work and
        generating a valid hash, which becomes increasingly challenging for the system.
        The difficulty vs time taken are as follows:
        1 - 1 milliseconds
        2 - 5 milliseconds
        3 - 30 milliseconds
        4 - 58 milliseconds
        5 - 257 milliseconds
        6 - 22556 milliseconds

        2. For isChainValid():

        - In terms of validating the blockchain, the time it takes to determine whether the chain is
        valid remains relatively constant as the maximum difficulty level increases from 1 to 6.
        This is due to the fact that the proof of work does not need to be computed with increasing difficulty
        levels, only a hash needs to be found and compared with the difficulty level, making it a simpler process.


        3. For chainRepair():

        -	The approximate time it takes to repair a corrupted blockchain using chainRepair() significantly
        increases as the maximum difficulty of the blocks in the blockchain increases. Similar to addBlock(),
        there is an exponential increase in the time required to repair the chain as the maximum difficulty increases.
        The difficulty vs time taken are as follows:
        1 - 1 milliseconds
        2 - 3 milliseconds
        3 - 9 milliseconds
        4 - 11 milliseconds
        5 - 457 milliseconds
        6 - 2527 milliseconds
         */
    }
}