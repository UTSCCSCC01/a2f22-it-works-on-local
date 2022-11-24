package ca.utoronto.utm.mcs;

import com.mongodb.client.*;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;


public class MongoDao {

	public MongoCollection<Document> collection;
	private final String username = "root";
	private final String password = "123456";

	public MongoDao() {
		// TODO:
		// Connect to the mongodb database and create the database and collection.
		// Use Dotenv like in the DAOs of the other microservices.
		Dotenv dotenv = Dotenv.load();
		String addr = dotenv.get("MONGODB_ADDR");
		String uri = String.format("mongodb://%s:%s@", username, password) +"localhost"+":27017";
		MongoClient mongoClient = MongoClients.create(uri);
		MongoDatabase mongoDatabase = mongoClient.getDatabase("trip");
		this.collection =  mongoDatabase.getCollection("trips");
	}

	// *** implement database operations here *** //

	public ArrayList<String> getDriver(ObjectId id){
		try {
			FindIterable<Document> result = this.collection.find(Filters.eq("_id", id));
			if(result == null){
				return null;
			} else {
				ArrayList<String> arr = new ArrayList<>();
				for(Document res: result){
					arr.add(res.getString("driver"));
					arr.add(res.getString("passenger"));
					return arr;
				}
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}