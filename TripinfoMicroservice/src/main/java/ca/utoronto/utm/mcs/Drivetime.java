package ca.utoronto.utm.mcs;

/** 
 * Everything you need in order to send and recieve httprequests to 
 * other microservices is given here. Do not use anything else to send 
 * and/or recieve http requests from other microservices. Any other 
 * imports are fine.
 */
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

import com.mongodb.client.FindIterable;
import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Drivetime extends Endpoint {

    /**
     * GET /trip/driverTime/:_id
     * @param _id
     * @return 200, 400, 404, 500
     * Get time taken to get from driver to passenger on the trip with
     * the given _id. Time should be obtained from navigation endpoint
     * in location microservice.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        // TODO
        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[2].isEmpty() || params[3].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }

        try{
            String id = params[3];
            ObjectId oid = new ObjectId(id);
            ArrayList<String> result = this.dao.getDriver(oid);
            if(result == null){
                this.sendStatus(r, 404);
                return;
            } else {
                String uri = "http://localhost:8000/location/navigation/" + result.get(0) + "?passengerUid=" + result.get(1);

                HttpClient c = HttpClient.newBuilder().build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(uri))
                        .build();

                HttpResponse<String> res = c.send(request, HttpResponse.BodyHandlers.ofString());
                String json_res = res.body();
                JSONObject obj = new JSONObject(json_res);
                if ((obj.getString("status").equals("BAD REQUEST"))) {
                    this.sendStatus(r, 400);
                    return;
                } else if ((obj.getString("status").equals("NOT FOUND"))) {
                    this.sendStatus(r, 404);
                    return;
                } else if ((obj.getString("status").equals("INTERNAL SERVER ERROR"))) {
                    this.sendStatus(r, 500);
                    return;
                }

                JSONObject dataObj = new JSONObject(obj.getString("data"));
                JSONObject arrival_time = new JSONObject();
                arrival_time.put("arrival_time", dataObj.getInt("total_time"));
                JSONObject final_obj = new JSONObject();
                final_obj.put("data", arrival_time);

                this.sendResponse(r, final_obj, 200);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
