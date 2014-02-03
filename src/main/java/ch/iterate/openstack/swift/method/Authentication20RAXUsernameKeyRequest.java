package ch.iterate.openstack.swift.method;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import ch.iterate.openstack.swift.Client;
import com.google.gson.JsonObject;

public class Authentication20RAXUsernameKeyRequest extends HttpPost implements AuthenticationRequest {
    private static final Logger logger = Logger.getLogger(Authentication11UsernameKeyRequest.class);

    public Authentication20RAXUsernameKeyRequest(URI uri, String username, String apiKey, String tenantId) {
        super(uri);
        JsonObject passwordCredentials = new JsonObject();
        passwordCredentials.addProperty("username", username);
        passwordCredentials.addProperty("apiKey", apiKey);
        JsonObject auth = new JsonObject();
        auth.add("RAX-KSKEY:apiKeyCredentials", passwordCredentials);
        if(tenantId != null) {
            auth.addProperty("tenantId", tenantId);
        }
        JsonObject container = new JsonObject();
        container.add("auth", auth);
        HttpEntity entity = null;
        try {
            entity = new ByteArrayEntity(container.toString().getBytes("UTF-8"));
        }
        catch(UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        this.setHeader(HttpHeaders.ACCEPT, "application/json");
        this.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        this.setEntity(entity);
    }

    public Client.AuthVersion getVersion() {
        return Client.AuthVersion.v20;
    }
}
