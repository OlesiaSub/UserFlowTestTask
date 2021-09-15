package ru.hse.testtasks.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class BasicWebCrawlerImpl {
    private static int enumerator = 0;
    private final UnifiedSet<Integer> links = new UnifiedSet<>();
    private final BufferedWriter writer;
    private final String requestedURL;
    private final String requestedPrefix;

    private BasicWebCrawlerImpl(String requestedURL, String requestedPrefix) throws IOException {
        this.requestedURL = requestedURL;
        this.requestedPrefix = requestedPrefix;
        final File file = new File("output.txt");
        final FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
        writer = new BufferedWriter(fileWriter);
    }

    private void launch() {
        dfsLinks(requestedURL);
    }

    private void dfsLinks(String url) {
        final Stack<String> stack = new Stack<>();
        stack.push(url);
        while (!stack.empty()) {
            try {
                final String currentUrl = stack.peek();
                stack.pop();
                if (!links.contains(currentUrl.hashCode())) {
                    links.add(currentUrl.hashCode());
                    writer.write("URL #" + ++enumerator + ": " + currentUrl + "\nLINKS:\n");
                    final Document document = Jsoup.connect(currentUrl).get();
                    List<String> pageLinks = document.select("a[href]")
                            .stream()
                            .map(element -> element.attr("abs:href"))
                            .distinct()
                            .filter(link -> link.startsWith(requestedPrefix))
                            .collect(Collectors.toList());
                    for (String pageLink : pageLinks) {
                        writer.write(pageLink + "\n");
                        if (!links.contains(pageLink.hashCode())) {
                            stack.push(pageLink);
                        }
                    }
                }
            } catch (IOException | IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void closeResources() throws IOException {
        writer.write("All the links were traversed successfully!\n");
        writer.close();
    }

    /**
     * @param args args[0] - requested URL, starting point of the traversal (e.g. https://www.jetbrains.com/)
     *             args[1] - requested prefix, to traverse the links that start with the specified prefix (e.g. https://www.jetbrains.com/)
     */
    public static void main(String[] args) throws IOException {
        final BasicWebCrawlerImpl crawler = new BasicWebCrawlerImpl(args[0], args[1]);
        crawler.launch();
        crawler.closeResources();
    }

}
