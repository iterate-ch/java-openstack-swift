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

public class Authentication11UsernameKeyRequest extends HttpPost implements AuthenticationRequest {
    private static final Logger logger = Logger.getLogger(Authentication11UsernameKeyRequest.class);

    public Authentication11UsernameKeyRequest(final URI uri, final String username, final String key) {
        super(uri);
        JsonObject credentials = new JsonObject();
        credentials.addProperty("username", username);
        credentials.addProperty("key", key);
        JsonObject container = new JsonObject();
        container.add("credentials", credentials);
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
        return Client.AuthVersion.v11;
    }
}
