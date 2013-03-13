/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// DBRef.java

package com.mongodb;

import org.bson.BSONObject;

/**
 * overrides DBRefBase to understand a BSONObject representation of a reference.
 *
 * @dochub dbrefs
 */
public class DBRef extends DBRefBase {
    /**
     * Creates a DBRef
     *
     * @param db the database
     * @param o  a BSON object representing the reference
     */
    public DBRef(final DB db, final BSONObject o) {
        super(db, o.get("$ref").toString(), o.get("$id"));
    }

    /**
     * Creates a DBRef
     *
     * @param db the database
     * @param ns the namespace where the object is stored
     * @param id the object id
     */
    public DBRef(final DB db, final String ns, final Object id) {
        super(db, ns, id);
    }

    /**
     * fetches a referenced object from the database
     *
     * @param db  the database
     * @param ref the reference
     * @return
     * @throws MongoException
     */
    public static DBObject fetch(final DB db, final DBObject ref) {
        final String ns;
        final Object id;

        if ((ns = (String) ref.get("$ref")) != null && (id = ref.get("$id")) != null) {
            return db.getCollection(ns).findOne(new BasicDBObject("_id", id));
        }
        return null;
    }
}