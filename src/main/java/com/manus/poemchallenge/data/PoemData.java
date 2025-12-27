package com.manus.poemchallenge.data;

/**
 * 古诗词数据模型
 */
public class PoemData {
    private final String content;
    private final String title;
    private final String author;
    private final String dynasty;

    public PoemData(String content, String title, String author, String dynasty) {
        this.content = content;
        this.title = title;
        this.author = author;
        this.dynasty = dynasty;
    }

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDynasty() {
        return dynasty;
    }

    @Override
    public String toString() {
        return String.format("%s - 《%s》 (%s)", content, title, author);
    }
}
