package ru.job4j.articles.service.generator;

import ru.job4j.articles.model.Article;
import ru.job4j.articles.model.Word;

import java.util.List;

/**
 * Данный интерфейс описывает
 * генератор статей.
 *
 * Т.к.генерировать статьи можно
 * по-разному, поэтому использовали
 * интерфейс. Это позволит расширить
 * возможности генерации в дальнейшем.
 */
public interface ArticleGenerator {
    Article generate(List<Word> words);
}
