package ch.iterate.openstack.swift.method;

import org.apache.http.client.methods.HttpGet;

import java.net.URI;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.Constants;

public class Authentication10UsernameKeyRequest extends HttpGet implements AuthenticationRequest {

    public Authentication10UsernameKeyRequest(final URI uri, final String username, final String key) {
        super(uri);
        this.setHeader(Constants.X_STORAGE_USER_DEFAULT, username);
        this.setHeader(Constants.X_STORAGE_PASS_DEFAULT, key);
    }

    public Client.AuthVersion getVersion() {
        return Client.AuthVersion.v10;
    }
}
