package ch.iterate.openstack.swift.method;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import ch.iterate.openstack.swift.Client;

public class Authentication11UsernameKeyRequest extends HttpPost implements AuthenticationRequest {
    private static final Logger logger = Logger.getLogger(Authentication11UsernameKeyRequest.class);

    public Authentication11UsernameKeyRequest(final URI uri, final String username, final String key) {
        super(uri);
        JSONObject credentials = new JSONObject();
        credentials.put("username", username);
        credentials.put("key", key);
        JSONObject container = new JSONObject();
        container.put("credentials", credentials);
        String json = JSONValue.toJSONString(container);
        HttpEntity entity = null;
        try {
            entity = new ByteArrayEntity(json.getBytes("UTF-8"));
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
