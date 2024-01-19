/**
 * Author: Siddhesh Badhan
 * Andrew ID: sbadhan
 * Last Modified: 03/17/2023
 */


package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.math.BigInteger;

/**
 This class represents a Response Message object that can be used to send responses back to the user.
 The class contains an integer variable to store the selection number that the user selected.
 */
public class ResponseMessage {
    protected int choice;
    /**
     This method converts the ResponseMessage object to a JSON String using Gson.
     @return A JSON String representation of the ResponseMessage object.
     */
    public java.lang.String toString() {
        // Source to format date in Gson:
        // http://www.java2s.com/example/java-api/com/google/gson/gsonbuilder/setdateformat-1-18.html
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
        // Serialize to JSON
        return gson.toJson(this);
    }
}

/**
 This class represents a normal response message from the server.
 It extends the ResponseMessage class and adds a response String variable.
 */
class NormalResponseMessage extends ResponseMessage {
    String response;
    /**
     Constructor to initialize the values of the instance variables.
     @param choice the selection number that the user selected
     @param response the response from the server
     */
    NormalResponseMessage (int choice, String response) {
        super.choice = choice;
        this.response = response;
    }
}

/**
 Represents a response message sent by the server containing information about the status of the blockchain.
 Inherits from ResponseMessage class and contains instance variables related to blockchain's status.
 */
class StatusResponseMessage extends ResponseMessage {
    int chainSize;
    int totalHashes;
    int totalDifficulty;
    BigInteger recentNonce;
    int difficulty;
    int hashesPerSecond;
    String chainHash;
    /**
     Constructor to initialize the values of the instance variables
     @param choice The selection number of the response
     @param chainSize The size of the blockchain
     @param chainHash The hash of the blockchain
     @param totalHashes The total number of hashes computed in the blockchain
     @param totalDifficulty The total difficulty of the blockchain
     @param recentNonce The recent nonce computed in the blockchain
     @param difficulty The difficulty of the blockchain
     @param hashesPerSecond The number of hashes computed per second
     */
    StatusResponseMessage (int choice, int chainSize, String chainHash, int totalHashes, int totalDifficulty,
                           BigInteger recentNonce, int difficulty, int hashesPerSecond) {
        super.choice = choice;
        this.chainSize = chainSize;
        this.chainHash = chainHash;
        this.totalHashes = totalHashes;
        this.totalDifficulty = totalDifficulty;
        this.recentNonce = recentNonce;
        this.difficulty = difficulty;
        this.hashesPerSecond = hashesPerSecond;
    }
}

/**
 A subclass of ResponseMessage that represents a verification response message from the server.
 */
class VerificationResponseMessage extends ResponseMessage {
    String response;
    String verificationOp;
    /**
     Creates a new VerificationResponseMessage object with the specified choice, response, and verification operation.
     @param choice the selection number that the user selected
     @param response the response from the server for the verification request
     @param verificationOp the verification operation that was performed by the server
     */
    VerificationResponseMessage(int choice, String response, String verificationOp) {
        super.choice = choice;
        this.response = response;
        this.verificationOp = verificationOp;
    }
}