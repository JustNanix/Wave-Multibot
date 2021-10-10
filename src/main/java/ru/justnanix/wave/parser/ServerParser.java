package ru.justnanix.wave.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.justnanix.wave.Wave;
import ru.justnanix.wave.utils.FindUtils;
import ru.justnanix.wave.utils.Options;
import ru.justnanix.wave.utils.ThreadUtils;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class ServerParser {
    private List<String> servers = new CopyOnWriteArrayList<>();
    private int number = -1;

    public void init() {
        if (Options.testMode)
            return;

        System.out.println("\n * (ServerParser) -> Парсинг сервера...\n");

        parseServers(true);
        servers.removeIf(str -> !str.matches("\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?):\\d{1,5}\\b"));

        System.out.printf(" * (ServerParser) -> В сумме загружено %s серверов.\n\n", servers.size());

        new Thread(() -> {
            while (true) {
                ThreadUtils.sleep(10000L);
                parseServers(false);
            }
        }).start();
    }

    private void parseServers(boolean print) {
        String pattern = "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?):\\d{1,5}\\b";

        // ------------------------------------------------------------ monitoringminecraft ------------------------------------------------------------

        try {
            if (print)
                System.out.println(" * (ServerParser) -> Парсинг monitoringminecraft");

            List<String> temp = new CopyOnWriteArrayList<>();

            Document document = Jsoup.connect("https://monitoringminecraft.ru/novie-servera-1.12.2").get();
            Elements elements = document.getElementsByAttributeValue("class", "server");

            for (Element element : elements)
                temp.addAll(FindUtils.findStringsByRegex(element.text(), Pattern.compile(pattern)));

            temp.removeIf(str -> !str.matches("\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?):\\d{1,5}\\b"));
            servers.addAll(temp);

            if (print)
                System.out.printf(" * (ServerParser) -> Загружено %s серверов с monitoringminecraft.\n\n", temp.size());
        } catch (Throwable ignored) {}

        // ------------------------------------------------------------ minecraftrating ------------------------------------------------------------

        try {
            if (print)
                System.out.println(" * (ServerParser) -> Парсинг minecraftrating");

            List<String> temp = new CopyOnWriteArrayList<>();

            Document document = Jsoup.connect("https://minecraftrating.ru/new-servers/1.12.2").get();
            Elements elements = document.getElementsByAttributeValue("class", "tooltip");

            parseMethod1(elements, temp);
            servers.addAll(temp);

            if (print)
                System.out.printf(" * (ServerParser) -> Загружено %s серверов с minecraftrating.\n\n", temp.size());
        } catch (Throwable ignored) {}

        // ------------------------------------------------------------ misterlauncher ------------------------------------------------------------

        try {
            if (print)
                System.out.println(" * (ServerParser) -> Парсинг misterlauncher");

            List<String> temp = new CopyOnWriteArrayList<>();

            Document document = Jsoup.connect("https://misterlauncher.org/servera-novye/").get();
            Elements elements = document.getElementsByAttributeValue("data-toggle", "tooltip");

            parseMethod1(elements, temp);
            servers.addAll(temp);

            if (print)
                System.out.printf(" * (ServerParser) -> Загружено %s серверов с misterlauncher.\n\n", temp.size());
        } catch (Throwable ignored) {}

        // ------------------------------------------------------------ tmonitoring ------------------------------------------------------------

        try {
            if (print)
                System.out.println(" * (ServerParser) -> Парсинг tmonitoring");

            List<String> temp = new CopyOnWriteArrayList<>();

            Document document = Jsoup.connect("https://tmonitoring.com/servers-new/").get();
            Elements elements = document.getElementsByAttributeValue("class", "ip btn-copy-html");

            parseMethod1(elements, temp);
            servers.addAll(temp);

            if (print)
                System.out.printf(" * (ServerParser) -> Загружено %s серверов с tmonitoring.\n\n", temp.size());
        } catch (Throwable ignored) {}

        // ------------------------------------------------------------ minecraftstatistics ------------------------------------------------------------

        try {
            if (print)
                System.out.println(" * (ServerParser) -> Парсинг minecraftstatistics");

            List<String> temp = new CopyOnWriteArrayList<>();

            Document document = Jsoup.connect("https://minecraft-statistic.net/ru/servers-new/").get();
            Elements elements = document.getElementsByAttributeValue("class", "copy-ip f-700");

            parseMethod1(elements, temp);
            servers.addAll(temp);

            if (print)
                System.out.printf(" * (ServerParser) -> Загружено %s серверов с minecraftstatistics.\n\n", temp.size());
        } catch (Throwable ignored) {}

        servers = new CopyOnWriteArrayList<>(new HashSet<>(servers));
        Collections.shuffle(servers, Wave.getInstance().getRandom());
    }

    public void parseMethod1(Elements elements, List<String> temp) {
        String pattern = "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?):\\d{1,5}\\b";

        for (Element element : elements) {
            try {
                String text = element.ownText();

                if (!text.matches(".*[A-z].*") && FindUtils.findStringByRegex(text, Pattern.compile(pattern)) != null) {
                    try {
                        temp.add(FindUtils.findStringByRegex(text, Pattern.compile(pattern)));
                    } catch (Exception ignored) {}
                } else {
                    try {
                        if (text.contains(":")) {
                            temp.add(InetAddress.getByName(text.split(":")[0]).getHostAddress() + ":" + text.split(":")[1]);
                        } else {
                            temp.add(InetAddress.getByName(text).getHostAddress() + ":25565");
                        }
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
        }

        temp = new CopyOnWriteArrayList<>(new HashSet<>(temp));
        temp.removeIf(str -> !str.matches("\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?):\\d{1,5}\\b"));
    }

    public String nextServer() {
        ++number;

        if (number >= servers.size())
            number = 0;

        return servers.get(number);
    }

    public List<String> getServers() {
        return servers;
    }
}
