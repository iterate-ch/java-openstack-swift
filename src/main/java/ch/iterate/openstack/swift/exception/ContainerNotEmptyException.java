/*
 * See COPYING for license information.
 */

package ch.iterate.openstack.swift.exception;

import ch.iterate.openstack.swift.Response;

/**
 * @author lvaughn
 */
public class ContainerNotEmptyException extends GenericException {
    private static final long serialVersionUID = 6928040254314736397L;

    public ContainerNotEmptyException(Response response) {
        super(response);
    }
}
