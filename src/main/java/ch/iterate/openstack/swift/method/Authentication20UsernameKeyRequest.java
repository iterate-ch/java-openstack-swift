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

public class Authentication20UsernameKeyRequest extends HttpPost implements AuthenticationRequest {
    private static final Logger logger = Logger.getLogger(Authentication11UsernameKeyRequest.class.getName());

    public Authentication20UsernameKeyRequest(URI uri, String username, String key, String tenantName) {
        super(uri);
        JsonObject passwordCredentials = new JsonObject();
        passwordCredentials.addProperty("username", username);
        passwordCredentials.addProperty("apiKey", key);
        JsonObject auth = new JsonObject();
        auth.add("apiKeyCredentials", passwordCredentials);
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
