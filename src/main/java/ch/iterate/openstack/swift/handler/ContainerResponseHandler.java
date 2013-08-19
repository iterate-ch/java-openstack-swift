package ch.iterate.openstack.swift.handler;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ch.iterate.openstack.swift.Response;
import ch.iterate.openstack.swift.exception.AuthorizationException;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.exception.NotFoundException;
import ch.iterate.openstack.swift.model.Container;
import ch.iterate.openstack.swift.model.Region;

public class ContainerResponseHandler implements ResponseHandler<List<Container>> {

    private Region region;

    public ContainerResponseHandler(final Region region) {
        this.region = region;
    }

    public List<Container> handleResponse(final HttpResponse response) throws IOException {
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            final StringTokenizer tokenize = new StringTokenizer(EntityUtils.toString(response.getEntity()), "\n");
            final ArrayList<Container> containers = new ArrayList<Container>();
            while(tokenize.hasMoreTokens()) {
                containers.add(new Container(region, tokenize.nextToken()));
            }
            return containers;
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            return new ArrayList<Container>();
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            throw new NotFoundException(new Response(response));
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthorizationException(new Response(response));
        }
        else {
            throw new GenericException(new Response(response));
        }
    }
}
