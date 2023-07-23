package com.app.assetmaintenance.models;

public class ComplainsModel {

    String description, image, status, created_at;
    int id, user_id, user_assign_id;

    public ComplainsModel() {
    }

    public ComplainsModel(String description, String image, String status, String created_at, int id, int user_id, int user_assign_id) {
        this.description = description;
        this.image = image;
        this.status = status;
        this.created_at = created_at;
        this.id = id;
        this.user_id = user_id;
        this.user_assign_id = user_assign_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getUser_assign_id() {
        return user_assign_id;
    }

    public void setUser_assign_id(int user_assign_id) {
        this.user_assign_id = user_assign_id;
    }
}