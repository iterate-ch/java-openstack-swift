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

/**
 * @version $Id:$
 */
public class Authentication20AccessKeySecretKeyRequest extends HttpPost implements AuthenticationRequest {
    private static final Logger logger = Logger.getLogger(Authentication11UsernameKeyRequest.class);

    public Authentication20AccessKeySecretKeyRequest(URI uri, String accessKey, String secretKey, String tenantId) {
        super(uri);
        JSONObject passwordCredentials = new JSONObject();
        passwordCredentials.put("accessKey", accessKey);
        passwordCredentials.put("secretKey", secretKey);
        JSONObject auth = new JSONObject();
        auth.put("apiAccessKeyCredentials", passwordCredentials);
        if(StringUtils.isNotBlank(tenantId)) {
            auth.put("tenantId", tenantId);
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
