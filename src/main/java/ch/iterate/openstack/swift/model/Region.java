/*
 * See COPYING for license information.
 */

package ch.iterate.openstack.swift.model;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.util.List;

public class Region {

    private final String regionId;
    private final URI storageURL;
    private final URI cdnManagementURL;
    private final boolean isDefault;

    public Region(String regionId, URI storageURL, URI cdnManagementURL) {
        this(regionId, storageURL, cdnManagementURL, false);
    }

    public Region(String regionId, URI storageURL, URI cdnManagementURL, boolean isDefault) {
        this.regionId = regionId;
        this.storageURL = storageURL;
        this.cdnManagementURL = cdnManagementURL;
        this.isDefault = isDefault;
    }

    public String getRegionId() {
        return regionId;
    }

    public URI getStorageUrl() {
        return storageURL;
    }

    public URI getStorageUrl(List<NameValuePair> parameters) {
        return URI.create(String.format("%s?%s", this.getStorageUrl(), URLEncodedUtils.format(parameters, "UTF-8")));
    }

    public URI getStorageUrl(String container) {
        return URI.create(this.getStorageUrl() + "/" + encode(container));
    }

    public URI getStorageUrl(String container, List<NameValuePair> parameters) {
        return URI.create(String.format("%s?%s", this.getStorageUrl(container), URLEncodedUtils.format(parameters, "UTF-8")));
    }

    public URI getStorageUrl(String container, String object) {
        return URI.create(this.getStorageUrl() + "/" + encode(container) + "/" + encode(object));
    }

    public URI getStorageUrl(String container, String object, List<NameValuePair> parameters) {
        return URI.create(String.format("%s?%s", this.getStorageUrl(container, object), URLEncodedUtils.format(parameters, "UTF-8")));
    }

    public URI getCDNManagementUrl() {
        return cdnManagementURL;
    }

    public URI getCDNManagementUrl(List<NameValuePair> parameters) {
        return URI.create(String.format("%s?%s", this.getCDNManagementUrl(), URLEncodedUtils.format(parameters, "UTF-8")));
    }

    public URI getCDNManagementUrl(String container) {
        return URI.create(this.getCDNManagementUrl() + "/" + encode(container));
    }

    public URI getCDNManagementUrl(String container, String object) {
        return URI.create(this.getCDNManagementUrl() + "/" + encode(container) + "/" + encode(object, true));
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final Region that = (Region) o;
        if(regionId != null ? !regionId.equals(that.regionId) : that.regionId != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return regionId != null ? regionId.hashCode() : 0;
    }

    /**
     * Encode any unicode characters that will cause us problems.
     *
     * @param name URI to encode
     * @return The string encoded for a URI
     */
    private static String encode(String name) {
        return encode(name, false);
    }

    private static String encode(String object, boolean preserveslashes) {
        URLCodec codec = new URLCodec();
        try {
            final String encoded = codec.encode(object).replaceAll("\\+", "%20");
            if(preserveslashes) {
                return encoded.replaceAll("%2F", "/");
            }
            return encoded;
        }
        catch(EncoderException ee) {
            return object;
        }
    }
}
