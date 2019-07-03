package org.guild.dataprocessor.datatypes;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Metadata {

    private Integer id;
    private String company;
    private Long budget;
    private Long revenue;
    private Float popularity;
    private String genre ;
    private Float rating;
    private String date;

    public Metadata(Integer id) throws NullPointerException {
        if (id != null) {
            this.id = id;
        } else {
            throw new NullPointerException("Value cannot be null");
        }
    }

    public Integer getId() {
        return this.id;
    }

    public String getCompany() {
        return this.company;
    }

    public Long getBudget() {
        return this.budget;
    }

    public Long getRevenue() {
        return this.revenue;
    }

    public Float getPopularity() {
        return this.popularity;
    }

    public String getGenre() {
        return this.genre;
    }

    public String getDate() {
        return this.date;
    }

    public Float getRating() {
        return this.rating;
    }


    public JSONObject getJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("company", company);
        jsonObject.put("budget", budget);
        jsonObject.put("revenue", revenue);
        jsonObject.put("popularity", popularity);
        jsonObject.put("genre", genre);
        jsonObject.put("date", date);
        return jsonObject;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setBudget(Long budget) {
        this.budget = budget;
    }

    public void setRevenue(Long revenue) {
        this.revenue = revenue;
    }

    public void setPopularity(Float popularity) {
        this.popularity = popularity;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

}
