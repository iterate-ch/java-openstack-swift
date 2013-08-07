package ch.iterate.openstack.swift.method;

import org.apache.commons.lang.StringUtils;
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

public class Authentication20RAXUsernameKeyRequest extends HttpPost implements AuthenticationRequest {
    private static final Logger logger = Logger.getLogger(Authentication11UsernameKeyRequest.class);

    public Authentication20RAXUsernameKeyRequest(URI uri, String username, String apiKey, String tenant) {
        super(uri);
        JSONObject passwordCredentials = new JSONObject();
        passwordCredentials.put("username", username);
        passwordCredentials.put("apiKey", apiKey);
        JSONObject auth = new JSONObject();
        auth.put("RAX-KSKEY:apiKeyCredentials", passwordCredentials);
        if(StringUtils.isNotBlank(tenant)) {
            auth.put("tenantId", tenant);
        }
        JSONObject container = new JSONObject();
        container.put("auth", auth);
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
        return Client.AuthVersion.v20;
    }
}
