/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.openedu.mongodb;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import java.net.UnknownHostException;

/**
 *
 * @author Rafael
 */
public enum MongoResource {

    INSTANCE;
    private MongoClient mongoClient;
    private static final String CONNECTION_STRING = "mongodb://admin:Ya$hica27O@ds053139.mongolab.com:53139/openedu";
    private static final String DBNAME = "openedu";

    private MongoResource() {
        try {
            if (mongoClient == null) {
                mongoClient = getClient();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private MongoClient getClient() {
        try {
            MongoClientURI uri = new MongoClientURI(CONNECTION_STRING);
            return new MongoClient(uri);
        } catch (UnknownHostException uh) {
            System.out.println(uh.getMessage());
        }
        return null;
    }

    public DB getDataBase() {
        return mongoClient.getDB(DBNAME);
    }

}
