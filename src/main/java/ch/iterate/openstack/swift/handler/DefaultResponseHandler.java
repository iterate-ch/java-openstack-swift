package ch.iterate.openstack.swift.handler;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import java.io.IOException;

import ch.iterate.openstack.swift.Response;
import ch.iterate.openstack.swift.exception.AuthorizationException;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.exception.NotFoundException;

public class DefaultResponseHandler implements ResponseHandler<Response> {

    public Response handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
        StatusLine statusLine = response.getStatusLine();
        final int statusCode = statusLine.getStatusCode();
        if(statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES) {
            return new Response(response);
        }
        else if(statusCode == HttpStatus.SC_NOT_FOUND) {
            throw new NotFoundException(new Response(response));
        }
        else if(statusCode == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthorizationException(new Response(response));
        }
        throw new GenericException("Unexpected response", response.getAllHeaders(), statusLine);
    }
}
