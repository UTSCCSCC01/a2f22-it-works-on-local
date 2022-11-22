package ca.utoronto.utm.mcs;

import java.io.IOException;
import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

public class Nearby extends Endpoint {
    
    /**
     * GET /location/nearbyDriver/:uid?radius=:radius
     * @param uid, radius
     * @return 200, 400, 404, 500
     * Get drivers that are within a certain radius around a user.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        // TODO
        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[2].isEmpty() || params[3].isEmpty())  {
            this.sendStatus(r, 400);
            return;
        }

        try {
            Double user_long = 0.0, user_lat = 0.0, longitude = 0.0, latitude = 0.0, driver_long = 0.0, driver_lat = 0.0, distance = 0.0;
            boolean flag = false;
            String uid = params[2];
            double radius  = Double.parseDouble(params[3]);
            Result users = this.dao.getUserByUid(uid);
            if(users.hasNext()) {
                Record user = users.next();

                user_long = (user.get("n.longitude").asDouble()) / (180/Math.PI);
                user_lat = (user.get("n.latitude").asDouble()) / (180/Math.PI);
            } else {
                this.sendStatus(r, 404);
            }
            Result result = this.dao.getAllDriverUid();
            JSONObject res = new JSONObject();
            while (result.hasNext()) {
                Record driver = result.next();

                longitude = driver.get("n.longitude").asDouble();
                latitude = driver.get("n.latitude").asDouble();
                String street = driver.get("n.street").asString();

                driver_long = longitude / (180/Math.PI);
                driver_lat = latitude / (180/Math.PI);

                distance = 3963.0*Math.acos((Math.sin(user_lat)*Math.sin(driver_lat))+(Math.cos(user_lat)*Math.cos(driver_lat)*Math.cos(driver_long-user_long)));
                if(distance<=radius) {
                    flag = true;
                    JSONObject data = new JSONObject();
                    data.put("longitude", longitude);
                    data.put("latitude", latitude);
                    data.put("street", street);
                    res.put("data", "data");
                }
            }
            if(flag) {
                res.put("status", "OK");
            } else {
                res.put("status", "No nearby drivers");
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
