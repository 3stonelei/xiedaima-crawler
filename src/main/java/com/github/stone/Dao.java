package com.github.stone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Lilei
 * @date 2022/7/18-@20:43
 */
public class Dao {

    public static void deleteUrlFromDatabase(Connection connection, String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("delete from links_to_be_processed where link = ?")) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    //从数据库中获取url
    public static String loadUrlFromDatabase(Connection connection) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement("select link from links_to_be_processed limit 1");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    public static void updateProcessedLink(Connection connection, String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into links_have_been_processed values (?)")) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    public static void storeUrlIntoDatabase(Connection connection, String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into links_to_be_processed (link) values (?) ")) {
            statement.setString(1, link);
            statement.executeUpdate();
        }

    }

    public static boolean isProcessedLink(Connection connection, String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from links_have_been_processed where link=?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
            return false;
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    public static void insertNewsIntoDatabase(Connection connection, String url, String title, String content) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into news (title,content,url,create_at,nodified_at) values(?,?,?,now(),now())")) {
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setString(3, url);
            statement.executeUpdate();
        }
    }
}
