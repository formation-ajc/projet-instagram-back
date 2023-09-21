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
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public ResponseEntity<?> newPost(@RequestHeader(HttpHeaders.AUTHORIZATION) String headerAuth, @Valid @ModelAttribute PostRequest postRequest) {
        try {
            // On récupère l'email du token qui est l'email avant modification
            String email = jwtUtils.getEmailFromJwtToken(headerAuth.split(" ")[1]);
            MultipartFile uploadedFile = postRequest.getFile();
            byte[] fileBytes = uploadedFile.getBytes();

            // Save the byte array to a file
            String filename = UUID.randomUUID().toString();
            Files.write(Path.of(filePath + filename), fileBytes);

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

            for (Post post : posts) {
                File file = fileRepository.findByPost(post);

                // Load the file content into a byte array
                Path path = Path.of(filePath, file.getSrc());
                Resource resource = new UrlResource(path.toUri());

                if (resource.exists() && resource.isReadable()) {
                    try (InputStream inputStream = resource.getInputStream()) {
                        byte[] fileData = inputStream.readAllBytes();
                        postResponses.add(PostResponse.createSportResponse(post, file, fileData));
                    } catch (IOException e) {
                        System.out.println("Error reading file: " + e.getMessage());
                    }
                } else {
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
