package ca.utoronto.utm.mcs;

import com.mongodb.client.FindIterable;
import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Trip extends Endpoint {

    /**
     * PATCH /trip/:_id
     * @param _id
     * @body distance, endTime, timeElapsed, totalCost
     * @return 200, 400, 404
     * Adds extra information to the trip with the given id when the 
     * trip is done. 
     */

    @Override
    public void handlePatch(HttpExchange r) throws IOException, JSONException {
        // TODO
        String params[] = r.getRequestURI().toString().split("/");
        if (params.length != 3 || params[2].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }

        // check what values are present
        JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
        String fields[] = {"distance", "endTime", "timeElapsed", "discount", "totalCost", "driverPayout"};
        Class<?> fieldClasses[] = {Double.class, Integer.class, String.class, Double.class, Double.class, Double.class};
        if (!validateFields(body, fields, fieldClasses)) {
            this.sendStatus(r, 400);
            return;
        }

        String id = params[2];
        double distance = body.getDouble("distance");
        int endTime = body.getInt("endTime");
        String timeElapsed = body.getString("timeElapsed");
        double discount = body.getDouble("discount");
        double totalCost = body.getDouble("totalCost");
        double driverPayout = body.getDouble("driverPayout");

        try {
            FindIterable<Document> result = this.dao.getTripById(id);
            if (result != null) {
                boolean update = this.dao.updateTripInfo(id, distance, endTime, timeElapsed, discount, totalCost, driverPayout);
                if (update) {
                    this.sendStatus(r, 200);
                } else {
                    this.sendStatus(r, 500);
                }
            } else {
                this.sendStatus(r, 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }

    }
}
