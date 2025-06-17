package service;

import model.FileInfo;
import util.CustomHasher;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FileDeduplicationService {
    private final Map<String, List<FileInfo>> duplicates = new HashMap<>();

    public List<FileInfo> findDuplicates(Path root) {
        duplicates.clear();
        try {
            Files.walk(root)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            String hash = CustomHasher.hash(file);
                            long size = Files.size(file);
                            FileInfo info = new FileInfo(file, size, hash);
                            duplicates.computeIfAbsent(hash, k -> new ArrayList<>()).add(info);
                        } catch (IOException ignored) {}
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<FileInfo> result = new ArrayList<>();
        for (List<FileInfo> group : duplicates.values()) {
            if (group.size() > 1) result.addAll(group);
        }
        return result;
    }

    public int deleteFoundDuplicates() {
        int deleted = 0;
        for (List<FileInfo> group : duplicates.values()) {
            if (group.size() > 1) {
                for (int i = 1; i < group.size(); i++) {
                    try {
                        Files.deleteIfExists(group.get(i).getPath());
                        deleted++;
                    } catch (IOException ignored) {}
                }
            }
        }
        return deleted;
    }
}
