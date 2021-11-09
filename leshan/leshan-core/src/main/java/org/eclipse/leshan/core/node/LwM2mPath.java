/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.core.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.leshan.core.util.Validate;

/**
 * A path pointing to a LwM2M node (root, object, object instance, resource or resource instance).
 */
public class LwM2mPath implements Comparable<LwM2mPath> {

    public static final byte ROOT_DEPTH = 1;
    public static final byte OBJECT_DEPTH = 2;
    public static final byte OBJECT_INSTANCE_DEPTH = 3;
    public static final byte RESOURCE_DEPTH = 4;
    public static final byte RESOURCE_INSTANCE_DEPTH = 5;

    private final Integer objectId;
    private final Integer objectInstanceId;
    private final Integer resourceId;
    private final Integer resourceInstanceId;

    public final static LwM2mPath ROOTPATH = new LwM2mPath();

    private LwM2mPath() {
        this(null, null, null, null);
    }

    /**
     * Create a path to an object
     *
     * @param objectId the object identifier
     */
    public LwM2mPath(int objectId) {
        this(objectId, null, null, null);
        validate();
    }

    /**
     * Create a path to an object instance
     *
     * @param objectId the object identifier
     * @param objectInstanceId the instance identifier
     */
    public LwM2mPath(int objectId, int objectInstanceId) {
        this(objectId, objectInstanceId, null, null);
        validate();
    }

    /**
     * Create a path to a resource of a given object instance
     *
     * @param objectId the object identifier
     * @param objectInstanceId the instance identifier
     * @param resourceId the resource identifier
     */
    public LwM2mPath(int objectId, int objectInstanceId, int resourceId) {
        this(objectId, objectInstanceId, resourceId, null);
        validate();
    }

    /**
     * Create a path to a resource instance of a given resource
     *
     * @param objectId the object identifier
     * @param objectInstanceId the instance identifier
     * @param resourceId the resource identifier
     * @param resourceInstanceId the resource instance identifier
     */
    public LwM2mPath(int objectId, int objectInstanceId, int resourceId, int resourceInstanceId) {
        this((Integer) objectId, (Integer) objectInstanceId, (Integer) resourceId, (Integer) resourceInstanceId);
        validate();
    }

    /**
     * Constructs a {@link LwM2mPath} from a string representation
     *
     * @param path the path (e.g. "/3/0/1" or "/3")
     */
    public LwM2mPath(String path) {
        Validate.notNull(path);
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String[] p = path.split("/");
        if (0 > p.length || p.length > 4) {
            throw new IllegalArgumentException("Invalid length for path: " + path);
        }
        try {
            this.objectId = (p.length >= 1 && !p[0].isEmpty()) ? Integer.valueOf(p[0]) : null;
            this.objectInstanceId = (p.length >= 2) ? Integer.valueOf(p[1]) : null;
            this.resourceId = (p.length >= 3) ? Integer.valueOf(p[2]) : null;
            this.resourceInstanceId = (p.length == 4) ? Integer.valueOf(p[3]) : null;
            validate();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid elements in path: " + path, e);
        }
    }

    protected LwM2mPath(Integer objectId, Integer objectInstanceId, Integer resourceId, Integer resourceInstanceId) {
        this.objectId = objectId;
        this.objectInstanceId = objectInstanceId;
        this.resourceId = resourceId;
        this.resourceInstanceId = resourceInstanceId;
    }

    /**
     * Validate the current path and raise {@link IllegalArgumentException} is path is not valid
     * 
     * @see LwM2mNodeUtil#validatePath(LwM2mPath)
     */
    protected void validate() {
        LwM2mNodeUtil.validatePath(this);
    }

    /**
     * @param path the end of the new path
     * @return a new path which is the concatenation of this path and the given one in parameter.
     */
    public LwM2mPath append(String path) {
        LwM2mPath pathToAdd = new LwM2mPath(path);
        if (isRoot()) {
            return pathToAdd;
        } else {
            return new LwM2mPath(this.toString() + pathToAdd.toString());
        }
    }

    /**
     * @param end the end of the new path
     * @return a new path which is the concatenation of this path and the given one in parameter.
     */
    public LwM2mPath append(int end) {
        if (isRoot()) {
            return new LwM2mPath(end);
        } else if (isObject()) {
            return new LwM2mPath(getObjectId(), end);
        } else if (isObjectInstance()) {
            return new LwM2mPath(getObjectId(), getObjectInstanceId(), end);
        } else if (isResource()) {
            return new LwM2mPath((int) getObjectId(), (int) getObjectInstanceId(), (int) getResourceId(), end);
        } else {
            throw new IllegalArgumentException(String.format(
                    "Unable to append Id(%d) to path %s. Resource instance level is the deeper one.", end, this));
        }
    }

    /**
     * @param start start of the path
     * @return true if the current path start with the given path
     */
    public boolean startWith(LwM2mPath start) {
        // object id
        if (start.getObjectId() == null)
            return true;
        if (!start.getObjectId().equals(this.getObjectId())) {
            return false;
        }
        // object instance id
        if (start.getObjectInstanceId() == null)
            return true;
        if (!start.getObjectInstanceId().equals(this.getObjectInstanceId())) {
            return false;
        }
        // resource id
        if (start.getResourceId() == null)
            return true;
        if (!start.getResourceId().equals(this.getResourceId())) {
            return false;
        }
        // resource instance id
        if (start.getResourceInstanceId() == null)
            return true;
        if (!start.getResourceInstanceId().equals(this.getResourceInstanceId())) {
            return false;
        }
        return true;
    }

    /**
     * Returns the object ID in the path.
     *
     * @return the object ID. Can be <code>null</code> when this is an root path.
     */
    public Integer getObjectId() {
        return objectId;
    }

    /**
     * Returns the object instance ID in the path.
     *
     * @return the object instance ID. Can be <code>null</code> when this is an root/object path.
     */
    public Integer getObjectInstanceId() {
        return objectInstanceId;
    }

    /**
     * Returns the resource ID in the request path.
     *
     * @return the resource ID. Can be <code>null</code> when this is a root/object/object instance path.
     */
    public Integer getResourceId() {
        return resourceId;
    }

    /**
     * Returns the resource instance ID in the request path.
     *
     * @return the resource instance ID. Can be <code>null</code> when this is a root/object/object instance/resource
     *         path.
     */
    public Integer getResourceInstanceId() {
        return resourceInstanceId;
    }

    /**
     * @return <code>true</code> if this is the root path ("/").
     */
    public boolean isRoot() {
        return objectId == null && objectInstanceId == null && resourceId == null && resourceInstanceId == null;
    }

    /**
     * @return <code>true</code> if this is an Object path.
     */
    public boolean isObject() {
        return objectId != null && objectInstanceId == null && resourceId == null && resourceInstanceId == null;
    }

    /**
     * @return <code>true</code> if this is an ObjectInstance path.
     */
    public boolean isObjectInstance() {
        return objectId != null && objectInstanceId != null && resourceId == null && resourceInstanceId == null;
    }

    /**
     * @return <code>true</code> if this is a Resource path.
     */
    public boolean isResource() {
        return objectId != null && objectInstanceId != null && resourceId != null && resourceInstanceId == null;
    }

    /**
     * @return <code>true</code> if this is a Resource instance path.
     */
    public boolean isResourceInstance() {
        return objectId != null && objectInstanceId != null && resourceId != null && resourceInstanceId != null;
    }

    /**
     * @return a new {@link LwM2mPath} targeting an object from current path.
     */
    public LwM2mPath toObjectPath() {
        if (getObjectId() != null)
            return new LwM2mPath(getObjectId());
        throw new IllegalStateException(String.format("an object path can not be created from %s", this));
    }

    /**
     * @return a new {@link LwM2mPath} targeting an object instance from current path.
     */
    public LwM2mPath toObjectInstancePath() {
        if (getObjectInstanceId() != null)
            return new LwM2mPath(getObjectId(), getObjectInstanceId());
        throw new IllegalStateException(String.format("an object instance path can not be created from %s", this));
    }

    /**
     * @return a new {@link LwM2mPath} targeting an resource from current path.
     */
    public LwM2mPath toResourcePath() {
        if (getResourceId() != null)
            return new LwM2mPath(getObjectId(), getObjectInstanceId(), getResourceId());
        throw new IllegalStateException(String.format("an resource path can not be created from %s", this));
    }

    /**
     * The string representation of the path: /{Object ID}/{ObjectInstance ID}/{Resource ID}/{ResourceInstance ID}
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("/");
        if (getObjectId() != null) {
            b.append(getObjectId());
            if (getObjectInstanceId() != null) {
                b.append("/").append(getObjectInstanceId());
                if (getResourceId() != null) {
                    b.append("/").append(getResourceId());
                    if (getResourceInstanceId() != null) {
                        b.append("/").append(getResourceInstanceId());
                    }
                }
            }
        }
        return b.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + objectId;
        result = prime * result + ((objectInstanceId == null) ? 0 : objectInstanceId.hashCode());
        result = prime * result + ((resourceId == null) ? 0 : resourceId.hashCode());
        result = prime * result + ((resourceInstanceId == null) ? 0 : resourceInstanceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LwM2mPath other = (LwM2mPath) obj;
        if (objectId == null) {
            if (other.objectId != null) {
                return false;
            }
        } else if (!objectId.equals(other.objectId)) {
            return false;
        }
        if (objectInstanceId == null) {
            if (other.objectInstanceId != null) {
                return false;
            }
        } else if (!objectInstanceId.equals(other.objectInstanceId)) {
            return false;
        }
        if (resourceId == null) {
            if (other.resourceId != null) {
                return false;
            }
        } else if (!resourceId.equals(other.resourceId)) {
            return false;
        }
        if (resourceInstanceId == null) {
            if (other.resourceInstanceId != null) {
                return false;
            }
        } else if (!resourceInstanceId.equals(other.resourceInstanceId)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(LwM2mPath o) {
        int res = compareInteger(this.objectId, o.objectId);
        if (res != 0 || this.objectId == null)
            return res;

        res = compareInteger(this.objectInstanceId, o.objectInstanceId);
        if (res != 0 || this.objectInstanceId == null)
            return res;

        res = compareInteger(this.resourceId, o.resourceId);
        if (res != 0 || this.resourceId == null)
            return res;

        return compareInteger(this.resourceInstanceId, o.resourceInstanceId);
    }

    private int compareInteger(Integer i1, Integer i2) {
        if (i1 == i2) {
            return 0;
        } else if (i1 == null) {
            return -1;
        } else if (i2 == null) {
            return 1;
        } else {
            return i1.compareTo(i2);
        }
    }

    /**
     * Parse a string containing a full LWM2M path containing rootpath (rt="oma.lwm2m").
     * <p>
     * E.g. : fullpath="/myrootpath/1/0 and rootpath="/myrootpat/" will return <code>new LwM2mPath(1,0)</code>
     * 
     * @param fullpath the path to parse.
     * @param lwm2mRootpath the expected rootpath. <code>null</code> is considered as "/"
     * @return A valid {@link LwM2mPath} or null it does not start by lwm2mRootPath
     * 
     * @exception NumberFormatException if path contains not Numeric value
     * @exception LwM2mNodeException if path is invalid (e.g. too big number in path)
     * @exception IllegalArgumentException if path length is invalid
     */
    public static LwM2mPath parse(String fullpath, String lwm2mRootpath)
            throws NumberFormatException, LwM2mNodeException, IllegalArgumentException {
        if (lwm2mRootpath == null) {
            return new LwM2mPath(fullpath);
        }

        if (!fullpath.startsWith(lwm2mRootpath))
            return null;
        String path = fullpath.substring(lwm2mRootpath.length());

        return new LwM2mPath(path);
    }

    /**
     * Create list of LwM2mPath from list of paths
     *
     * @param paths list of paths as {@link String}.
     * @return list of paths as {@link LwM2mPath}.
     *
     * @exception LwM2mNodeException if path is invalid (e.g. too big number in path)
     * @exception IllegalArgumentException if path length is invalid or if path contains not Numeric value
     */
    public static List<LwM2mPath> getLwM2mPathList(List<String> paths) {
        List<LwM2mPath> res = new ArrayList<>(paths.size());
        for (String path : paths) {
            res.add(new LwM2mPath(path));
        }
        return res;
    }
}