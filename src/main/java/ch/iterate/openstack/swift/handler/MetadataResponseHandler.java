package ch.iterate.openstack.swift.handler;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;

public abstract class MetadataResponseHandler {


    /**
     * The Etag is the same as the objects MD5SUM
     *
     * @return The ETAG
     */
    protected String getETag(final HttpResponse response) {
        Header hdr = response.getFirstHeader(HttpHeaders.ETAG);
        if(hdr == null) {
            return null;
        }
        return hdr.getValue();
    }

    /**
     * The last modified header
     *
     * @return The last modified header
     */
    protected String getLastModified(final HttpResponse response) {
        Header hdr = response.getFirstHeader(HttpHeaders.LAST_MODIFIED);
        if(null == hdr) {
            return null;
        }
        return hdr.getValue();
    }

    /**
     * The X-Timestamp header
     *
     * @return The X-Timestamp header
     */
    protected String getTimestamp(final HttpResponse response) {
        Header hdr = response.getFirstHeader("X-Timestamp");
        if(null == hdr) {
            return null;
        }
        return hdr.getValue();
    }

    /**
     * Get the content type
     *
     * @return The content type (e.g., MIME type) of the response
     */
    protected String getContentType(final HttpResponse response) {
        Header hdr = response.getFirstHeader(HttpHeaders.CONTENT_TYPE);
        if(null == hdr) {
            return null;
        }
        return hdr.getValue();
    }

    /**
     * Get the content length of the response (as reported in the header)
     *
     * @return the length of the content
     */
    protected String getContentLength(final HttpResponse response) {
        Header hdr = response.getFirstHeader(HttpHeaders.CONTENT_LENGTH);
        if(hdr == null) {
            return "0";
        }
        return hdr.getValue();
    }
}
