package ch.iterate.openstack.swift.handler;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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

public class AuthenticationJson20ResponseHandler implements ResponseHandler<AuthenticationResponse> {

    public AuthenticationResponse handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
        if(response.getStatusLine().getStatusCode() == 200 ||
                response.getStatusLine().getStatusCode() == 203) {
            Charset charset = HTTP.DEF_CONTENT_CHARSET;
            ContentType contentType = ContentType.get(response.getEntity());
            if(contentType != null) {
                if(contentType.getCharset() != null) {
                    charset = contentType.getCharset();
                }
            }
            JSONObject json = (JSONObject) JSONValue.parse(new InputStreamReader(response.getEntity().getContent(), charset));
            JSONObject auth = (JSONObject) json.get("access");
            JSONObject user = ((JSONObject) auth.get("user"));
            String defaultRegion = null;
            if(user.containsKey("RAX-AUTH:defaultRegion")) {
                defaultRegion = user.get("RAX-AUTH:defaultRegion").toString();
            }
            String token = ((JSONObject) auth.get("token")).get("id").toString();
            JSONArray serviceCatalogs = (JSONArray) auth.get("serviceCatalog");
            Set<Region> regions = new HashSet<Region>();
            Map<String, String> cdnUrls = new HashMap<String, String>();
            for(Object serviceCatalog : serviceCatalogs) {
                if(((JSONObject) serviceCatalog).get("type").equals("rax:object-cdn")) {
                    for(Object endpoint : (JSONArray) ((JSONObject) serviceCatalog).get("endpoints")) {
                        String regionId = ((JSONObject) endpoint).get("region").toString();
                        String publicUrl = ((JSONObject) endpoint).get("publicURL").toString();
                        cdnUrls.put(regionId, publicUrl);
                    }
                }
                if(((JSONObject) serviceCatalog).get("type").equals("hpext:cdn")) {
                    for(Object endpoint : (JSONArray) ((JSONObject) serviceCatalog).get("endpoints")) {
                        String regionId = ((JSONObject) endpoint).get("region").toString();
                        String publicUrl = ((JSONObject) endpoint).get("publicURL").toString();
                        cdnUrls.put(regionId, publicUrl);
                    }
                }
            }
            for(Object serviceCatalog : serviceCatalogs) {
                if(((JSONObject) serviceCatalog).get("type").equals("object-store")) {
                    for(Object endpoint : (JSONArray) ((JSONObject) serviceCatalog).get("endpoints")) {
                        String regionId = ((JSONObject) endpoint).get("region").toString();
                        String publicUrl = ((JSONObject) endpoint).get("publicURL").toString();
                        String cdnUrl = cdnUrls.containsKey(regionId) ? cdnUrls.get(regionId) : null;
                        regions.add(new Region(regionId, URI.create(publicUrl), cdnUrl == null ? null : URI.create(cdnUrl),
                                regionId.equals(defaultRegion)));
                    }
                }
            }
            return new AuthenticationResponse(response, token, regions);
        }
        else if(response.getStatusLine().getStatusCode() == 401 || response.getStatusLine().getStatusCode() == 403) {
            throw new AuthorizationException(new Response(response));
        }
        else {
            throw new GenericException(new Response(response));
        }
    }
}
