package io.javabrains.moviecatalogservice.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogItem {
    private String name;
    private String desc;
    private int rating;

    public CatalogItem(String name, String desc, int rating) {
        this.name = name;
        this.desc = desc;
        this.rating = rating;
    }
}
