package ru.job4j.articles.service;

import ru.job4j.articles.model.Article;
import ru.job4j.articles.model.Word;
import ru.job4j.articles.store.Store;

/**
 * Данный интерфейс описывает сервис,
 * который можно применять к статьям.
 *
 * В данном случае у нас сервис по
 * генерации статей.
 */
public interface ArticleService {
    void generate(Store<Word> wordStore, int count, Store<Article> articleStore);
}
