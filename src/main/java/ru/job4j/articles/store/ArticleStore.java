package ru.job4j.articles.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.articles.model.Article;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Данный класс описывает
 * хранилище статей.
 *
 * Статьи будут храниться в БД HSQL,
 * в отдельной таблице.
 */
public class ArticleStore implements Store<Article>, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleStore.class.getSimpleName());

    private final Properties properties;

    private Connection connection;

    public ArticleStore(Properties properties) {
        this.properties = properties;
        initConnection();
        initScheme();
        clear();
    }

    private void initConnection() {
        LOGGER.info("Создание подключения к БД статей");
        try {
            connection = DriverManager.getConnection(
                    properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password")
            );
        } catch (SQLException throwables) {
            LOGGER.error("Не удалось выполнить операцию: { }", throwables.getCause());
            throw new IllegalStateException();
        }
    }

    private void initScheme() {
        LOGGER.info("Инициализация таблицы статей");
        try (var statement = connection.createStatement()) {
            var sql = Files.readString(Path.of("db/scripts", "articles.sql"));
            statement.execute(sql);
        } catch (Exception e) {
            LOGGER.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        }
    }

    @Override
    public Article save(Article model) {
        LOGGER.info("Сохранение статьи");
        var sql = "insert into articles(text) values(?)";
        try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, model.getText());
            statement.executeUpdate();
            var key = statement.getGeneratedKeys();
            while (key.next()) {
                model.setId(key.getInt(1));
            }
            LOGGER.info("Сохранение статьи # {}", model.getId());
        } catch (Exception e) {
            LOGGER.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        }
        return model;
    }

    @Override
    public List<Article> findAll() {
        LOGGER.info("Загрузка всех статей");
        var sql = "select * from articles";
        var articles = new ArrayList<Article>();
        try (var statement = connection.prepareStatement(sql)) {
            var selection = statement.executeQuery();
            while (selection.next()) {
                articles.add(new Article(
                        selection.getInt("id"),
                        selection.getString("text")
                ));
            }
        } catch (Exception e) {
            LOGGER.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        }
        return articles;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Данный метод очищает данные
     * в таблице, если там есть
     * хотя бы одна строка.
     *
     * Выбрал метод truncate.
     * Он быстрее, чем delete.
     * В данном конкретном случае
     * нам этого достаточно.
     */
    @Override
    public void clear() {
        String select = "select count(*) from public.articles;";
        String truncate = "truncate table public.articles RESTART IDENTITY;";
        try (Statement statement = connection.createStatement()) {
            var rows = statement.execute(select);
            if (rows) {
                LOGGER.info("Производим очистку статей");
                statement.execute(truncate);
            }
        } catch (Exception e) {
            LOGGER.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        }
    }
}
