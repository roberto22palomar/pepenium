package io.github.roberto22palomar.pepenium.core.observability;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class AtomicArtifactWriter {

    private AtomicArtifactWriter() {
    }

    static void writeString(Path target, String content, Charset charset) throws IOException {
        Path absoluteTarget = target.toAbsolutePath().normalize();
        Path parent = absoluteTarget.getParent();
        if (parent == null) {
            throw new IOException("Cannot atomically write a path without a parent: " + target);
        }

        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, "." + absoluteTarget.getFileName(), ".tmp");
        try {
            Files.writeString(temporary, content, charset);
            moveIntoPlace(temporary, absoluteTarget);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void moveIntoPlace(Path temporary, Path target) throws IOException {
        try {
            Files.move(temporary, target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
