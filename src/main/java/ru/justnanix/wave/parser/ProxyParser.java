package ru.justnanix.wave.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import ru.justnanix.wave.Wave;
import ru.justnanix.wave.utils.ThreadUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ProxyParser {
    private List<Proxy> proxies = new CopyOnWriteArrayList<>();
    private int number = -1;

    public void init() {
        System.out.println("\n * (ProxyParser) -> Парсинг прокси...");

        try {
            File proxyFile = new File("proxy.txt");
            if (proxyFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(proxyFile))) {
                    while (reader.ready()) {
                        try {
                            String line = reader.readLine();
                            proxies.add(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(line.split(":")[0], Integer.parseInt(line.split(":")[1]))));
                        } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}

                System.out.printf(" * (ProxyParser) -> Загружено %s прокси.\n", proxies.size());
                return;
            }

            try {
                Document proxyList = Jsoup.connect("https://api.proxyscrape.com/?request=displayproxies&proxytype=socks4").get();
                proxies.addAll(Arrays.stream(proxyList.text().split(" ")).distinct().map((proxy) -> new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxy.split(":")[0], Integer.parseInt(proxy.split(":")[1])))).collect(Collectors.toList()));
            } catch (Throwable ignored) {}

            try {
                Document proxyList = Jsoup.connect("https://www.proxy-list.download/api/v1/get?type=socks4").get();
                proxies.addAll(Arrays.stream(proxyList.text().split(" ")).distinct().map((proxy) -> new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxy.split(":")[0], Integer.parseInt(proxy.split(":")[1])))).collect(Collectors.toList()));
            } catch (Throwable ignored) {}

            try {
                Document proxyList = Jsoup.connect("https://openproxylist.xyz/socks4.txt").get();
                proxies.addAll(Arrays.stream(proxyList.text().split(" ")).distinct().map((proxy) -> new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxy.split(":")[0], Integer.parseInt(proxy.split(":")[1])))).collect(Collectors.toList()));
            } catch (Throwable ignored) {}

            try {
                for (int k = 64; k < 64 * 25; k += 64) {
                    Document proxyList3 = Jsoup.connect("https://hidemy.name/ru/proxy-list/?type=4&start=" + k + "#list").get();

                    for (int i = 1; i < proxyList3.getElementsByTag("tr").size(); i++) {
                        try {
                            Elements elements = proxyList3.getElementsByTag("tr").get(i).getElementsByTag("td");

                            String host = elements.get(0).text();
                            int port = Integer.parseInt(elements.get(1).text());

                            proxies.add(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port)));
                        } catch (Throwable ignored) {}
                    }
                }
            } catch (Throwable ignored) {}

            proxies = new CopyOnWriteArrayList<>(new HashSet<>(proxies));
            Collections.shuffle(proxies, Wave.getInstance().getRandom());

            System.out.printf(" * (ProxyParser) -> Загружено %s прокси.\n", proxies.size());

            proxyFile.createNewFile();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(proxyFile))) {
                for (Proxy proxy : proxies) {
                    writer.write(proxy.toString().split("/")[1] + "\n");
                }
            }
        } catch (Exception ignored) {}
    }

    public Proxy nextProxy() {
        ++number;

        if (number >= proxies.size())
            number = 0;

        return proxies.get(number);
    }

    public List<Proxy> getProxies() {
        return proxies;
    }
}
