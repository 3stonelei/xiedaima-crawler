package com.github.stone;

import java.sql.SQLException;

/**
 * @author Lilei
 * @date 2022/7/19-@14:24
 */
public interface CrawlerDao {
    void deleteUrlFromDatabase(String link) throws SQLException;

    String loadUrlFromDatabase() throws SQLException;

    void storeUrlIntoDatabase(String link, String sql) throws SQLException;

    boolean isProcessedLink(String link) throws SQLException;

    void insertNewsIntoDatabase(String url, String title, String content) throws SQLException;
}
