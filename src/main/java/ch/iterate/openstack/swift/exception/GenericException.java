/*
 * See COPYING for license information.
 */

package ch.iterate.openstack.swift.exception;

import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;

import ch.iterate.openstack.swift.Response;

/**
 * @author lvaughn
 */
public class GenericException extends ClientProtocolException {
    private static final long serialVersionUID = -3302541176431848365L;

    private Header[] httpHeaders;
    private StatusLine httpStatusLine;

    /**
     * An exception generated when a openstack tries to do something they aren't authorized to do.
     *
     * @param message        The message
     * @param httpHeaders    The returned HTTP headers
     * @param httpStatusLine The HTTP Status lined returned
     */
    public GenericException(String message, Header[] httpHeaders, StatusLine httpStatusLine) {
        super(message);
        this.httpHeaders = httpHeaders;
        this.httpStatusLine = httpStatusLine;
    }

    public GenericException(String message, Throwable cause) {
        super(message, cause);
    }

    public GenericException(Response response) {
        this(response.getStatusLine().getReasonPhrase(), response.getResponseHeaders(), response.getStatusLine());
    }

    /**
     * @return The HTTP headers returned by the server
     */
    public Header[] getHttpHeaders() {
        return httpHeaders;
    }

    /**
     * @return The HTTP Headers returned by the server in a human-readable string.
     */
    public String getHttpHeadersAsString() {
        StringBuilder httpHeaderString = new StringBuilder();
        for(Header h : httpHeaders) {
            httpHeaderString.append(h.getName()).append(": ").append(h.getValue()).append("\n");
        }
        return httpHeaderString.toString();
    }

    /**
     * @return The HTTP status line from the server
     */
    public StatusLine getHttpStatusLine() {
        return httpStatusLine;
    }

    /**
     * @return The numeric HTTP status code from the server
     */
    public int getHttpStatusCode() {
        return httpStatusLine.getStatusCode();
    }

    /**
     * @return The HTTP status message from the server
     */
    public String getHttpStatusMessage() {
        return httpStatusLine.getReasonPhrase();
    }

    /**
     * @return The version of HTTP used.
     */
    public String getHttpVersion() {
        return httpStatusLine.getProtocolVersion().toString();
    }

}
