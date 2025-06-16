package model;

import java.nio.file.Path;

public class FileInfo {
    private Path path;
    private long size;
    private String hash;

    public FileInfo(Path path, long size, String hash) {
        this.path = path;
        this.size = size;
        this.hash = hash;
    }

    public Path getPath() { return path; }
    public long getSize() { return size; }
    public String getHash() { return hash; }
}
