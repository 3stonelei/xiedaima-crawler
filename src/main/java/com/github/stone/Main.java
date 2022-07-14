package com.github.stone;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Lilei
 * @date 2022/7/12-@11:13
 */
public class Main {
    public static void main(String[] args) throws IOException {
        //待处理的链接池
        List<String> linkPool = new ArrayList<>();
        linkPool.add("https://sina.cn");
        //已经处理的链接
        Set<String> processedPool = new HashSet<>();

        while (true) {
            if (linkPool.isEmpty()) {
                break;
            }
            String link = linkPool.remove(linkPool.size() - 1);
            if (processedPool.contains(link)) {
                continue;
            }
            if (isNeedLink(link)) {
                //需要的链接
                Document doc = httpGetAndHtmlParse(link);

                //java8中表达式代替获取页面中链接并存入连接池
                doc.select("a").stream().map(aTag -> aTag.attr("herf")).forEach(linkPool::add);
                //是一个新闻页面就存入数据
                isNewsPageToStoreData(doc);

                processedPool.add(link);
            }

        }
    }

    private static Document httpGetAndHtmlParse(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //先处理链接，将以//开头的之前加上https：
        System.out.println(link);
        if (link.startsWith("//")) {
            link = "https:" + link;
            System.out.println(link);
        }
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("user-agent", " Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);

            return Jsoup.parse(html);
        }
    }

    private static void isNewsPageToStoreData(Document doc) {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags
            ) {
                String title = articleTags.get(0).child(0).text();
                System.out.println(title);
            }
        }
    }

    private static boolean isNeedLink(String link) {
        return !isLoginPage(link) && isNewsPage(link) || isFrontPage(link);
    }

    private static boolean isFrontPage(String link) {
        return "https://sina.cn".equals(link);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isLoginPage(String link) {
        return link.contains("passport.sina.cn");
    }
}
