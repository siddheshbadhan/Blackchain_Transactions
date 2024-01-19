/**
 * Author: Siddhesh Badhan
 * Andrew ID: sbadhan
 * Last Modified: 03/17/2023
 *
 * This Java file is a component of the BlockChain and serves as a Block object.
 * It contains a constructor and methods to calculate hashes, compute proof of work,
 * and convert the Block object to a JSON string.
 */

package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Block {

    // Stores the index of the Block in the Blockchain
    private int index;
    // Stores the time when the Block was created
    private java.sql.Timestamp timeStamp;
    // Stores the transaction on the Block
    private java.lang.String data;
    // Stores the SHA256 hash of a block's parent. This is also called a hash pointer
    private java.lang.String previousHash;
    private java.math.BigInteger nonce;
    // Stores the minimum number of left most hex digits needed by a proper hash.
    private int difficulty;


    // Constructor to initialise the values of the instance variables of the Block class
    Block (int index, java.sql.Timestamp timeStamp, java.lang.String data, int difficulty) {
        this.index = index;
        this.timeStamp = timeStamp;
        this.data = data;
        this.difficulty = difficulty;
        this.nonce = BigInteger.valueOf(0);
    }

    /**
     Calculates the SHA-256 hash of the block using its index, timestamp, data, previous hash, nonce and difficulty
     @return the SHA-256 hash of the block in String format
     */

    public String calculateHash() {

        // String whose hash is to be found
        String hashInput = index + timeStamp.toString() + data + previousHash + nonce + difficulty;

        byte[] hashedBytes = new byte[0];
        try {
            // Access MessageDigest class for SHA256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Compute the digest
            byte[] inputBytes = hashInput.getBytes();
            // Hash the input byte array and store the result in another byte array
            hashedBytes = md.digest(inputBytes);
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("No SHA-256 available" + e);
        }
        // Return the SHA256 hash in String form
        return bytesToHex(hashedBytes);
    }

    /**

     Computes the proof of work for the current block by finding a nonce that
     results in a hash with a specified number of leading zeros.
     @return The SHA-256 hash of the block with the correct proof of work
     */

     public String proofOfWork() {
        String leadingZeros = "0".repeat(difficulty);
        String hash;
        while (true) {
            hash = calculateHash();
            if (hash.startsWith(leadingZeros)) {
                break;
            }
            nonce = nonce.add(BigInteger.ONE);
        }
        return hash;
    }


    /***
     * This function overrides Java's toString method to convert the object of the Block
     * class to a JSON string
     * @return A JSON representation of all of this block's data is returned
     */
    public java.lang.String toString() {
        // Source to format date in Gson:
        // http://www.java2s.com/example/java-api/com/google/gson/gsonbuilder/setdateformat-1-18.html
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
        // Serialize to JSON
        return gson.toJson(this);
    }

    // Code to convert from byte array to hexadecimal String
    // Source: https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     Converts a byte array to its corresponding hexadecimal string representation.
     @param bytes the byte array to be converted to hexadecimal string
     @return the hexadecimal string representation of the input byte array
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


    /***
     * Function to get transaction details of Block
     * @return Transaction details of Block
     */
    public String getData() {
        return data;
    }

    /***
     * Function to get difficulty of Block
     * @return Difficulty of Block
     */
    public int getDifficulty() {
        return difficulty;
    }

    /***
     * Function to get index of Block
     * @return Index of Block
     */
    public int getIndex() {
        return index;
    }

    /***
     * Function to get nonce of Block
     * @return Nonce of Block
     */
    public BigInteger getNonce() {
        return nonce;
    }

    /***
     * Function to get hash of parent of Block
     * @return Hash of parent of Block
     */
    public String getPreviousHash() {
        return previousHash;
    }

    /***
     * Function to get timestamp of creation of Block
     * @return Time of creation of Block
     */
    public java.sql.Timestamp getTimestamp() {
        return timeStamp;
    }

    /***
     * Function to set transaction details of Block
     * @param data Transaction details of Block
     */
    public void setData(java.lang.String data) {
        this.data = data;
    }

    /***
     * Function to set difficulty of Block
     * @param difficulty Difficulty of Block
     */
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    /***
     * Function to set nonce of Block
     * @param nonce Nonce of Block
     */
    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    /***
     * Function to set index of Block
     * @param index Index of Block
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /***
     * Function to set hash of parent of Block
     * @param previousHash Hash of parent of Block
     */
    public void setPreviousHash(java.lang.String previousHash) {
        this.previousHash = previousHash;
    }

    /***
     * Function to set time of creation of Block
     * @param timestamp Time of creation of Block
     */
    public void setTimestamp(java.sql.Timestamp timestamp) {
        this.timeStamp = timestamp;
    }
}
