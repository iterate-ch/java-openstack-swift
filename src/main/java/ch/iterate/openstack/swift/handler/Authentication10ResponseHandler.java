package ch.iterate.openstack.swift.handler;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import ch.iterate.openstack.swift.AuthenticationResponse;
import ch.iterate.openstack.swift.Constants;
import ch.iterate.openstack.swift.Response;
import ch.iterate.openstack.swift.exception.AuthorizationException;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.Region;

public class Authentication10ResponseHandler implements ResponseHandler<AuthenticationResponse> {

    public AuthenticationResponse handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
        if(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 203
                || response.getStatusLine().getStatusCode() == 204) {
            return new AuthenticationResponse(response, response.getFirstHeader(Constants.X_AUTH_TOKEN).getValue(),
                    Collections.singleton(new Region(null,
                            this.getStorageURL(response), this.getCDNManagementURL(response), true)));
        }
        else if(response.getStatusLine().getStatusCode() == 401 || response.getStatusLine().getStatusCode() == 403) {
            throw new AuthorizationException(new Response(response));
        }
        throw new GenericException(new Response(response));
    }

    /**
     * This method makes no assumptions about the user having been logged in.  It simply looks for the Storage URL header
     * as defined by FilesConstants.X_STORAGE_URL and if this exists it returns its value otherwise the value returned will be null.
     *
     * @return null if the user is not logged into Cloud FS or the Storage URL
     */
    private URI getStorageURL(final HttpResponse response) {
        Header hdr = response.getFirstHeader(Constants.X_STORAGE_URL);
        if(null == hdr) {
            return null;
        }
        return URI.create(hdr.getValue());
    }

    /**
     * This method makes no assumptions about the user having been logged in.  It simply looks for the CDN Management URL header
     * as defined by FilesConstants.X_CDN_MANAGEMENT_URL and if this exists it returns its value otherwise the value returned will be null.
     *
     * @return null if the user is not logged into Cloud FS or the Storage URL
     */
    private URI getCDNManagementURL(final HttpResponse response) {
        Header hdr = response.getFirstHeader(Constants.X_CDN_MANAGEMENT_URL);
        if(null == hdr) {
            return null;
        }
        return URI.create(hdr.getValue());
    }


}
