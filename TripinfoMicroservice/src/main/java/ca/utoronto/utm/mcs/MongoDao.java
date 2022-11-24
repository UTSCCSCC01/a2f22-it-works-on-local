package ca.utoronto.utm.mcs;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.*;
import com.mongodb.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.Date;


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
    
		String uri = String.format("mongodb://%s:%s@", username, password) + addr + ":27017";
		//String uri = String.format("mongodb://%s:%s@", username, password) + "localhost"+ ":27017";
		MongoClient mongoClient = MongoClients.create(uri);
		MongoDatabase mongoDatabase = mongoClient.getDatabase("trip");
		this.collection = mongoDatabase.getCollection("trips");
	}

	// *** implement database operations here *** //
	public ObjectId confirmTrip(String passenger, String driver, int startTime) {
		Document doc = new Document();

		doc.put("driver", driver);
		doc.put("passenger", passenger);
		doc.put("startTime", startTime);

		try {
			this.collection.insertOne(doc);
			ObjectId id = doc.getObjectId("_id");
			return id;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public FindIterable<Document> getTripById(String id) {

		try {
			Bson filter = Filters.eq("_id",id);
			FindIterable<Document> result = this.collection.find(filter);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean updateTripInfo(String id, double distance, int endTime, String timeElapsed, double discount, double totalCost, double driverPayout) {

		try {
			Bson filter = Filters.eq("_id", new ObjectId(id));
			Bson updates = Updates.combine(
					Updates.set("distance", distance),
					Updates.set("endTime", endTime),
					Updates.set("timeElapsed", timeElapsed),
					Updates.set("discount", discount),
					Updates.set("totalCost", totalCost),
					Updates.set("driverPayout", driverPayout));

			UpdateOptions options = new UpdateOptions().upsert(true);

			UpdateResult result = this.collection.updateOne(filter, updates, options);
			if(result.getModifiedCount() > 0) {
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public FindIterable<Document> getTripByPassengerId(String pid) {

		try {
			Bson filter = Filters.eq("passenger",pid);
			FindIterable<Document> result = this.collection.find(filter);
			Bson projection = Projections.fields(Projections.exclude("passenger"));
			return result.projection(projection);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public FindIterable<Document> getTripByDriverId(String did) {

		try {
			Bson filter = Filters.eq("driver",did);
			FindIterable<Document> result = this.collection.find(filter);
			Bson projection = Projections.fields(Projections.exclude("driver"));
			return result.projection(projection);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
