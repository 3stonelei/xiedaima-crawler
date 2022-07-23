package com.github.stone;

/**
 * @author Lilei
 * @date 2022/7/20-@22:02
 */
public class main {
    public static void main(String[] args) {
        MybatisDao dao = new MybatisDao();
        for (int i = 0; i <10 ; i++) {
            new Crawler(dao).start();
        }

    }
}
