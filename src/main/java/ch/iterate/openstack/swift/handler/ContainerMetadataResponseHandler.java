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
import ch.iterate.openstack.swift.exception.ContainerNotFoundException;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.ContainerMetadata;

public class ContainerMetadataResponseHandler extends MetadataResponseHandler implements ResponseHandler<ContainerMetadata> {

    public ContainerMetadata handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT ||
                response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

            ContainerMetadata metadata = new ContainerMetadata(
                    this.getContentType(response),
                    this.getContentLength(response),
                    this.getETag(response),
                    this.getLastModified(response));

            for(Header h : response.getAllHeaders()) {
                if(h.getName().startsWith(Constants.X_CONTAINER_META)
                        || Constants.HTTP_HEADER_EDITABLE_NAMES.contains(h.getName().toLowerCase(Locale.ENGLISH))) {
                    metadata.addMetaData(h.getName(), decode(h.getValue()));
                }
            }
            return metadata;
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            throw new ContainerNotFoundException(new Response(response));
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthorizationException(new Response(response));
        }
        throw new GenericException(new Response(response));
    }
}