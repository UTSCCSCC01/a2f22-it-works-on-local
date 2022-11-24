package ca.utoronto.utm.mcs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.neo4j.driver.Record;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Neo4jDAO {

    private final Session session;
    private final Driver driver;
    private final String username = "neo4j";
    private final String password = "123456";

    public Neo4jDAO() {
        Dotenv dotenv = Dotenv.load();
        String addr = dotenv.get("NEO4J_ADDR");
        String uriDb = "bolt://" + addr + ":7687";

        this.driver = GraphDatabase.driver(uriDb, AuthTokens.basic(this.username, this.password));
        this.session = this.driver.session();
    }

    // *** implement database operations here *** //

    public Result addUser(String uid, boolean is_driver) {
        String query = "CREATE (n: user {uid: '%s', is_driver: %b, longitude: 0, latitude: 0, street: ''}) RETURN n";
        query = String.format(query, uid, is_driver);
        return this.session.run(query);
    }

    public Result deleteUser(String uid) {
        String query = "MATCH (n: user {uid: '%s' }) DETACH DELETE n RETURN n";
        query = String.format(query, uid);
        return this.session.run(query);
    }

    public Result getUserLocationByUid(String uid) {
        String query = "MATCH (n: user {uid: '%s' }) RETURN n.longitude, n.latitude, n.street";
        query = String.format(query, uid);
        return this.session.run(query);
    }

    public Result getUserByUid(String uid) {
        String query = "MATCH (n: user {uid: '%s' }) RETURN n";
        query = String.format(query, uid);
        return this.session.run(query);
    }

    public Result updateUserIsDriver(String uid, boolean isDriver) {
        String query = "MATCH (n:user {uid: '%s'}) SET n.is_driver = %b RETURN n";
        query = String.format(query, uid, isDriver);
        return this.session.run(query);
    }

    public Result updateUserLocation(String uid, double longitude, double latitude, String street) {
        String query = "MATCH(n: user {uid: '%s'}) SET n.longitude = %f, n.latitude = %f, n.street = \"%s\" RETURN n";
        query = String.format(query, uid, longitude, latitude, street);
        return this.session.run(query);
    }

    public Result getRoad(String roadName) {
        String query = "MATCH (n :road) where n.name='%s' RETURN n";
        query = String.format(query, roadName);
        return this.session.run(query);
    }

    public Result createRoad(String roadName, boolean has_traffic) {
        String query = "CREATE (n: road {name: '%s', has_traffic: %b}) RETURN n";
        query = String.format(query, roadName, has_traffic);
        return this.session.run(query);
    }

    public Result updateRoad(String roadName, boolean has_traffic) {
        String query = "MATCH (n:road {name: '%s'}) SET n.has_traffic = %b RETURN n";
        query = String.format(query, roadName, has_traffic);
        return this.session.run(query);
    }

    public Result createRoute(String roadname1, String roadname2, int travel_time, boolean has_traffic) {
        String query = "MATCH (r1:road {name: '%s'}), (r2:road {name: '%s'}) CREATE (r1) -[r:ROUTE_TO {travel_time: %d, has_traffic: %b}]->(r2) RETURN type(r)";
        query = String.format(query, roadname1, roadname2, travel_time, has_traffic);
        return this.session.run(query);
    }

    public Result deleteRoute(String roadname1, String roadname2) {
        String query = "MATCH (r1:road {name: '%s'})-[r:ROUTE_TO]->(r2:road {name: '%s'}) DELETE r RETURN COUNT(r) AS numDeletedRoutes";
        query = String.format(query, roadname1, roadname2);
        return this.session.run(query);
    }

    public Result usersStreet(String driverUid, String passengerUid){
        String query = "MATCH (driver: user {uid: '%s'}), (passenger: user {uid: '%s'}) " +
                        "RETURN driver.street, passenger.street";
        query = String.format(query, driverUid, passengerUid);
        return this.session.run(query);
    }

    public int getTotalEdges(String driverStreet, String passengerStreet){
        String query = "MATCH p = shortestPath((driverStreet: road {name: '%s'})-[*]-(passengerStreet: road {name: '%s'}))" +
                "RETURN length(p)";
        query = String.format(query, driverStreet, passengerStreet);
        Result result = this.session.run(query);
        if(result.hasNext()){
            int value = Integer.parseInt(result.next().toString().replaceAll("[^0-9]", ""));
            return value;
        }
        return 0;
    }

    public int  getTotalTime(String driverStreet, String passengerStreet){
        if(driverStreet.equals(passengerStreet)){
            return 0;
        }
        String query = "MATCH p = shortestPath((driverStreet: road {name: '%s'})-[*]-(passengerStreet: road {name: '%s'})) WITH " +
                "[r in relationships(p) | r.travel_time] as totalTime " +
                "RETURN reduce(t=0, r in totalTime | t+r ) as totalTime";
        query = String.format(query, driverStreet, passengerStreet);
        Result result = this.session.run(query);
        if(result.hasNext()){
            int value = Integer.parseInt(result.next().toString().replaceAll("[^0-9]", ""));
            return value;
        }
        return 0;
    }

    public boolean validRoad(String street){
        Result result = getRoad(street);
        if(!result.hasNext()){
            return false;
        }
        return true;
    }

    public boolean validUser(String uid){
        Result result = getUserByUid(uid);
        if(!result.hasNext()){
            return false;
        }
        return true;
    }

    public ArrayList<JSONObject> getPath(String driverStreet, String passengerStreet) throws JSONException {
        ArrayList<JSONObject> route = new ArrayList<JSONObject>();
        if(!validRoad(driverStreet) || !validRoad(passengerStreet)){
            return null;
        }

        if(driverStreet.equals(passengerStreet)){
            JSONObject temp = new JSONObject();
            temp.put("time", 0);
            temp.put("street", driverStreet);

            String query = "MATCH (street: road {name: '%s'}) " +
                    "RETURN toLower(toString(street.has_traffic))";

            query = String.format(query, driverStreet);
            Result result = this.session.run(query);
            boolean [] has_traffic = new boolean[1];
            if (result.hasNext()) {
                List<Record> record = result.list();
                for (int i = 0; i < record.size(); i++) {
                    String val = record.get(i).values().toString();
                    String newVal = val.substring(2, val.length() - 2);
                    has_traffic[0] = Boolean.parseBoolean(newVal);
                }
            }
            temp.put("is_traffic", has_traffic[0]);
            route.add(temp);
            return route;
        }

        int value = getTotalEdges(driverStreet, passengerStreet);
        int [] travelTimes = new int [value + 1];
        boolean [] has_traffic = new boolean[value + 1];
        String[] road_name = new String[value + 1];
        JSONObject final_obj = new JSONObject();

        String query1 = "MATCH p = shortestPath((driverStreet: road {name: '%s'})-[*]-(passengerStreet: road {name: '%s'})) " +
                        "UNWIND relationships(p) as r " +
                        "RETURN r.travel_time";


        query1 = String.format(query1, driverStreet, passengerStreet);
        Result result1 = this.session.run(query1);
        if (result1.hasNext()) {
            List<Record> record = result1.list();
            for (int i = 0; i < record.size(); i++) {
                String val = record.get(i).values().toString();
                String newVal = val.substring(1, val.length() - 1);
                travelTimes[i] = Integer.parseInt(newVal);
            }
        }

        String query2 = "MATCH p = shortestPath((driverStreet: road {name: '%s'})-[*]-(passengerStreet: road {name: '%s'})) " +
                "UNWIND nodes(p) as n " +
                "RETURN toLower(toString(n.has_traffic))";

        query2 = String.format(query2, driverStreet, passengerStreet);
        Result result2 = this.session.run(query2);
        if (result2.hasNext()) {
            List<Record> record = result2.list();
            for (int i = 0; i < record.size(); i++) {
                String val = record.get(i).values().toString();
                String newVal = val.substring(2, val.length() - 2);
                has_traffic[i] = Boolean.parseBoolean(newVal);
            }
        }

        String query3 = "MATCH p = shortestPath((driverStreet: road {name: '%s'})-[*]-(passengerStreet: road {name: '%s'})) " +
                "UNWIND nodes(p) as n " +
                "RETURN n.name";

        query3 = String.format(query3, driverStreet, passengerStreet);
        Result result3 = this.session.run(query3);
        if (result3.hasNext()) {
            List<Record> record = result3.list();
            for (int i = 0; i < record.size(); i++) {
                String val = record.get(i).values().toString();
                String newVal = val.substring(2, val.length() - 2);
                road_name[i] = newVal;
            }
        }

        for(int i = 0; i < value + 1; i++){
            JSONObject temp_obj = new JSONObject();
            if(i == 0){
                temp_obj.put("street", road_name[i]);
                temp_obj.put("time", 0);
                temp_obj.put("is_traffic", has_traffic[i]);
            } else {
                temp_obj.put("street", road_name[i]);
                temp_obj.put("time", travelTimes[i-1]);
                temp_obj.put("is_traffic", has_traffic[i]);
            }
            route.add(temp_obj);
        }

        return route;
    }

    public Result getAllDriverUid() {
        String query = "MATCH (n :user) where n.is_driver=true RETURN n.uid";
        return this.session.run(query);
    }
}

