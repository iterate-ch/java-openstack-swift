package ch.iterate.openstack.swift.handler;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import java.io.IOException;
import java.util.Locale;

import ch.iterate.openstack.swift.Constants;
import ch.iterate.openstack.swift.Response;
import ch.iterate.openstack.swift.exception.AuthorizationException;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.exception.NotFoundException;
import ch.iterate.openstack.swift.model.ObjectMetadata;

public class ObjectMetadataResponseHandler extends MetadataResponseHandler implements ResponseHandler<ObjectMetadata> {

    public ObjectMetadata handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT ||
                response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

            ObjectMetadata metadata = new ObjectMetadata(
                    this.getContentType(response),
                    this.getContentLength(response),
                    this.getETag(response),
                    this.getLastModified(response));

            for(Header h : response.getAllHeaders()) {
                if(h.getName().startsWith(Constants.X_OBJECT_META)
                        || Constants.HTTP_HEADER_EDITABLE_NAMES.contains(h.getName().toLowerCase(Locale.ENGLISH))) {
                    metadata.addMetaData(h.getName(), h.getValue());
                }
            }
            return metadata;
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            throw new NotFoundException(new Response(response));
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthorizationException(new Response(response));
        }
        throw new GenericException(new Response(response));
    }
}
