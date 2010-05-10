/*
 * Copyright © 2010 Red Hat, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.redhat.rhevm.api.common.resource;

import javax.ws.rs.core.HttpHeaders;

import com.redhat.rhevm.api.model.BaseResource;

import com.redhat.rhevm.api.common.util.MutabilityAssertor;


public abstract class AbstractUpdatableResource<R extends BaseResource> {

    protected static final String[] STRICTLY_IMMUTABLE = {"id"};


    protected R model;

    public AbstractUpdatableResource(R model) {
        this.model = model;
    }

    /**
     * Validate update from an immutability point of view.
     *
     * @param incoming  the incoming resource representation
     * @param existing  the existing resource representation
     * @param headers   the incoming HTTP headers
     * @throws WebApplicationException wrapping an appropriate response
     * iff an immutability constraint has been broken
     */
    protected void validateUpdate(R incoming, R existing, HttpHeaders headers) {
        MutabilityAssertor.validateUpdate(getStrictlyImmutable(), incoming, existing, headers);
    }

    /**
     * Override this method if any additional resource-specific fields are
     * strictly immutable
     *
     * @return array of strict immutable field names
     */
    protected String[] getStrictlyImmutable() {
        return STRICTLY_IMMUTABLE;
    }

    public abstract R getModel();

    protected synchronized R setModel(R model) {
        return this.model = model;
    }
}