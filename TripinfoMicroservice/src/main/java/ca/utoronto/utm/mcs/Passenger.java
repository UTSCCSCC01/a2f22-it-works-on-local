package ca.utoronto.utm.mcs;

import com.mongodb.client.FindIterable;
import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Passenger extends Endpoint {

    /**
     * GET /trip/passenger/:uid
     * @param uid
     * @return 200, 400, 404
     * Get all trips the passenger with the given uid has.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException,JSONException{
        // TODO
        String params[] = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[3].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }
        String pid = params[3];

        try {
            FindIterable<Document> result = this.dao.getTripByPassengerId(pid);
            if (result.cursor().hasNext()) {
                JSONObject res = new JSONObject();
                JSONObject data = new JSONObject();
                data.put("trips", findIterableToJSONArray(result));
                res.put("data", data);
                this.sendResponse(r, res, 200);
                return;
            }
            this.sendStatus(r, 404);
        } catch (Exception e) {
            this.sendStatus(r, 500);
        }
    }
    public static JSONArray findIterableToJSONArray(FindIterable<Document> docs) throws Exception {
        JSONArray arr = new JSONArray();
        int i = 0;
        for (Document doc : docs) {
            arr.put(i, new JSONObject(doc.toJson()));
            i++;
        }
        return arr;
    }
}
