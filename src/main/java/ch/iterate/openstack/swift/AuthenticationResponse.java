package ch.iterate.openstack.swift;

import org.apache.http.HttpResponse;

import java.util.Set;

import ch.iterate.openstack.swift.model.Region;

/**
 * @version $Id:$
 */
public class AuthenticationResponse extends Response {

    protected String authToken;
    private Set<Region> regions;

    public AuthenticationResponse(final HttpResponse r, final String authToken, final Set<Region> regions) {
        super(r);
        this.authToken = authToken;
        this.regions = regions;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Set<Region> getRegions() {
        return regions;
    }
}
