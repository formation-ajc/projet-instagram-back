INSERT IGNORE INTO role (name) VALUES ('SUPER');
INSERT IGNORE INTO role (name) VALUES ('ADMIN');
INSERT IGNORE INTO role (name) VALUES ('USER');

INSERT IGNORE INTO user
(email, firstname, lastname, password)
VALUES
    ('robin.foutel@gmail.com', 'Robin', 'Foutel', '$2y$10$Rd4/r3gRbLsZ8ju.Zx.Hm.ZFvaklg2ziJ6G3RMvrelGMgW1dL4AI6');

INSERT IGNORE INTO user_role
(role_id, user_id)
VALUES (
           (SELECT id FROM role WHERE name LIKE 'SUPER'), (SELECT id FROM user WHERE email LIKE 'robin.foutel@gmail.com')
       );


