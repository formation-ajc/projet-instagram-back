package com.projet.instagram.schema.response.post;

import com.projet.instagram.model.File;
import com.projet.instagram.model.Post;
import com.projet.instagram.model.User;
import com.projet.instagram.schema.response.user.UserPublicResponse;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class PostResponse {

    private Long id;
    private String name;

    private String type;

    private String data;
    private String description;
    private Integer likes;
    private LocalDate publishDate;
    private UserPublicResponse publisher;

    public PostResponse(Long id, String name, String type, String data, String description, Integer likes, LocalDate publishDate, UserPublicResponse publisher) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.data = data;
        this.description = description;
        this.likes = likes;
        this.publishDate = publishDate;
        this.publisher = publisher;
    }

    public static PostResponse createSportResponse(Post post, File file, String data) {
        return new PostResponse(
            post.getId(),
            file.getName(),
            file.getType(),
            data,
            post.getDescription(),
            post.getLikes(),
            post.getPublishDate(),
            (post.getUserId() != null)?
                new UserPublicResponse(
                    post.getUserId().getId(),
                    post.getUserId().getFirstname(),
                    post.getUserId().getLastname(),
                    post.getUserId().getEmail()
                ):null
        );
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public UserPublicResponse getPublisher() {
        return publisher;
    }

    public void setPublisher(UserPublicResponse publisher) {
        this.publisher = publisher;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
