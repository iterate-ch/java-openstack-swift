package ch.iterate.openstack.swift.method;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.iterate.openstack.swift.Client;
import com.google.gson.JsonObject;

/**
 * {
 * "auth":{
 * "passwordCredentials":{
 * "username":"test_user",
 * "password":"mypass"
 * },
 * "tenantName":"customer-x"
 * }
 * }
 */
public class Authentication20UsernamePasswordRequest extends HttpPost implements AuthenticationRequest {
    private static final Logger logger = Logger.getLogger(Authentication11UsernameKeyRequest.class.getName());

    public Authentication20UsernamePasswordRequest(URI uri, String username, String password, String tenantName) {
        super(uri);
        JsonObject passwordCredentials = new JsonObject();
        passwordCredentials.addProperty("username", username);
        passwordCredentials.addProperty("password", password);
        JsonObject auth = new JsonObject();
        auth.add("passwordCredentials", passwordCredentials);
        if(tenantName != null) {
            auth.addProperty("tenantName", tenantName);
        }
        JsonObject container = new JsonObject();
        container.add("auth", auth);
        HttpEntity entity = null;
        try {
            entity = new ByteArrayEntity(container.toString().getBytes("UTF-8"));
        }
        catch(UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        this.setHeader(HttpHeaders.ACCEPT, "application/json");
        this.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        this.setEntity(entity);
    }

    public Client.AuthVersion getVersion() {
        return Client.AuthVersion.v20;
    }
}
