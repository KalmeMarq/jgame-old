package me.kalmemarq.jgame.common;

import me.kalmemarq.jgame.common.logger.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public enum OperatingSystem {
    WINDOWS("windows") {
        @Override
        protected String[] getOpenURLCommand(URL url) {
            return new String[] { "rundll32", "url.dll,FileProtocolHandler", url.toString() };
        }
    },
    MACOS("macos") {
        @Override
        protected String[] getOpenURLCommand(URL url) {
            return new String[] { "open", url.toString() };
        }
    },
    LINUX("linux"),
    SOLARIS("solaris"),
    UNKNOWN("unknown");

    public static final Logger LOGGER = Logger.getLogger("Util/OS");
    
    private final String name;
    
    OperatingSystem(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getAppData() {
        return switch (this) {
            case WINDOWS, LINUX -> System.getenv("APPDATA");
            case MACOS, SOLARIS, UNKNOWN -> System.getProperty("user.home") + "/Library/Application Support";
        };
    }

    protected String[] getOpenURLCommand(URL url) {
        String str = url.toString();
        if (url.getProtocol().equals("file")) {
            str = str.replace("file:", "file://");
        }

        return new String[] { "xdg-open", str };
    }

    private void openURL(URL url) throws IOException {
        Process process = Runtime.getRuntime().exec(this.getOpenURLCommand(url));
        process.getInputStream().close();
        process.getErrorStream().close();
        process.getOutputStream().close();
    }

    public void open(URL url) {
        CompletableFuture.runAsync(() -> {
            try {
                this.openURL(url);
            } catch (IOException e) {
                LOGGER.error("Failed to open url {}", url, e);
            }
        });
    }

    public void open(URI uri) {
        CompletableFuture.runAsync(() -> {
            try {
                this.openURL(uri.toURL());
            } catch (IOException e) {
                LOGGER.error("Failed to open uri {}", uri, e);
            }
        });
    }

    public void open(File file) {
        CompletableFuture.runAsync(() -> {
            try {
                this.openURL(file.toURI().toURL());
            } catch (IOException e) {
                LOGGER.error("Failed to open file {}", file, e);
            }
        });
    }

    public void open(Path path) {
        CompletableFuture.runAsync(() -> {
            try {
                this.openURL(path.toUri().toURL());
            } catch (IOException e) {
                LOGGER.error("Failed to open path {}", path, e);
            }
        });
    }

    public void open(String uri) {
        CompletableFuture.runAsync(() -> {
            try {
                this.openURL(new URI(uri).toURL());
            } catch (IOException | URISyntaxException e) {
                LOGGER.error("Failed to open uri {}", uri, e);
            }
        });
    }

    @Nullable
    private static OperatingSystem CACHE;
    
    public static OperatingSystem get() {
        if (CACHE != null) {
            return CACHE;
        }

        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            CACHE = OperatingSystem.WINDOWS;
        } else if (os.contains("mac") || os.contains("darwin")) {
            CACHE = OperatingSystem.MACOS;
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix") || os.contains("linux")) {
            CACHE = OperatingSystem.LINUX;
        } else if (os.contains("sunos")) {
            CACHE = OperatingSystem.SOLARIS;
        } else {
            CACHE = OperatingSystem.UNKNOWN;
        }

        return CACHE;
    }
}
