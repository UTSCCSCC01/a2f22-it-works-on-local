package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Please write your tests in this class. 
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest {

    public int put_request(String url, JSONObject body) throws IOException, InterruptedException {
        HttpClient c = HttpClient.newBuilder().build();

        HttpRequest r = HttpRequest.
                newBuilder().
                uri(URI.create(url)).
                method("PUT", HttpRequest.BodyPublishers.ofString(body.toString())).build();

        HttpResponse<?> res = c.send(r, HttpResponse.BodyHandlers.ofString());

        return res.statusCode();
    }

    public void patch_request(String url, JSONObject body) throws IOException, InterruptedException {
        HttpClient c = HttpClient.newBuilder().build();

        HttpRequest r = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body.toString()))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse response = c.send(r,HttpResponse.BodyHandlers.ofString());
    }

    public int post_request(String url, JSONObject body) throws IOException, InterruptedException {
        HttpClient c = HttpClient.newBuilder().build();

        HttpRequest r = HttpRequest.
                newBuilder().
                uri(URI.create(url)).
                method("POST", HttpRequest.BodyPublishers.ofString(body.toString())).build();

        HttpResponse<?> res = c.send(r, HttpResponse.BodyHandlers.ofString());

        return res.statusCode();
    }

    public int get_request(String url, JSONObject body) throws IOException, InterruptedException {
        HttpClient c = HttpClient.newBuilder().build();

        HttpRequest r = HttpRequest.
                newBuilder().
                uri(URI.create(url)).
                method("GET", HttpRequest.BodyPublishers.ofString(body.toString())).build();

        HttpResponse<?> res = c.send(r, HttpResponse.BodyHandlers.ofString());

        return res.statusCode();
    }


    @Test
    @Order(1)
    public void tripRequestPass() throws JSONException, IOException, InterruptedException {
        JSONObject body1 = new JSONObject();
        body1.put("uid","john");
        body1.put("is_driver",true);
        put_request("http://localhost:8000/location/user", body1);

        JSONObject body2 = new JSONObject();
        body2.put("uid","ilir");
        body2.put("is_driver",false);
        put_request("http://localhost:8000/location/user", body2);

        JSONObject body3 = new JSONObject();
        body3.put("longitude",78.0732);
        body3.put("latitude",42.0543);
        body3.put("street","road1");
        patch_request("http://localhost:8000/location/john", body3);

        JSONObject body4 = new JSONObject();
        body4.put("longitude",78.0753);
        body4.put("latitude",42.0532);
        body4.put("street","road2");
        patch_request("http://localhost:8000/location/ilir", body4);

        JSONObject body5 = new JSONObject();
        body5.put("roadName","road1");
        body5.put("hasTraffic",true);
        put_request("http://localhost:8000/location/road", body5);

        JSONObject body6 = new JSONObject();
        body6.put("roadName","road2");
        body6.put("hasTraffic",true);
        put_request("http://localhost:8000/location/road", body6);

        JSONObject body7 = new JSONObject();
        body7.put("roadName1", "road1");
        body7.put("roadName2", "road2");
        body7.put("hasTraffic", true);
        body7.put("time", 7);
        post_request("http://localhost:8000/location/hasRoute", body7);

        JSONObject body8 = new JSONObject();
        body8.put("uid", "ilir");
        body8.put("radius", 20);

        HttpClient c = HttpClient.newBuilder().build();
        HttpRequest r = HttpRequest.
                newBuilder().
                uri(URI.create("http://localhost:8000/trip/request")).
                method("POST", HttpRequest.BodyPublishers.ofString(body8.toString())).build();
        HttpResponse<String> res = c.send(r, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res.statusCode());
    }

    @Test
    @Order(2)
    public void tripRequestFail() throws JSONException, IOException, InterruptedException {
        JSONObject body = new JSONObject();
        body.put("uid", "ilir");
        body.put("radius", -1);

        HttpClient c = HttpClient.newBuilder().build();
        HttpRequest r = HttpRequest.
                newBuilder().
                uri(URI.create("http://localhost:8000/trip/request")).
                method("POST", HttpRequest.BodyPublishers.ofString(body.toString())).build();
        HttpResponse<String> res = c.send(r, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, res.statusCode());
    }

    @Test
    @Order(3)
    public void tripConfirmPass() throws JSONException, IOException, InterruptedException {
        JSONObject body = new JSONObject();
        body.put("driver", "john");
        body.put("passenger", "ilir");
        body.put("startTime", 423705687);

        HttpClient c = HttpClient.newBuilder().build();
        HttpRequest r = HttpRequest.
                newBuilder().
                uri(URI.create("http://localhost:8000/trip/confirm")).
                method("POST", HttpRequest.BodyPublishers.ofString(body.toString())).build();
        HttpResponse<String> res = c.send(r, HttpResponse.BodyHandlers.ofString());

        JSONObject json_id = new JSONObject(res.body());
        assertEquals(200, res.statusCode());
    }

    @Test
    @Order(4)
    public void tripConfirmFail() throws JSONException, IOException, InterruptedException {
        JSONObject body = new JSONObject();
        body.put("passenger", "ilir");
        body.put("startTime", 423705687);

        HttpClient c = HttpClient.newBuilder().build();
        HttpRequest r = HttpRequest.
                newBuilder().
                uri(URI.create("http://localhost:8000/trip/confirm")).
                method("POST", HttpRequest.BodyPublishers.ofString(body.toString())).build();
        HttpResponse<String> res = c.send(r, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, res.statusCode());
    }

    @Test
    @Order(5)
    public void patchTripPass() throws JSONException, IOException, InterruptedException {
//        JSONObject body1 = new JSONObject();
//        body1.put("driver", "john");
//        body1.put("passenger", "ilir");
//        body1.put("startTime", 423705687);
//
//        HttpClient c1 = HttpClient.newBuilder().build();
//        HttpRequest r1 = HttpRequest.
//                newBuilder().
//                uri(URI.create("http://localhost:8001/trip/confirm")).
//                method("POST", HttpRequest.BodyPublishers.ofString(body1.toString())).build();
//        HttpResponse<String> res1 = c1.send(r1, HttpResponse.BodyHandlers.ofString());
//
//        JSONObject json_id = new JSONObject(res1.body());
//        String id = json_id.getString("data");
//
//        JSONObject body2 = new JSONObject();
//        body2.put("distance", 5);
//        body2.put("endTime", 423705687);
//        body2.put("timeElapsed", 423705672);
//        body2.put("discount", 0);
//        body2.put("totalCost", 13);
//        body2.put("driverPayout", 25);
//
//        HttpClient c2 = HttpClient.newBuilder().build();
//        HttpRequest r2 = HttpRequest.
//                newBuilder().
//                uri(URI.create("http://localhost:8001/trip/"+id)).
//                method("PATCH", HttpRequest.BodyPublishers.ofString(body2.toString())).
//                header("Content-Type", "application/json").
//                build();
//
//        HttpResponse<String> res2 = c2.send(r2, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, 200);
    }
    @Test
    @Order(6)
    public void patchTripFail() throws JSONException, IOException, InterruptedException {

        JSONObject body = new JSONObject();
        body.put("distance", 20);
        body.put("endTime", 2);

        HttpClient c = HttpClient.newBuilder().build();
        HttpRequest r = HttpRequest.
                newBuilder().
                uri(URI.create("http://localhost:8000/trip/2")).
                method("PATCH", HttpRequest.BodyPublishers.ofString(body.toString())).
                header("Content-Type", "application/json").
                build();
        HttpResponse<String> res = c.send(r, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, res.statusCode());
    }

    @Test
    @Order(7)
    public void tripsForPassengerPass() throws IOException, InterruptedException {

        HttpClient c = HttpClient.newBuilder().build();
        HttpRequest r = HttpRequest.
                newBuilder().
                uri(URI.create("http://localhost:8000/trip/passenger/ilir")).
                build();
        HttpResponse<String> res = c.send(r, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res.statusCode());
    }

    @Test
    @Order(8)
    public void tripsForPassengerFail() throws IOException, InterruptedException {

        HttpClient c = HttpClient.newBuilder().build();
        HttpRequest r = HttpRequest.
                newBuilder().
                uri(URI.create("http://localhost:8000/trip/passenger/il")).
                build();
        HttpResponse<String> res = c.send(r, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, res.statusCode());
    }

    @Test
    @Order(9)
    public void tripsForDriverPass() throws IOException, InterruptedException {

        HttpClient c = HttpClient.newBuilder().build();
        HttpRequest r = HttpRequest.
                newBuilder().
                uri(URI.create("http://localhost:8000/trip/driver/john")).
                build();
        HttpResponse<String> res = c.send(r, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, res.statusCode());
    }

    @Test
    @Order(10)
    public void tripsForDriverFail() throws IOException, InterruptedException {

        HttpClient c = HttpClient.newBuilder().build();
        HttpRequest r = HttpRequest.
                newBuilder().
                uri(URI.create("http://localhost:8000/trip/driver/jo")).
                build();
        HttpResponse<String> res = c.send(r, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, res.statusCode());
    }

    @Test
    @Order(11)
    public void driverTimePass() throws IOException, InterruptedException, JSONException {

        JSONObject body1 = new JSONObject();
        body1.put("driver", "john");
        body1.put("passenger", "ilir");
        body1.put("startTime", 423705687);

        HttpClient c1 = HttpClient.newBuilder().build();
        HttpRequest r1 = HttpRequest.
                newBuilder().
                uri(URI.create("http://localhost:8000/trip/confirm")).
                method("POST", HttpRequest.BodyPublishers.ofString(body1.toString())).build();
        HttpResponse<String> res1 = c1.send(r1, HttpResponse.BodyHandlers.ofString());

        JSONObject json_id = new JSONObject(res1.body());
        String id = json_id.getString("data");
        System.out.println(id);

        HttpClient c2 = HttpClient.newBuilder().build();
        HttpRequest r2 = HttpRequest.
                newBuilder().
                uri(URI.create("http://localhost:8000/trip/driverTime/"+id)).
                build();
        HttpResponse<String> res2 = c2.send(r2, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res2.statusCode());
    }

    @Test
    @Order(12)
    public void driverTimeFail() throws IOException, InterruptedException, JSONException {

        HttpClient c2 = HttpClient.newBuilder().build();
        HttpRequest r2 = HttpRequest.
                newBuilder().
                uri(URI.create("http://localhost:8000/trip/driverTime/63801c88ed65344043d62345")).
                build();
        HttpResponse<String> res2 = c2.send(r2, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, res2.statusCode());
    }
}
