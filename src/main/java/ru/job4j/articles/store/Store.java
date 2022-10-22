package ru.job4j.articles.store;

import java.util.List;

/**
 * Данный интерфейс описывает хранилище.
 *
 * @param <T> хранимый обьект.
 *           Это может быть всё что угодно.
 */
public interface Store<T> {
    T save(T model);
    List<T> findAll();

    void clear();
}
