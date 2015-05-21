package ch.iterate.openstack.swift.handler;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.iterate.openstack.swift.AuthenticationResponse;
import ch.iterate.openstack.swift.Response;
import ch.iterate.openstack.swift.exception.AuthorizationException;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.Region;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class AuthenticationJson11ResponseHandler implements ResponseHandler<AuthenticationResponse> {

    public AuthenticationResponse handleResponse(final HttpResponse response) throws IOException {
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            Charset charset = HTTP.DEF_CONTENT_CHARSET;
            ContentType contentType = ContentType.get(response.getEntity());
            if(contentType != null) {
                if(contentType.getCharset() != null) {
                    charset = contentType.getCharset();
                }
            }
            try {
                final JsonParser parser = new JsonParser();
                final JsonObject json = parser.parse(new InputStreamReader(response.getEntity().getContent(), charset)).getAsJsonObject();
                final JsonObject auth = json.getAsJsonObject("auth");
                final String token = auth.getAsJsonObject("token").get("id").getAsString();
                final Map<String, String> cdnUrls = new HashMap<String, String>();
                JsonObject serviceCatalog = auth.getAsJsonObject("serviceCatalog");
                for(JsonElement e : serviceCatalog.getAsJsonArray("cloudFilesCDN")) {
                    final JsonObject cloudFilesCDN = e.getAsJsonObject();
                    String regionId = cloudFilesCDN.get("region").getAsString();
                    String publicUrl = cloudFilesCDN.get("publicURL").getAsString();
                    cdnUrls.put(regionId, publicUrl);
                }
                Set<Region> regions = new HashSet<Region>();
                for(JsonElement e : serviceCatalog.getAsJsonArray("cloudFiles")) {
                    JsonObject cloudFiles = e.getAsJsonObject();
                    String regionId = cloudFiles.get("region").getAsString();
                    String publicUrl = cloudFiles.get("publicURL").getAsString();
                    String cdnUrl = cdnUrls.containsKey(regionId) ? cdnUrls.get(regionId) : null;
                    Boolean v1Default = cloudFiles.get("v1Default") != null
                            ? cloudFiles.get("v1Default").getAsBoolean() : Boolean.FALSE;
                    regions.add(new Region(regionId, URI.create(publicUrl), cdnUrl == null ? null : URI.create(cdnUrl), v1Default));
                }
                return new AuthenticationResponse(response, token, regions);
            }
            catch(JsonParseException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED
                || response.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN) {
            throw new AuthorizationException(new Response(response));
        }
        throw new GenericException(new Response(response));
    }
}
