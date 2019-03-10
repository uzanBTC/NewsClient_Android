package be.kuleuven.softdev.haientang.newsclient.model;

/**
 * Created by Haien on 5/26/2018.
 */

public class NewsItem {
    public String image_URL,title,date;
    public int likes;

    public NewsItem(String image_URL, String title, String date, int likes) {
        this.image_URL = image_URL;
        this.title = title;
        this.date = date;
        this.likes = likes;
    }
}
