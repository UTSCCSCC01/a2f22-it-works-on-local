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

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Request extends Endpoint {

    /**
     * POST /trip/request
     * @body uid, radius
     * @return 200, 400, 404, 500
     * Returns a list of drivers within the specified radius
     * using location microservice. List should be obtained
     * from navigation endpoint in location microservice
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        // TODO
        try {
            JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
            String uid = body.getString("uid");
            int radius = body.getInt("radius");

            String uri = "http://localhost:8000/location/nearbyDriver/" + uid + "?radius=" + Integer.toString(radius);

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
            ArrayList<String> uid_arr = new ArrayList<String>();
            Iterator<String> keys = dataObj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                uid_arr.add(key);
            }
            JSONObject final_obj = new JSONObject();
            final_obj.put("data", new JSONArray(uid_arr));
            this.sendResponse(r, final_obj, 200);
        } catch (Exception e) {
            this.sendStatus(r, 500);
        }
    }
}
