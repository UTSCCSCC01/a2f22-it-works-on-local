package ca.utoronto.utm.mcs;

import com.mongodb.client.MongoCollection;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class MongoDao {

	public MongoCollection<Document> collection;
	MongoDatabase mongoDatabase;
	MongoClient mongoClient;

	public MongoDao() {
        // TODO: 
        // Connect to the mongodb database and create the database and collection. 
        // Use Dotenv like in the DAOs of the other microservices.
		MongoClientURI uri = new MongoClientURI("mongodb://root:123456@mongodb/?authSource=admin");
		MongoClient mongoClient = new MongoClient(uri);
		this.mongoDatabase = mongoClient.getDatabase("trip");
		this.collection =  mongoClient.getDatabase("trip").getCollection("trips");
	}

	// *** implement database operations here *** //

}
