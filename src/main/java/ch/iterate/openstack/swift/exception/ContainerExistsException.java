/*
 * See COPYING for license information.
 */

package ch.iterate.openstack.swift.exception;

import ch.iterate.openstack.swift.Response;

/**
 * @author lvaughn
 */
public class ContainerExistsException extends GenericException {
    private static final long serialVersionUID = 7282149064519490145L;

    public ContainerExistsException(Response response) {
        super(response);
    }

}
