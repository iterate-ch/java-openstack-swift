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
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.exception.NotFoundException;
import ch.iterate.openstack.swift.model.CDNContainer;
import ch.iterate.openstack.swift.model.Region;

public class CdnContainerInfoHandler implements ResponseHandler<CDNContainer> {

    private Region region;
    private String container;

    public CdnContainerInfoHandler(final Region region, String container) {
        this.region = region;
        this.container = container;
    }

    public CDNContainer handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT ||
                response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            CDNContainer result = new CDNContainer(region, container);
            result.setCdnURL(this.getCdnUrl(response));
            result.setSslURL(this.getCdnSslUrl(response));
            result.setStreamingURL(this.getCdnStreamingUrl(response));
            result.setiOSStreamingURL(this.getCdnIosStreamingUrl(response));
            for(Header header : response.getAllHeaders()) {
                String name = header.getName().toLowerCase();
                if(Constants.X_CDN_ENABLED.equalsIgnoreCase(name)) {
                    result.setEnabled(Boolean.valueOf(header.getValue()));
                }
                else if(Constants.X_CDN_RETAIN_LOGS.equalsIgnoreCase(name)) {
                    result.setRetainLogs(Boolean.valueOf(header.getValue()));
                }
                else if(Constants.X_CDN_TTL.equalsIgnoreCase(name)) {
                    result.setTtl(Integer.parseInt(header.getValue()));
                }
                else if(Constants.X_CDN_REFERRER_ACL.equalsIgnoreCase(name)) {
                    result.setReferrerACL(header.getValue());
                }
                else if(Constants.X_CDN_USER_AGENT_ACL.equalsIgnoreCase(name)) {
                    result.setUserAgentACL(header.getValue());
                }
            }
            return result;
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

    /**
     * Get the URL For a shared container
     *
     * @return null if the header is not present or the correct value as defined by the header
     */
    private String getCdnUrl(final HttpResponse response) {
        Header cdnHeader = response.getFirstHeader(Constants.X_CDN_URI);
        if(cdnHeader != null) {
            return cdnHeader.getValue();
        }
        return null;
    }

    /**
     * Get the SSL URL For a shared container
     *
     * @return null if the header is not present or the correct value as defined by the header
     */
    private String getCdnSslUrl(final HttpResponse response) {
        Header cdnHeader = response.getFirstHeader(Constants.X_CDN_SSL_URI);
        if(cdnHeader != null) {
            return cdnHeader.getValue();
        }
        return null;
    }

    /**
     * Get the SSL URL For a shared container
     *
     * @return null if the header is not present or the correct value as defined by the header
     */
    private String getCdnStreamingUrl(final HttpResponse response) {
        Header cdnHeader = response.getFirstHeader(Constants.X_CDN_Streaming_URI);
        if(cdnHeader != null) {
            return cdnHeader.getValue();
        }
        return null;
    }

    private String getCdnIosStreamingUrl(final HttpResponse response) {
        Header cdnHeader = response.getFirstHeader(Constants.X_CDN_IOS_URI);
        if(cdnHeader != null) {
            return cdnHeader.getValue();
        }
        return null;
    }
}
