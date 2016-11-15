package deckserver.dwr.bean;

public class NewsBean {

    private String url;
    private String text;

    NewsBean(String url, String text) {
        this.url = url;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getUrl() {
        return url;
    }

}
