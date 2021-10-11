package ru.justnanix.wave;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import org.yaml.snakeyaml.Yaml;
import ru.justnanix.wave.bot.Bot;
import ru.justnanix.wave.parser.NicksParser;
import ru.justnanix.wave.parser.ProxyParser;
import ru.justnanix.wave.parser.ServerParser;
import ru.justnanix.wave.utils.Options;
import ru.justnanix.wave.utils.Statistics;
import ru.justnanix.wave.utils.StringGenerator;
import ru.justnanix.wave.utils.ThreadUtils;

import java.io.File;
import java.io.FileInputStream;
import java.net.Proxy;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Wave {
    private static Wave instance;

    private final Random random = new Random(System.currentTimeMillis());

    private final ServerParser serverParser = new ServerParser();
    private final ProxyParser proxyParser = new ProxyParser();
    private final NicksParser nicksParser = new NicksParser();

    {
        System.out.println(" -> [ Wave v1.4 by JustNanix ] <- \n");
        System.setProperty("socksProxyVersion", "4");

        try {
            new ProcessBuilder("cmd", "/c", "title Wave v1.4").inheritIO().start().waitFor();
        } catch (Exception ignored) {}

        try {
            Map<String, Object> values = new Yaml().load(new FileInputStream("config.yml"));

            {
                Options.infoFormat = (int) values.get("infoFormat");

                Options.botsCount = (int) values.get("botsCount");
                Options.joinDelay = (int) values.get("joinDelay");

                Options.randomNicks = (boolean) values.get("randomNicks");
                Options.randomNicksLength = (int) values.get("randomNicksLength");

                Options.randomPasswords = (boolean) values.get("randomPasswords");
                Options.randomPasswordsLength = (int) values.get("randomPasswordsLength");

                Options.doubleJoin = (boolean) values.get("doubleJoin");
                Options.antiBotFilter = (boolean) values.get("antiBotFilter");

                Options.testMode = (boolean) values.get("testMode");
                Options.testModeIp = (String) values.get("testModeIp");

                Options.autoRestart = (boolean) values.get("autoRestart");
                Options.autoRestartDelay = (int) values.get("autoRestartDelay");

                Options.move = (boolean) values.get("move");

                Options.commands = (ArrayList) values.get("commands");
            }

            instance = this;
        } catch (Exception e) {
            System.out.println(" * (System) При загрузке конфига возникла ошибка: "+e+"!");
            ThreadUtils.sleep(2000L);
            System.exit(0);
        }

        System.err.close();
    }

    public void launch() {
        System.out.println(" * (System) Запуск Wave...");

        proxyParser.init();
        serverParser.init();
        nicksParser.init();

        if (Options.autoRestart) {
            new Thread(() -> {
                try {
                    ThreadUtils.sleep(TimeUnit.MINUTES.toMillis(Options.autoRestartDelay));

                    String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                    String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8).substring(1).replace("/", "\\");

                    File java = new File(".jdk/bin/java.exe");
                    if (java.exists()) {
                        Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "cmd", "/k", "\".jdk/bin/java.exe\" -Xmx2G -server -jar "+decodedPath});
                    } else {
                        Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "cmd", "/k", "java -Xmx2G -server -jar "+decodedPath});
                    }

                    System.exit(0);
                } catch (Exception ignored) {}
            }).start();
        }

        if (Options.infoFormat > 0) {
            new Thread(() -> {
                while (true) {
                    ThreadUtils.sleep(2500L);

                    System.out.printf(" * (Info) Ботов онлайн: %s | Решено капч: %s | Сообщений отправлено: %s %n", Statistics.botList.size(), Statistics.solvedCaptcha.get(), Statistics.messagesSent.get());
                }
            }).start();
        }

        while (true) {
            Proxy proxy = proxyParser.nextProxy();

            String server = Options.testMode ? Options.testModeIp : serverParser.nextServer();
            String nick = Options.randomNicks ? StringGenerator.generateStringInt(Options.randomNicksLength) : nicksParser.nextNick();

            for (int i = 0; i < Options.botsCount; i++) {
                try {
                    String host = server.split(":")[0];
                    int port = Integer.parseInt(server.split(":")[1]);

                    new Thread(
                            () -> new Bot(new MinecraftProtocol(nick), host, port, proxy).connect()
                    ).start();
                } catch (OutOfMemoryError ignored) {}
            }

            ThreadUtils.sleep(Options.joinDelay);
        }
    }

    public static Wave getInstance() {
        return instance;
    }

    public ServerParser getServerParser() {
        return serverParser;
    }

    public ProxyParser getProxyParser() {
        return proxyParser;
    }

    public NicksParser getNicksParser() {
        return nicksParser;
    }

    public Random getRandom() {
        return random;
    }
}
