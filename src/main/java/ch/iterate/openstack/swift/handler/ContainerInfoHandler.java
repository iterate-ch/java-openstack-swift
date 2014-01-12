package ch.iterate.openstack.swift.handler;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import java.io.IOException;

import ch.iterate.openstack.swift.Constants;
import ch.iterate.openstack.swift.Response;
import ch.iterate.openstack.swift.exception.AuthorizationException;
import ch.iterate.openstack.swift.exception.ContainerNotFoundException;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.ContainerInfo;
import ch.iterate.openstack.swift.model.Region;

public class ContainerInfoHandler implements ResponseHandler<ContainerInfo> {

    private Region region;
    private String container;

    public ContainerInfoHandler(final Region region, final String container) {
        this.region = region;
        this.container = container;
    }

    public ContainerInfo handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT ||
                response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return new ContainerInfo(region, container,
                    this.getContainerObjectCount(response), this.getContainerBytesUsed(response));
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            throw new ContainerNotFoundException(new Response(response));
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthorizationException(new Response(response));
        }
        throw new GenericException(new Response(response));
    }

    /**
     * Get the number of objects in the header
     *
     * @return Null if the header is not present or the correct value as defined by the header
     */
    private Integer getContainerObjectCount(final HttpResponse response) {
        Header contCountHeader = response.getFirstHeader(Constants.X_CONTAINER_OBJECT_COUNT);
        if(contCountHeader != null) {
            return Integer.parseInt(contCountHeader.getValue());
        }
        return null;
    }

    /**
     * Get the number of bytes used by the container
     *
     * @return Null if the header is not present or the correct value as defined by the header
     */
    private Long getContainerBytesUsed(final HttpResponse response) {
        Header contBytesUsedHeader = response.getFirstHeader(Constants.X_CONTAINER_BYTES_USED);
        if(contBytesUsedHeader != null) {
            return Long.parseLong(contBytesUsedHeader.getValue());
        }
        return null;
    }

}
