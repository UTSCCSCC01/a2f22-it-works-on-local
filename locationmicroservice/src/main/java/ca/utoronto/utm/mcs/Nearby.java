package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.util.List;

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
        String[] params2 = params[3].split("\\?");
        String[] params3 = params2[1].split("=");
        if (params.length != 4 || params[2].isEmpty() || params[3].isEmpty() || params2[0].isEmpty() || params3[1].isEmpty())  {
            this.sendStatus(r, 400);
            return;
        }

        try {
            Double user_long = 0.0, user_lat = 0.0, longitude = 0.0, latitude = 0.0, driver_long = 0.0, driver_lat = 0.0, distance = 0.0;
            boolean flag = false;
            String uid = params2[0];
            double radius  = Double.parseDouble(params3[1]);
            Result users = this.dao.getUserByUid(uid);
            if(users.hasNext()) {
                Result locations = this.dao.getUserLocationByUid(uid);
                Record location = locations.next();

                user_long = (location.get("n.longitude").asDouble()) / (180/Math.PI);
                user_lat = (location.get("n.latitude").asDouble()) / (180/Math.PI);
            } else {
                this.sendStatus(r, 404);
            }
            Result result = this.dao.getAllDriverUid();
            JSONObject res = new JSONObject();
            JSONObject driver = new JSONObject();
            while (result.hasNext()) {
                Record curr_driver = result.next();
                String driver_uid = curr_driver.get("n.uid").asString();

                Record driver_info = this.dao.getUserLocationByUid(driver_uid).next();

                longitude = driver_info.get("n.longitude").asDouble();
                latitude = driver_info.get("n.latitude").asDouble();
                String street = driver_info.get("n.street").asString();

                driver_long = longitude / (180/Math.PI);
                driver_lat = latitude / (180/Math.PI);

                distance = 3963.0*Math.acos((Math.sin(user_lat)*Math.sin(driver_lat))+(Math.cos(user_lat)*Math.cos(driver_lat)*Math.cos(driver_long-user_long)));
                if(distance<=radius) {
                    flag = true;
                    JSONObject data = new JSONObject();
                    data.put("longitude", longitude);
                    data.put("latitude", latitude);
                    data.put("street", street);
                    driver.put(driver_uid, data);
                    res.put("data", driver);
                }
            }
            if(flag) {
                this.sendResponse(r, res, 200);
            } else {
                this.sendStatus(r, 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
