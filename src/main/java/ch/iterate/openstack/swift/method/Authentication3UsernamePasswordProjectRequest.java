package ch.iterate.openstack.swift.method;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import ch.iterate.openstack.swift.Client;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 {
     "auth": {
         "identity": {
             "methods": [
                 "password"
             ],
             "password": {
                 "user": {
                     "domain": {
                         "id": "default"
                     },
                     "name": "my-username",
                     "password": "my-password"
                 }
             }
         },
         "scope": {
             "project": {
                 "domain": {
                     "id": "default"
                 },
                 "name": "project-x"
             }
         }
     }
 }
 */
public class Authentication3UsernamePasswordProjectRequest extends HttpPost implements AuthenticationRequest {
    private static final Logger logger = Logger.getLogger(Authentication11UsernameKeyRequest.class);

    public Authentication3UsernamePasswordProjectRequest(URI uri, String username, String secret, String project) {
        this(uri, username, secret, project, "default");
    }

    public Authentication3UsernamePasswordProjectRequest(URI uri, String username, String secret, String project, String domain) {
        super(uri);
        JsonObject jsonAuth = new JsonObject();
        JsonObject jsonIdentity = new JsonObject();
        JsonArray jsonMethods = new JsonArray();
        jsonMethods.add(new JsonPrimitive("password"));
        jsonIdentity.add("methods", jsonMethods);
        JsonObject jsonPassword = new JsonObject();
        jsonIdentity.add("password", jsonPassword);
        JsonObject jsonUser = new JsonObject();
        JsonObject jsonDomain = new JsonObject();
        jsonDomain.addProperty("id", domain);
        jsonUser.add("domain", jsonDomain);
        jsonUser.addProperty("name", username);
        jsonUser.addProperty("password", secret);
        jsonPassword.add("user", jsonUser);
        jsonAuth.add("identity", jsonIdentity);
        // If you do not include the optional scope and the authenticating user has a defined default project
        // (the default_project_id attribute for the user), that default project is treated as the preferred authorization scope.
        if(project != null) {
            JsonObject jsonScope = new JsonObject();
            JsonObject jsonProject = new JsonObject();
            jsonProject.add("domain", jsonDomain);
            jsonProject.addProperty("name", project);
            jsonScope.add("project", jsonProject);
            jsonAuth.add("scope", jsonScope);
        }
        JsonObject container = new JsonObject();
        container.add("auth", jsonAuth);
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
        return Client.AuthVersion.v3;
    }
}
