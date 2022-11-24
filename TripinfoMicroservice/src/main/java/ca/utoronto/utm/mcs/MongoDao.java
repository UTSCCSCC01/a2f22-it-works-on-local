package ca.utoronto.utm.mcs;

import com.mongodb.client.MongoCollection;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


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
		String uri = String.format("mongodb://%s:%s@", username, password) + addr + ":27017";
		this.mongoDatabase = mongoClient.getDatabase("trip");
		this.collection = mongoClient.getDatabase("trip").getCollection("trips");
	}

	// *** implement database operations here *** //

}
