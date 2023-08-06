package me.kalmemarq.jgame.common;

import org.jetbrains.annotations.NotNull;

public class Identifier implements Comparable<Identifier> {
    private final String namespace;
    private final String path;

    private Identifier(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }
    
    private static String[] parseId(String id) {
        String[] arr = { "", id };
        
        int colonIdx = id.indexOf(':');
        
        if (colonIdx >= 0) {
            arr[1] = id.substring(colonIdx + 1);
            
            if (colonIdx > 0) {
                arr[0] = id.substring(0, colonIdx);
            }
        }
        
        if (arr[0].length() == 0) {
            throw new InvalidIdentifierException("'" + id + "' identifier is missing a namespace.");
        }
        
        return arr;   
    }

    public static Identifier of(String id) {
        String[] parsedId = parseId(id);
        return of(parsedId[0], parsedId[1]);
    }

    public static Identifier of(String namespace, String path) {
        if (!StringHelper.isValidString(namespace, Identifier::isNamespaceAllowedChar)) {
            throw new InvalidIdentifierException("'" + namespace +"' is an invalid namespace. It should only contain [a-z0-9_] characters.");
        }

        if (!StringHelper.isValidString(path, Identifier::isPathAllowedChar)) {
            throw new InvalidIdentifierException("'" + path +"' is an invalid path. It should only contain [a-z0-9_/] characters.");
        }
        
        return new Identifier(namespace, path);
    }
    
    public String getNamespace() {
        return this.namespace;
    }

    public String getPath() {
        return this.path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Identifier other)) return false;
        return this.namespace.equals(other.namespace) && this.path.equals(other.path);
    }

    @Override
    public int hashCode() {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }
    
    @Override
    public String toString() {
        return this.namespace + ":" + this.path;
    }

    @Override
    public int compareTo(@NotNull Identifier o) {
        int c = this.namespace.compareTo(o.namespace);
        if (c == 0) c = this.path.compareTo(o.path);
        return c;
    }

    private static boolean isNamespaceAllowedChar(char chr) {
        return (chr >= '0' && chr <= '9') || (chr >= 'a' && chr <= 'z') || chr == '_';
    }

    private static boolean isPathAllowedChar(char chr) {
        return (chr >= '0' && chr <= '9') || (chr >= 'a' && chr <= 'z') || chr == '_' || chr == '/';
    }
}
