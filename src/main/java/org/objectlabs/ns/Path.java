package org.objectlabs.ns;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Path {

    private static final String PATH_SEPARATOR = "/";

    public Path() {
        super();
    }

    public Path(String path) {
        if (path != null) {
            if (path.startsWith(PATH_SEPARATOR)) {
                setAbsolute(true);
            }
            setPath(parsePath(path));
        }
    }

    public Path(List<String> path) {
        this(path, false);
    }

    public Path(List<String> path, boolean isAbsolute) {
        setPath(path);
        setAbsolute(isAbsolute);
    }

    private List<String> path;

    public List<String> getPath() {
        if (path == null) path = new ArrayList<String>();
        return(path);
    }

    public void setPath(List<String> value) {
        path = value;
    }

    private boolean absolute = false;

    public boolean isAbsolute() {
        return(absolute);
    }

    public void setAbsolute(boolean value) {
        absolute = value;
    }

    public boolean isEmpty() {
        return(getPath().isEmpty());
    }

    public String getHead() {
        List<String> path = getPath();
        if (path.isEmpty()) {
            return(null);
        }
        return(path.get(0));
    }

    public Path getTail() {
        List<String> path = getPath();
        if (path.isEmpty()) {
            return(new Path());
        }
        return(new Path(path.subList(1, path.size())));
    }

    public Path getParent() {
        List<String> path = getPath();
        if (path.size() < 2) {
            return(null);
        }

        return(new Path(path.subList(0, path.size() - 1), isAbsolute()));
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        if (isAbsolute()) {
            buff.append(PATH_SEPARATOR);
        }

        boolean first = true;
        for (String elem : getPath()) {
            if (!first) {
                buff.append(PATH_SEPARATOR);
            }
            buff.append(elem);
            first = false;
        }

        return(buff.toString());
    }

    protected List<String> parsePath(String path) {
        List result = new ArrayList();

        if (path != null && !path.equals("")) {
            if(path.startsWith("/")) {
                path = path.substring(1);
            }
            String[] pathList = path.split("/");
            if (pathList != null) {
                for (String elem : pathList) {
                    if (elem != null) {
                        result.add(URLDecoder.decode(elem, StandardCharsets.UTF_8));
                    }
                }
            }
        }

        return(result);
    }
}
