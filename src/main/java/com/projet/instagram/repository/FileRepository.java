package com.projet.instagram.repository;

import com.projet.instagram.model.File;
import com.projet.instagram.model.Post;
import com.projet.instagram.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    File findByPost(Post post);
}