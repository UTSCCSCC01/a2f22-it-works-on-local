package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class Register extends Endpoint {

    /**
     * POST /user/register
     * @body name, email, password
     * @return 200, 400, 500
     * Register a user into the system using the given information.
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
        String name = null;
        String password = null;

        // check what values are present
        if (deserialized.has("email")) {
            if (deserialized.get("email").getClass() != String.class) {
                this.sendStatus(r, 400);
                return;
            }
            email = deserialized.getString("email");
        }
        if (deserialized.has("name")) {
            if (deserialized.get("name").getClass() != String.class) {
                this.sendStatus(r, 400);
                return;
            }
            name = deserialized.getString("name");
        }
        if (deserialized.has("password")) {
            if (deserialized.get("password").getClass() != String.class) {
                this.sendStatus(r, 400);
                return;
            }
            password = deserialized.getString("password");
        }

        // if all the variables are still null then there's no variables in request so retrun 400
        if (email == null || name == null || password == null) {
            this.sendStatus(r, 200);
            return;
        }

        // update db, return 500 if error
        try {
            int uid;
            boolean resultHasNext;
            Random rand = new Random();
            uid = rand. nextInt(9000000) + 1000000;
            resultHasNext = this.dao.getUsersFromUid(uid).next();
            while(!resultHasNext){
                uid = rand. nextInt(9000000) + 1000000;
                resultHasNext = this.dao.getUsersFromUid(uid).next();
            }
            String generatedPassword = hashingMD5(password);

            this.dao.addUser(uid, email, generatedPassword, name, 0, false);
        }
        catch (SQLException e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
            return;
        }

        // return 200 if everything is updated without error
        this.sendStatus(r, 200);
    }
    public String hashingMD5(String password) {
        String generatedPassword = null;
        try
        {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Add password bytes to digest
            md.update(password.getBytes());

            // Get the hash's bytes
            byte[] bytes = md.digest();

            // These bytes[] has bytes in decimal format. Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            // Get complete hashed password in hex format
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }
}
