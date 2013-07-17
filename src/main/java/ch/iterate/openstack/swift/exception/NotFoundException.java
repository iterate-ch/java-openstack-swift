/*
 * See COPYING for license information.
 */

/*
 * See COPYING for license information.
 */

package ch.iterate.openstack.swift.exception;

import ch.iterate.openstack.swift.Response;

/**
 * @author lvaughn
 */
public class NotFoundException extends GenericException {
    private static final long serialVersionUID = 111718445621236026L;

    public NotFoundException(Response response) {
        super(response);
    }

}
