/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.fabcar;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;
import java.util.stream.Collectors;

/**
 * Java implementation of the Fabric Car Contract described in the Writing Your
 * First Application tutorial
 */
@Contract(
        name = "FabCar",
        info = @Info(
                title = "FabCar contract",
                description = "The hyperlegendary car contract",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "f.carr@example.com",
                        name = "F Carr",
                        url = "https://hyperledger.example.com")))
@Default
public final class FabCar implements ContractInterface {

    private final Genson genson = new Genson();

    private enum HashFileErrors {
        FILE_NOT_FOUND,
        FILE_ALREADY_EXISTS
    }

    /**
     * Retrieves a file with the specified key from the ledger.
     *
     * @param ctx the transaction context
     * @param key the key
     * @return the File found on the ledger if there was one
     */
    @Transaction()
    public String queryFile(final Context ctx, final String key) {
        ChaincodeStub stub = ctx.getStub();
        String fileState = stub.getStringState(key);

        if (fileState.isEmpty()) {
            String errorMessage = String.format("File %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, HashFileErrors.FILE_NOT_FOUND.toString());
        }

        String file = genson.serialize(fileState);

        return file;
    }

    /**
     * Creates some initial Cars on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction()
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        String[] fileData = {};

        for (int i = 0; i < fileData.length; i++) {
            String key = String.format("%d", i);

            HashFile file = genson.deserialize(fileData[i], HashFile.class);
            String fileState = genson.serialize(file);
            stub.putStringState(key, fileState);
        }
    }

    /**
     * Creates a new car on the ledger.
     *
     * @param ctx the transaction context
     * @param key the key for the new car
     * @param filename the filename of the new car
     * @param username the username of the new car
     * @param filehash the filehash of the new car
     * @param filedate the filedate of the new car
     * @return the created Car
     */
    @Transaction()
    public HashFile createHashFile(final Context ctx, final String key, final String filename, final String username,
                              final String filehash, final String filedate) {
        ChaincodeStub stub = ctx.getStub();

        String fileState = stub.getStringState(key);
        if (!fileState.isEmpty()) {
            String errorMessage = String.format("FileHash %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, HashFileErrors.FILE_ALREADY_EXISTS.toString());
        }
        System.out.println("파일명 :" + filename + "유저명 :" + username + "시간 : " + filedate + "해쉬 :" + filehash);
        HashFile file = new HashFile(filehash, filedate, filename, username);
        fileState = genson.serialize(file);
        stub.putStringState(key, fileState);

        return file;
    }

    /**
     * Retrieves all cars from the ledger.
     *
     * @param ctx the transaction context
     * @return array of Cars found on the ledger
     */
    @Transaction()
    public String queryAllHashFile(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<HashFileQueryResult> queryResults = new ArrayList<>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            HashFile file = genson.deserialize(result.getStringValue(), HashFile.class);
            queryResults.add(new HashFileQueryResult(result.getKey(), file));
        }

        final String response = genson.serialize(queryResults);

        return response;
    }
    @Transaction()
    public String queryUser(final Context ctx, final String username) {
        ChaincodeStub stub = ctx.getStub();

        List<HashFileQueryResult> queryResults = new ArrayList<>();
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result : results) { // (데이터형 접근변수명 : 배열)
            String hotelState = result.getStringValue();

            if (hotelState.isEmpty()) {
                String errorMessage = String.format("%s 아이디로 등록된 문서가 없습니다.", username);
                System.out.println(errorMessage);
                throw new ChaincodeException(errorMessage, HashFileErrors.FILE_NOT_FOUND.toString());
            }
            HashFile hashFile = genson.deserialize(hotelState, HashFile.class);
            queryResults.add(new HashFileQueryResult(result.getKey(), hashFile));
        }

        List<HashFileQueryResult> matchingResults = queryResults.stream()
                .filter(hotelQueryResult -> hotelQueryResult.getRecord().getusername().equals(username))
                .collect(Collectors.toList());
        System.out.println(queryResults);

        if (matchingResults.isEmpty()) {
            String errorMessage = String.format("%s 아이디로 등록된 문서가 없습니다.", username);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, HashFileErrors.FILE_NOT_FOUND.toString());
        }

        final String response = genson.serialize(matchingResults);
        return response;
    }

    /**
     * Changes the filehash of a car on the ledger.
     *
     * @param ctx the transaction context
     * @param key the key
     * @param newfilehash the new filehash
     * @return the updated Car
     */
    @Transaction()
    public HashFile changeFileHash(final Context ctx, final String key, final String newfilehash) {
        ChaincodeStub stub = ctx.getStub();

        String fileState = stub.getStringState(key);

        if (fileState.isEmpty()) {
            String errorMessage = String.format("File %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, HashFileErrors.FILE_NOT_FOUND.toString());
        }

        HashFile file = genson.deserialize(fileState, HashFile.class);

        HashFile newFile = new HashFile(file.getfilename(), file.getusername(), file.getfiledate(), newfilehash);
        String newFileState = genson.serialize(newFile);
        stub.putStringState(key, newFileState);

        return newFile;
    }
}
