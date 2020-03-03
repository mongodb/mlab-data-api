package org.objectlabs.mongodb;

import java.util.ArrayList;
import java.util.Collection;


public class MongoUtils {

    public static final String ADMIN_DB_NAME = "admin";
    public static final String LOCAL_DB_NAME = "local";

    public static final Collection<String> SYSTEM_DB_NAMES = new ArrayList<String>();
    static {
        SYSTEM_DB_NAMES.add(LOCAL_DB_NAME);
        SYSTEM_DB_NAMES.add(ADMIN_DB_NAME);
        SYSTEM_DB_NAMES.add("config");
    }

    public static final Collection<String> READ_ONLY_DB_NAMES = new ArrayList<String>();
    static {
        READ_ONLY_DB_NAMES.add("config");
        READ_ONLY_DB_NAMES.add(LOCAL_DB_NAME);
    }

    public static final Collection<String> READ_ONLY_COLLECTION_NAMES = new ArrayList<String>();
    static {
        READ_ONLY_COLLECTION_NAMES.add("system.indexes");
        READ_ONLY_COLLECTION_NAMES.add("system.namespaces");
        READ_ONLY_COLLECTION_NAMES.add("objectlabs-system");
    }

}

