/*
 * See COPYING for license information.
 */

package ch.iterate.openstack.swift.exception;

import ch.iterate.openstack.swift.Response;

public class AuthorizationException extends GenericException {
    private static final long serialVersionUID = -3142674319839157198L;

    public AuthorizationException(Response response) {
        super(response);
    }
}
