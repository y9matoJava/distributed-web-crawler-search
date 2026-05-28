package io.github.ymatojava.crawler.core.keyword;

import java.util.Set;

/**
 * Словарь стоп-слов.
 *
 * Стоп-слова — это часто встречающиеся слова, не несущие самостоятельной
 * смысловой нагрузки (предлоги, союзы, местоимения). Их исключение из индекса
 * значительно уменьшает его размер и повышает релевантность поиска.
 */
public class StopWords {

    /**
     * HashSet обеспечивает проверку наличия слова за O(1).
     * В реальном проекте этот список загружался бы из файла.
     */
    private static final Set<String> STOP_WORDS = Set.of(
            // English
            "a", "an", "the", "and", "but", "or", "because", "as", "until", "while",
            "of", "at", "by", "for", "with", "about", "against", "between", "into", "through",
            "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out",
            "on", "off", "over", "under", "again", "further", "then", "once", "here",
            "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more",
            "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so",
            "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now",
            "i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours",
            "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers",
            "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves",
            "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are",
            "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing",
            // Russian
            "и", "в", "во", "не", "что", "он", "на", "я", "с", "со", "как", "а", "то", "все", "она",
            "так", "его", "но", "да", "ты", "к", "у", "же", "вы", "за", "бы", "по", "только", "ее",
            "мне", "было", "вот", "от", "меня", "еще", "нет", "о", "из", "ему", "теперь", "когда",
            "даже", "ну", "вдруг", "ли", "если", "уже", "или", "ни", "быть", "был", "него", "до", "вас",
            "нибудь", "опять", "уж", "вам", "ведь", "там", "потом", "себя", "ничего", "ей", "может", "они",
            "тут", "где", "мы", "для", "об", "чем", "их", "кто"
    );

    /**
     * Проверяет, является ли слово стоп-словом.
     *
     * @param word Слово в нижнем регистре
     * @return true, если слово нужно отфильтровать
     */
    public boolean isStopWord(String word) {
        if (word == null) return false;
        return STOP_WORDS.contains(word);
    }
}
