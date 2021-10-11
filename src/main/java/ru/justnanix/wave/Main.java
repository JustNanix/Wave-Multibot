package ru.justnanix.wave;

import java.nio.file.*;
import java.io.File;

public class Main {

    public static void main (String[] args) {
        try {
            File config = new File("config.yml");
            if (!config.exists()) {
                Path zipfile = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                FileSystem fs = FileSystems.newFileSystem(zipfile, null);
                Files.copy(fs.getPath("/config.yml"), config.toPath());
            }
        } catch (Exception ignored) {}

        if (System.console() == null) {
            try {
				File java = new File(".jdk/bin/java.exe");
                String jar = Main.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
				if (java.exists()) {
					Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "cmd", "/k", "\".jdk/bin/java.exe\" -Xmx2G -server -jar "+jar});
				} else {
					Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "cmd", "/k", "java -Xmx2G -server -jar "+jar});
				}
            } catch (Exception ignored) {}
        } else {
            new Wave().launch();
        }
    }
}
