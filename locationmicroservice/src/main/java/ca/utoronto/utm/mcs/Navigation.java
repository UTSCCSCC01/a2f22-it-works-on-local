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
            JSONObject response = new JSONObject();
            String parameters = params[3];
            String[] inputs = parameters.split("\\?");
            String driverUid = inputs[0];
            String passengerUid = inputs[1].split("=")[1];

            if(!this.dao.validUser(driverUid) || !this.dao.validUser(passengerUid)){
                this.sendStatus(r, 400);
                return;
            } else {
                Result result = this.dao.usersStreet(driverUid, passengerUid);
                if (result.hasNext()) {
                    try {
                        Record user = result.next();
                        String d_street = user.get("driver.street").asString();
                        String p_street = user.get("passenger.street").asString();
                        if(!this.dao.validRoad(d_street) || !this.dao.validRoad(p_street)){
                            this.sendStatus(r, 400);
                            return;
                        } else {
                            JSONObject navData = new JSONObject();
                            ArrayList<JSONObject> route = this.dao.getPath(d_street, p_street);
                            navData.put("total_time", this.dao.getTotalTime(d_street, p_street));
                            navData.put("route", new JSONArray(route));
                            response.put("data", navData);
                            this.sendResponse(r, response, 200);
                        }
                    } catch (Exception e) {
                        this.sendStatus(r, 500);
                        return;
                    }
                } else {
                    this.sendStatus(r, 404);
                    return;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
