package com.github.stone;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.stream.Collectors;


/**
 * @author Lilei
 * @date 2022/7/12-@11:13
 */
public class Crawler extends Thread {

    private MybatisDao dao;

    public Crawler(MybatisDao dao) {
        this.dao = dao;
    }

    @Override
    public void run() {

        String link;
        try {
            while ((link = dao.getLinkThenDelete()) != null) {

                if (dao.isProcessedLink(link)) {
                    continue;
                }
                if (isNeedLink(link)) {
                    //需要的链接
                    Document doc = httpGetAndHtmlParse(link);
                    //java8中表达式代替获取页面中链接并存入连接池
                    storeUrlIntoDatabase(doc);
                    //是一个新闻页面就存入数据
                    isNewsPageToStoreData(doc, link);
                    //已处理的链接加入数据库
                    dao.storeAlreadyProcessedLink(link);
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void storeUrlIntoDatabase(Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            if (!href.contains("javascript:") && href != "") {
                dao.storeToBeProcessedLink(href);
            }

        }
    }


    private static Document httpGetAndHtmlParse(String link) throws IOException {
//        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build()).build();

        //先处理链接，将以//开头的之前加上https：
        System.out.println(link);
        if (link.startsWith("//")) {
            link = "https:" + link;
            System.out.println(link);
        }
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("user-agent", " Mozilla/5.0 (WCindows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
        try (CloseableHttpResponse response1 = httpClient.execute(httpGet)) {
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);

            return Jsoup.parse(html);
        }
    }

    private void isNewsPageToStoreData(Document doc, String url) throws SQLException {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags
            ) {
                System.out.println(url);
                String title = articleTags.get(0).child(0).text();
                System.out.println(title);
                String content = articleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
                dao.insertNewsIntoDatabase(url, title, content);
            }
        }
    }

    private static boolean isNeedLink(String link) {
        return !isLoginPageOrUnusedPage(link) && (isNewsPage(link) || isFrontPage(link));
    }

    private static boolean isFrontPage(String link) {
        return "https://sina.cn".equals(link);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn") || link.contains("sina.cn");
    }

    private static boolean isLoginPageOrUnusedPage(String link) {
        return link.contains("passport.sina.cn") && link.contains("guba.sina.cn") &&
                link.startsWith("//guba.sina.cn") && link.startsWith("//cmnt.sina.cn")
                && link.startsWith("//db.auto.sina.cn") && link.contains("auto.sina.cn")
                && link.contains("jiaju.sina.cn") && link.contains("jiaju.sina.cn");
    }
}
