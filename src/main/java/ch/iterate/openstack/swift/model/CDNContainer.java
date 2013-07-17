/*
 * See COPYING for license information.
 */

package ch.iterate.openstack.swift.model;

/**
 * @author lvaughn
 */
public class CDNContainer {

    private Region region;
    private String name;
    private boolean enabled;
    private String userAgentACL;
    private String referrerACL;
    private int ttl;
    private String cdnURL;
    private String sslURL;
    private String streamingURL;
    private String iOSStreamingURL;
    private boolean retainLogs;

    public CDNContainer(Region region) {
        this.region = region;
    }

    public CDNContainer(Region region, String name) {
        this.region = region;
        this.name = name;
    }

    public Region getRegion() {
        return region;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the retainLogs
     */
    public boolean getRetainLogs() {
        return retainLogs;
    }

    /**
     * @param retainLogs the retainLogs to set
     */
    public void setRetainLogs(boolean retainLogs) {
        this.retainLogs = retainLogs;
    }

    /**
     * @return Is this container CDN enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the userAgentACL
     */
    public String getUserAgentACL() {
        return userAgentACL;
    }

    /**
     * @param userAgentACL the userAgentACL to set
     */
    public void setUserAgentACL(String userAgentACL) {
        this.userAgentACL = "".equals(userAgentACL) ? null : userAgentACL;
    }

    /**
     * @return the refererACL
     */
    public String getReferrerACL() {
        return referrerACL;
    }

    /**
     * @param referrerACL the refererACL to set
     */
    public void setReferrerACL(String referrerACL) {
        this.referrerACL = "".equals(referrerACL) ? null : referrerACL;
    }

    /**
     * @return the ttl
     */
    public int getTtl() {
        return ttl;
    }

    /**
     * @param ttl the ttl to set
     */
    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    /**
     * @return the cdnURL
     */
    public String getCdnURL() {
        return cdnURL;
    }

    /**
     * @param cdnURL the cdnURL to set
     */
    public void setCdnURL(String cdnURL) {
        this.cdnURL = cdnURL;
    }

    public String getSslURL() {
        return sslURL;
    }

    public void setSslURL(final String sslURL) {
        this.sslURL = sslURL;
    }

    /**
     * @return The  Streaming URL for accessing content in this container via the CDN
     */
    public String getStreamingURL() {
        return this.streamingURL;
    }

    /**
     * @param streamingURL the streamingURL to set
     */
    public void setStreamingURL(String streamingURL) {
        this.streamingURL = streamingURL;
    }

    public String getiOSStreamingURL() {
        return iOSStreamingURL;
    }

    public void setiOSStreamingURL(final String iOSStreamingURL) {
        this.iOSStreamingURL = iOSStreamingURL;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final CDNContainer that = (CDNContainer) o;
        if(cdnURL != null ? !cdnURL.equals(that.cdnURL) : that.cdnURL != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return cdnURL != null ? cdnURL.hashCode() : 0;
    }
}
