package com.github.stone;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

/**
 * @author Lilei
 * @date 2022/7/19-@22:28
 */
public class JdbcDao implements CrawlerDao {

        private final Connection connection;

        @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
        public JdbcDao() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/crawler_news", "root", "123456");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public void deleteUrlFromDatabase(String link) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement("delete from links_to_be_processed where link = ?")) {
                statement.setString(1, link);
                statement.executeUpdate();
            }
        }

        //从数据库中获取url
        public String loadUrlFromDatabase() throws SQLException {

            try (PreparedStatement statement = connection.prepareStatement("select link from links_to_be_processed limit 1");
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    return resultSet.getString(1);
                }
            }
            return null;
        }

        public void storeUrlIntoDatabase(String link, String sql) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, link);
                statement.executeUpdate();
            }

        }

        public boolean isProcessedLink(String link) throws SQLException {
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

        public void insertNewsIntoDatabase(String url, String title, String content) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement("insert into news (title,content,url,create_at,nodified_at) values(?,?,?,now(),now())")) {
                statement.setString(1, title);
                statement.setString(2, content);
                statement.setString(3, url);
                statement.executeUpdate();
            }
        }

        @Override
        public void storeToBeProcessedLink(String href) throws SQLException {
            storeUrlIntoDatabase(href, "insert into links_to_be_processed (link) values (?) ");
        }

        @Override
        public void storeAlreadyProcessedLink(String link) throws SQLException {
            storeUrlIntoDatabase(link, "insert into links_have_been_processed values (?)");
        }

}
