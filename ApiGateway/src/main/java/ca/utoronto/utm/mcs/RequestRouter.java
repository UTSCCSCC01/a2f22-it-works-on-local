package ca.utoronto.utm.mcs;

import java.io.IOException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Everything you need in order to send and recieve httprequests to
 * the microservices is given here. Do not use anything else to send
 * and/or recieve http requests from other microservices. Any other
 * imports are fine.
 */
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.OutputStream;    // Also given to you to send back your response
import java.util.HashMap;

public class RequestRouter implements HttpHandler {

    /**
     * You may add and/or initialize attributes here if you
     * need.
     */
	public HashMap<Integer, String> errorMap;
	public RequestRouter() {
		errorMap = new HashMap<>();
		errorMap.put(200, "OK");
		errorMap.put(400, "BAD REQUEST");
		errorMap.put(401, "UNAUTHORIZED");
		errorMap.put(404, "NOT FOUND");
		errorMap.put(405, "METHOD NOT ALLOWED");
		errorMap.put(409, "CONFLICT");
		errorMap.put(500, "INTERNAL SERVER ERROR");
	}

	@Override
	public void handle(HttpExchange r) throws IOException{
        // TODO
		//r.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // For CORS
		try {
			URI uri = r.getRequestURI();
			String p = uri.getPath();
			String query = uri.getQuery();
			String method = r.getRequestMethod();
			String body = Utils.convert(r.getRequestBody());
			String final_p="";

			System.out.printf("uri %s\n", uri);

			String uri_string = uri.toString();

			if(uri_string.contains("location")){
				if(query == null){
					final_p = "http://locationmicroservice:8000" + p;
				} else {
					final_p = "http://locationmicroservice:8000" + p + "?" + query;
				}
 			} else if (uri_string.contains("user")){
				if(query == null){
					final_p = "http://usermicroservice:8000" + p;
				} else {
					final_p = "http://usermicroservice:8000" + p + "?" + query;
				}
			} else if (uri_string.contains("trip")){
				if(query == null){
					final_p = "http://tripinfomicroservice:8000" + p;
				} else {
					final_p = "http://tripinfomicroservice:8000" + p + "?" + query;
				}
			} else {
				this.sendStatus(r, 404);
			}

			System.out.printf("Final_p = %s", final_p);

			HttpClient c1 = HttpClient.newBuilder().build();

			HttpRequest r1 = HttpRequest.
					newBuilder().
					uri(URI.create(final_p)).
					method(method, HttpRequest.BodyPublishers.ofString(body)).build();

			HttpResponse<String> res = c1.send(r1, HttpResponse.BodyHandlers.ofString());
			JSONObject obj = new JSONObject(res.body());
			this.sendResponse(r, obj, res.statusCode());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void sendResponse(HttpExchange r, JSONObject obj, int statusCode) throws JSONException, IOException {
		obj.put("status", errorMap.get(statusCode));
		String response = obj.toString();
		r.sendResponseHeaders(statusCode, response.length());
		this.writeOutputStream(r, response);
	}
	public void writeOutputStream(HttpExchange r, String response) throws IOException {
		OutputStream os = r.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	public void sendStatus(HttpExchange r, int statusCode) throws JSONException, IOException {
		JSONObject res = new JSONObject();
		res.put("status", errorMap.get(statusCode));
		String response = res.toString();
		r.sendResponseHeaders(statusCode, response.length());
		this.writeOutputStream(r, response);
	}

}
