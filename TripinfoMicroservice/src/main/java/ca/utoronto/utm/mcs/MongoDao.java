package ca.utoronto.utm.mcs;

import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import com.mongodb.client.MongoCollection;


public class MongoDao {

	public MongoCollection<Document> collection;
	private final String username = "root";
	private final String password = "123456";
	MongoDatabase mongoDatabase;
	MongoClient mongoClient;

	public MongoDao() {
		// TODO:
		// Connect to the mongodb database and create the database and collection.
		// Use Dotenv like in the DAOs of the other microservices.
		Dotenv dotenv = Dotenv.load();
		String addr = dotenv.get("MONGODB_ADDR");
		String uri = String.format("mongodb://%s:%s@", username, password) +"localhost"+":27017";
		this.mongoClient = new MongoClient("localhost", 27017);
		this.mongoDatabase = mongoClient.getDatabase("trip");
		this.collection =  mongoClient.getDatabase("trip").getCollection("trips");
	}

	// *** implement database operations here *** //

}