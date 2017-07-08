/*
 * See COPYING for license information.
 */

package ch.iterate.openstack.swift;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import ch.iterate.openstack.swift.io.ContentLengthInputStream;
import ch.iterate.openstack.swift.io.HttpMethodReleaseInputStream;

public class Response {
    private HttpResponse response;

    public Response(HttpResponse r) {
        response = r;
    }

    /**
     * The HTTP headers from the response
     *
     * @return The headers
     */
    public Header[] getResponseHeaders() {
        return response.getAllHeaders();
    }

    /**
     * The HTTP Status line (both the status code and the status message).
     *
     * @return The status line
     */
    public StatusLine getStatusLine() {
        return response.getStatusLine();
    }

    /**
     * Get the HTTP status code
     *
     * @return The status code
     */
    public int getStatusCode() {
        return response.getStatusLine().getStatusCode();
    }

    /**
     * Get the HTTP status message
     *
     * @return The message portion of the status line
     */
    public String getStatusMessage() {
        return response.getStatusLine().getReasonPhrase();
    }

    public HttpResponse getResponse() {
        return response;
    }

    /**
     * Returns the response body as text
     *
     * @return The response body
     * @throws IOException If an error occurs reading the input stream
     */
    public String getResponseBodyAsString() throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Get the response body as a Stream
     *
     * @return An input stream that will return the response body when read
     * @throws IOException If an error occurs reading the input stream
     */
    public ContentLengthInputStream getResponseBodyAsStream() throws IOException {
        return new ContentLengthInputStream(new HttpMethodReleaseInputStream(response), response.getEntity().getContentLength());
    }

    /**
     * Get the body of the response as a byte array
     *
     * @return The body of the response.
     * @throws IOException If an error occurs reading the input stream
     */
    public byte[] getResponseBody() throws IOException {
        return EntityUtils.toByteArray(response.getEntity());
    }

    /**
     * Returns the specified response header. Note that header-name matching is case insensitive.
     *
     * @param headerName - The name of the header to be returned.
     * @return The specified response header. If the response contained multiple instances of the header, its values will be combined using the ',' separator as specified by RFC2616.
     */
    public Header getResponseHeader(String headerName) {
        return response.getFirstHeader(headerName);
    }

    /**
     * Returns the response headers with the given name. Note that header-name matching is case insensitive.
     *
     * @param headerName - the name of the headers to be returned.
     * @return An array of zero or more headers
     */
    public Header[] getResponseHeaders(String headerName) {
        return response.getHeaders(headerName);
    }

    public String getContentEncoding() {
        return response.getEntity().getContentEncoding().getValue();
    }
}
