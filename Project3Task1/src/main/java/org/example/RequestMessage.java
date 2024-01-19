/**
 * Author: Siddhesh Badhan
 * Andrew ID: sbadhan
 * Last Modified: 03/17/2023
 */

package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 This class represents a request message that is sent from a client to a server.
 It contains an integer value to represent the type of operation being requested.
 It has a method to convert the object to a JSON string format using Gson library.
 */
public class RequestMessage {
    protected int operation;
    /**
     Converts the RequestMessage object to a JSON string format using Gson library.
     @return a string in JSON format representing the RequestMessage object
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

 A subclass of RequestMessage that represents a normal request message.
 */
class NormalRequestMessage extends RequestMessage {
    /**
     Constructs a NormalRequestMessage object with the given operation.
     @param operation an integer value representing the operation of the request message
     */
    NormalRequestMessage (int operation) {
        super.operation = operation;
    }

}

/**
 Represents a request message for adding a new block to the blockchain.
 Inherits from the RequestMessage class and adds additional variables to store
 the difficulty of the new block and the transaction data to be stored on the new block.
 */
class AddRequestMessage extends RequestMessage {
    int difficulty;
    String transactionData;
    /**
     Constructor to initialize the values of the instance variables.
     @param operation The operation code for the message
     @param difficulty The difficulty of the new block
     @param transactionData The transaction data to be stored on the new block
     */
    AddRequestMessage (int operation, int difficulty, String transactionData) {
        super.operation = operation;
        this.difficulty = difficulty;
        this.transactionData = transactionData;
    }
}

/**
 This class represents a Corrupt Request message,
 which is used to request the corruption of a specific Block in the blockchain.
 It extends the RequestMessage class, and has additional instance variables
 for the blockID of the Block to be corrupted and the new (corrupted) transaction data to be stored on that block.
 */
class CorruptRequestMessage extends RequestMessage {
    int blockID;
    String data;
    /**
     Constructor to initialize the values of the instance variables.
     @param operation The operation code for the request message.
     @param blockID The blockID of the Block to be corrupted.
     @param data The new (corrupted) transaction data to be stored on the blockID.
     */
    CorruptRequestMessage (int operation, int blockID, String data) {
        super.operation = operation;
        this.blockID = blockID;
        this.data = data;
    }
}
