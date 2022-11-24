package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

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

    @Test
    @Order(1)
    public void getNavigationPass() throws JSONException, IOException, InterruptedException {
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
            body4.put("longitude",78.0653);
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


            HttpClient c = HttpClient.newBuilder().build();
            HttpRequest r = HttpRequest.
                    newBuilder().
                    uri(URI.create("http://localhost:8000/location/navigation/john?passengerUid=ilir")).build();
            HttpResponse<String> res = c.send(r, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res.statusCode());
    }

    @Test
    @Order(2)
    public void getNavigationFail() throws JSONException, IOException, InterruptedException {

        HttpClient c = HttpClient.newBuilder().build();
        HttpRequest r = HttpRequest.
                newBuilder().
                uri(URI.create("http://localhost:8000/location/navigation/john?passengerUid=ilir12")).build();
        HttpResponse<String> res = c.send(r, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, res.statusCode());
    }
}