package com.github.stone;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * @author Lilei
 * @date 2022/7/19-@19:58
 */
public class MybatisDao implements CrawlerDao {
    private SqlSessionFactory sqlSessionFactory;

    public MybatisDao() {
        String resource = "db/mybatis/config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    public synchronized String getLinkThenDelete() throws SQLException {
        String link = loadUrlFromDatabase();
        if (link != null) {
            deleteUrlFromDatabase(link);
        }
        return link;
    }
    @Override
    public void deleteUrlFromDatabase(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.delete("com.github.stone.MyMapper.deleteLink", link);
        }
    }

    @Override
    public  String loadUrlFromDatabase() throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectOne("com.github.stone.MyMapper.selectLink");
        }
    }

    @Override
    public boolean isProcessedLink(String link) throws SQLException {
        int count;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            count = session.selectOne("com.github.stone.MyMapper.selectProcessedLink", link);
        }
        return count != 0;
    }

    @Override
    public void insertNewsIntoDatabase(String url, String title, String content) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.stone.MyMapper.insertNews", new News(title, content,url));
        }
    }

    @Override
    public void storeToBeProcessedLink(String href) throws SQLException {
        HashMap<String, Object> param = new HashMap<>();
        param.put("table_name", "link_to_be_processed");
        param.put("link", href);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.stone.MyMapper.insertLink", param);
        }
    }

    @Override
    public void storeAlreadyProcessedLink(String link) throws SQLException {
        HashMap<String, Object> param = new HashMap<>();
        param.put("table_name", "link_have_been_processed");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.stone.MyMapper.insertLink", param);
        }
    }
}
