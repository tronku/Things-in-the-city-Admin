package com.example.things_in_city.admin;

import java.util.HashMap;
import java.util.Map;

public class Places {

    public String name, about, category, tags, geoUrl, geoLabel, city, website;

    public Places(String name, String about, String category, String tags, String city, String geoUrl, String geoLabel, String website) {
        this.name = name;
        this.about = about;
        this.category = category;
        this.tags = tags;
        this.geoUrl = geoUrl;
        this.geoLabel = geoLabel;
        this.city = city;
        this.website = website;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("description", about);
        result.put("category", category);
        result.put("tags", tags);
        result.put("geoUrl", geoUrl);
        result.put("geoLabel", geoLabel);
        result.put("city", city);
        result.put("website", website);
        return result;
    }
}
