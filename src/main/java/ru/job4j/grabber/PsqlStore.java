package ru.job4j.grabber;

import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
            cnn = DriverManager.getConnection(cfg.getProperty("jdbc.url"),
                    cfg.getProperty("jdbc.username"), cfg.getProperty("jdbc.password"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement ps = cnn.prepareStatement(
                "insert into post(name, link, text, created) values (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getLink());
            ps.setString(3, post.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            ps.execute();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement ps = cnn.prepareStatement("select * from post")) {
            ResultSet rsl = ps.executeQuery();
            while (rsl.next()) {
                posts.add(createPost(rsl));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement ps = cnn.prepareStatement("select * from post where id = ?")) {
            ps.setInt(1, id);
            ResultSet rsl = ps.executeQuery();
            if (rsl.next()) {
                post = createPost(rsl);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    private Post createPost(ResultSet rsl) throws SQLException {
        return new Post(rsl.getInt(1),
                rsl.getString(2),
                rsl.getString(3),
                rsl.getString(4),
                rsl.getTimestamp(5).toLocalDateTime());
    }

    public static void main(String[] args) {
        Properties config = new Properties();
        try (InputStream in = PsqlStore.class.getClassLoader().
                getResourceAsStream("app.properties")) {
            config.load(in);
            Class.forName(config.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Store store = new PsqlStore(config);
        List<Post> list = new HabrCareerParse(new HabrCareerDateTimeParser()).
                list("https://career.habr.com/vacancies/java_developer?page=");
        for (Post post : list) {
            store.save(post);
        }
        List<Post> fromSql = store.getAll();
        for (Post post : fromSql) {
            System.out.println(post);
        }
        System.out.println(store.findById(3));
    }
}