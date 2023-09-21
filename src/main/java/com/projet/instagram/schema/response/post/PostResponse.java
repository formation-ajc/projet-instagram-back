package com.projet.instagram.schema.response.post;

import com.projet.instagram.model.File;
import com.projet.instagram.model.Post;
import com.projet.instagram.schema.response.user.UserPublicResponse;

import java.time.LocalDate;

public class PostResponse {

    private Long id;
    private String name;

    private String type;

    private byte[] file;
    private String description;
    private Integer likes;
    private LocalDate publishDate;
    private UserPublicResponse publisher;

    public PostResponse(Long id, String name, String type, byte[] file, String description, Integer likes, LocalDate publishDate, UserPublicResponse publisher) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.file = file;
        this.description = description;
        this.likes = likes;
        this.publishDate = publishDate;
        this.publisher = publisher;
    }

    public static PostResponse createSportResponse(Post post, File file, byte[] fileData) {
        return new PostResponse(
            post.getId(),
            file.getName(),
            file.getType(),
            fileData,
            post.getDescription(),
            post.getLikes(),
            post.getPublishDate(),
            (post.getUser() != null)?
                new UserPublicResponse(
                    post.getUser().getId(),
                    post.getUser().getFirstname(),
                    post.getUser().getLastname(),
                    post.getUser().getEmail()
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

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
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
