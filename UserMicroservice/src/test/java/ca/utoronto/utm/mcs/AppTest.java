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

    public int post_request(String url, JSONObject body) throws IOException, InterruptedException {
        HttpClient c = HttpClient.newBuilder().build();

        HttpRequest r = HttpRequest.
                newBuilder().
                uri(URI.create(url)).
                POST(HttpRequest.BodyPublishers.ofString(body.toString())).build();

        HttpResponse<String> res = c.send(r, HttpResponse.BodyHandlers.ofString());
        return res.statusCode();
    }

    @Test
    @Order(1)
    public void userRegisterPass() throws JSONException, IOException, InterruptedException {
        JSONObject body = new JSONObject();
        body.put("name","Mojo");
        body.put("email","mojoh@gmail.com");
        body.put("password", "123456");
        int val = post_request("http://apigateway:8000/user/register", body);

        assertEquals(200, val);
    }

    @Test
    @Order(2)
    public void userRegisterFail() throws JSONException, IOException, InterruptedException {
        JSONObject body = new JSONObject();
        body.put("name","Mojo");
        body.put("email","mojoh@gmail.com");
        body.put("password", "123456");
        int val = post_request("http://apigateway:8000/user/register", body);

        assertEquals(400, val);
    }
    @Test
    @Order(3)
    public void userLoginPass() throws JSONException, IOException, InterruptedException {
        JSONObject body = new JSONObject();
        body.put("email","mojoh@gmail.com");
        body.put("password", "123456");
        int val = post_request("http://apigateway:8000/user/login", body);

        assertEquals(200, val);
    }

    @Test
    @Order(4)
    public void userLoginFail() throws JSONException, IOException, InterruptedException {
        JSONObject body = new JSONObject();
        body.put("email","mojoh2@gmail.com");
        body.put("password", "123");
        int val = post_request("http://apigateway:8000/user/register", body);

        assertEquals(400, val);
    }

}
