package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

public class Navigation extends Endpoint {
    
    /**
     * GET /location/navigation/:driverUid?passengerUid=:passengerUid
     * @param driverUid, passengerUid
     * @return 200, 400, 404, 500
     * Get the shortest path from a driver to passenger weighted by the
     * travel_time attribute on the ROUTE_TO relationship.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[2].isEmpty() || params[3].isEmpty())  {
            this.sendStatus(r, 400);
            return;
        }

        try {
            String parameters = params[3];
            String[] inputs = parameters.split("\\?");
            String driverUid = inputs[0];
            String passengerUid = inputs[1];

            Result result = this.dao.usersStreet(driverUid, passengerUid);
            if (result.hasNext()) {
                try {
                    Record user = result.next();
                    String d_street = user.get("driver.street").asString();
                    String p_street = user.get("passenger.street").asString();
                    JSONObject response = new JSONObject();
                    response.put("status", "OK");
                    JSONObject navData = new JSONObject();
                    ArrayList<JSONObject> route = this.dao.getPath(d_street, p_street);
                    navData.put("total_time", this.dao.getTotalTime(d_street,p_street));
                    navData.put("route", new JSONArray(route));
                    response.put("data", navData);

                    byte[] val = response.toString().replace("\\\"", "").getBytes(); //Converts JSON Object to String
                    if (val == null) {
                        r.sendResponseHeaders(404, -1);
                        return;
                    }
                    r.sendResponseHeaders(200, val.length);
                    OutputStream os = r.getResponseBody();
                    os.write(val);
                    os.close();
                    return;

                } catch (Exception e) {
                    r.sendResponseHeaders(500, -1);
                    e.printStackTrace();
                    return;
                }
            }

//            if (result.hasNext()) {
//                JSONObject res = new JSONObject();
//
//                Record user = result.next();
//                Double longitude = user.get("n.longitude").asDouble();
//                Double latitude = user.get("n.latitude").asDouble();
//                String street = user.get("n.street").asString();
//
//                JSONObject data = new JSONObject();
//                data.put("longitude", longitude);
//                data.put("latitude", latitude);
//                data.put("street", street);
//                res.put("status", "OK");
//                res.put("data", data);
//
//                this.sendResponse(r, res, 200);
//            } else {
//                this.sendStatus(r, 404);
//            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
