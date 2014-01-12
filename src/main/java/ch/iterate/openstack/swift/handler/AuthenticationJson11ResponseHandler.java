package ch.iterate.openstack.swift.handler;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

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

public class AuthenticationJson11ResponseHandler implements ResponseHandler<AuthenticationResponse> {

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
            JSONObject json;
            try {
                json = (JSONObject) JSONValue.parseWithException(new InputStreamReader(response.getEntity().getContent(), charset));
            }
            catch(ParseException e) {
                throw new GenericException(e.getMessage(), e);
            }
            JSONObject auth = (JSONObject) json.get("auth");
            String token = ((JSONObject) auth.get("token")).get("id").toString();

            Map<String, String> cdnUrls = new HashMap<String, String>();
            JSONObject serviceCatalog = (JSONObject) auth.get("serviceCatalog");
            for(Object cloudFilesCDN : (JSONArray) serviceCatalog.get("cloudFilesCDN")) {
                String regionId = ((JSONObject) cloudFilesCDN).get("region").toString();
                String publicUrl = ((JSONObject) cloudFilesCDN).get("publicURL").toString();
                cdnUrls.put(regionId, publicUrl);
            }
            Set<Region> regions = new HashSet<Region>();
            for(Object cloudFiles : (JSONArray) serviceCatalog.get("cloudFiles")) {
                String regionId = ((JSONObject) cloudFiles).get("region").toString();
                String publicUrl = ((JSONObject) cloudFiles).get("publicURL").toString();
                String cdnUrl = cdnUrls.containsKey(regionId) ? cdnUrls.get(regionId) : null;
                Boolean v1Default = ((JSONObject) cloudFiles).containsKey("v1Default")
                        ? (Boolean) ((JSONObject) cloudFiles).get("v1Default")
                        : Boolean.FALSE;
                regions.add(new Region(regionId, URI.create(publicUrl), cdnUrl == null ? null : URI.create(cdnUrl), v1Default));
            }
            return new AuthenticationResponse(response, token, regions);
        }
        else if(response.getStatusLine().getStatusCode() == 401 || response.getStatusLine().getStatusCode() == 403) {
            throw new AuthorizationException(new Response(response));
        }
        throw new GenericException(new Response(response));
    }
}
