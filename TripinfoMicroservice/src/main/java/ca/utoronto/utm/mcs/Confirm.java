package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Confirm extends Endpoint {

    /**
     * POST /trip/confirm
     * @body driver, passenger, startTime
     * @return 200, 400
     * Adds trip info into the database after trip has been requested.
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        // TODO
        JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
        String passenger = null;
        String driver = null;
        int startTime = 0;

        // check what values are present
        if (body.has("passenger")) {
            if (body.get("passenger").getClass() != String.class) {
                this.sendStatus(r, 400);
                return;
            }
            passenger = body.getString("passenger");
        }
        if (body.has("driver")) {
            if (body.get("driver").getClass() != String.class) {
                this.sendStatus(r, 400);
                return;
            }
            driver = body.getString("driver");
        }
        if (body.has("startTime")) {
            if (body.get("startTime").getClass() != Integer.class) {
                System.out.println("Entered 400 3rd part");
                System.out.println(body.get("startTime").getClass());
                this.sendStatus(r, 400);
                return;
            }
            startTime = body.getInt("startTime");
        }

        // if all the variables are still null then there's no variables in request so retrun 400
        if (passenger == null || driver == null || startTime == 0) {
            this.sendStatus(r, 400);
            return;
        }

        // update db, return 500 if error
        try {
            ObjectId id = this.dao.confirmTrip(passenger, driver, startTime);
            if(id == null) {
                this.sendStatus(r, 500);
                return;
            }
            // return 200 if everything is updated without error
            JSONObject res = new JSONObject();
            res.put("data", id);
            this.sendResponse(r, res, 200);
        } catch (Exception e) {
            this.sendStatus(r, 500);
        }
    }
}
