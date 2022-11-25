package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login extends Endpoint {

    /**
     * POST /user/login
     * @body email, password
     * @return 200, 400, 401, 404, 500
     * Login a user into the system if the given information matches the 
     * information of the user in the database.
     */
    
    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        // TODO
        // check if request url isn't malformed
        String[] splitUrl = r.getRequestURI().getPath().split("/");
        if (splitUrl.length != 3) {
            this.sendStatus(r, 400);
            return;
        }

        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String email = null;
        String password = null;

        // check what values are present
        if (deserialized.has("email")) {
            if (deserialized.get("email").getClass() != String.class) {
                this.sendStatus(r, 400);
                return;
            }
            email = deserialized.getString("email");
        }
        if (deserialized.has("password")) {
            if (deserialized.get("password").getClass() != String.class) {
                this.sendStatus(r, 400);
                return;
            }
            password = deserialized.getString("password");
        }

        // if all the variables are still null then there's no variables in request so retrun 400
        if (email == null || password == null) {
            this.sendStatus(r, 400);
            return;
        }

        // make query and get required data, return 500 if error
        ResultSet rs;
        boolean resultHasNext;
        try {
            rs = this.dao.getUserDataFromCredentials(email);
            resultHasNext = rs.next();
        }
        catch (SQLException e) {
            this.sendStatus(r, 500);
            return;
        }

        // check if user was found, return 404 if not found
        if (!resultHasNext) {
            this.sendStatus(r, 404);
            return;
        }

        // get data
        String pass;
        try {
            pass = rs.getString("password");
        }
        catch (SQLException e) {
            this.sendStatus(r, 500);
            return;
        }

        // Check if passwords match
        String generatedPassword = this.dao.hashingMD5(password);
        if(!generatedPassword.equals(pass)) {
            this.sendStatus(r, 401);
            return;
        }

        // return 200 if user is logged in with the given credentials
        this.sendStatus(r, 200);
    }
}
