package com.projet.instagram.controller;

import com.projet.instagram.model.File;
import com.projet.instagram.model.Post;
import com.projet.instagram.model.User;
import com.projet.instagram.model.security.RefreshToken;
import com.projet.instagram.repository.FileRepository;
import com.projet.instagram.repository.PostRepository;
import com.projet.instagram.repository.UserRepository;
import com.projet.instagram.schema.request.post.PostRequest;
import com.projet.instagram.schema.request.user.UpdatePasswordUserRequest;
import com.projet.instagram.schema.request.user.UpdateUserRequest;
import com.projet.instagram.schema.response.post.PostResponse;
import com.projet.instagram.utils.security.JwtUtils;
import com.projet.instagram.schema.response.security.MessageResponse;
import com.projet.instagram.schema.response.security.SignupResponse;
import com.projet.instagram.service.security.RefreshTokenService;
import com.projet.instagram.service.security.UserDetailsImpl;
import com.projet.instagram.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("${api.baseURL}/posts")
public class PostController {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    PostRepository postRepository;

    @Value("${hashfiles.location}")
    private String filePath;

    @PostMapping("")
    public ResponseEntity<?> newPost(@RequestHeader(HttpHeaders.AUTHORIZATION) String headerAuth, @Valid @RequestBody PostRequest postRequest) {
        try {
            // On récupère l'email du token qui est l'email avant modification
            String email = jwtUtils.getEmailFromJwtToken(headerAuth.split(" ")[1]);

            String filename = UUID.randomUUID().toString();

            FileWriter writer = new FileWriter(filePath + filename);
            writer.write(postRequest.getData()); // Vous devez définir req.body.data dans votre contexte Java
            writer.close();
            System.out.println("Saved!");


            File file = new File();
            file.setName(postRequest.getName());
            file.setSize(postRequest.getSize());
            file.setType(postRequest.getType());
            file.setSrc(filename);

            fileRepository.save(file);

            Post post = new Post();
            if (userRepository.findByEmail(email).isPresent()) {
                post.setUserId(userRepository.findByEmail(email).get());
            }
            post.setFileId(file);
            post.setDescription(postRequest.getDescription());
            post.setLikes(0);
            post.setPrivate(postRequest.getIsPrivate());
            post.setPublishDate(LocalDate.now());

            postRepository.save(post);

            return ResponseEntity.ok(new MessageResponse("Success: Post added!"));
        }
        catch(Exception e) {// see note 2
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse("Error: Post not added!"));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getPost(@RequestHeader(HttpHeaders.AUTHORIZATION) String headerAuth) {
        try {
            // On récupère l'email du token qui est l'email avant modification
            String email = jwtUtils.getEmailFromJwtToken(headerAuth.split(" ")[1]);

            User user = null;
            if (userRepository.findByEmail(email).isPresent()) {
                user = userRepository.findByEmail(email).get();
            }

            List<Post> posts = postRepository.findByUserId(user);

            List<PostResponse> postResponses = new ArrayList<>();

            for (Post post: posts) {

                File file = fileRepository.findByPost(post);

                Path audioPath = Path.of(filePath, file.getSrc());
                Resource resource = new UrlResource(audioPath.toUri());
                if (resource.exists() && resource.isReadable()) {
                    try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder content = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            content.append(line);
                        }

                        String fileContentAsString = content.toString();
                        postResponses.add(PostResponse.createSportResponse(post, file, fileContentAsString));
                    } catch (IOException e) {
                        System.out.println("Error reading file: " + e.getMessage());
                    }

//                    return ResponseEntity.ok().body(audioData);
                } else {
//                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No audio found!");
                    System.out.println("Impossible de récupérer le fichier : " + file.getName());
                }
            }

            return ResponseEntity.ok(postResponses);

        }
        catch(Exception e) {// see note 2
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse("Error: Impossible to charge posts!"));
        }
    }
}
