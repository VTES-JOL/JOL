package deckserver.dwr.bean;

public class NewsBean {

    public String url;
    public String text;

    public NewsBean(String url, String text) {
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
