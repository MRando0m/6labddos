package io.swagger.integration;

import io.swagger.model.Comment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class CommentServiceIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("comments_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testCreateAndRetrieveComment() {
        Comment newComment = new Comment();
        newComment.setUsername("user1");
        newComment.setText("Это тестовый комментарий");

        ResponseEntity<Comment> postResponse = restTemplate.postForEntity(getBaseUrl() + "/comments", newComment, Comment.class);
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode(), "Ошибка создания комментария");
        Comment created = postResponse.getBody();
        assertNotNull(created, "Созданный комментарий равен null");
        assertNotNull(created.getId(), "ID комментария не должен быть null после создания");

        ResponseEntity<Comment> getResponse = restTemplate.getForEntity(getBaseUrl() + "/comments/" + created.getId(), Comment.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode(), "Ошибка получения комментария");
        Comment retrieved = getResponse.getBody();
        assertNotNull(retrieved, "Полученный комментарий равен null");
        assertEquals("user1", retrieved.getUsername(), "Неверное имя пользователя");
        assertEquals("Это тестовый комментарий", retrieved.getText(), "Неверный текст комментария");
    }

    @Test
    void testUpdateComment() {
        Comment newComment = new Comment();
        newComment.setUsername("user2");
        newComment.setText("Начальный комментарий");
        ResponseEntity<Comment> postResponse = restTemplate.postForEntity(getBaseUrl() + "/comments", newComment, Comment.class);
        Comment created = postResponse.getBody();
        assertNotNull(created, "Созданный комментарий равен null");
        Long id = created.getId();
        assertNotNull(id, "ID комментария не заполнен");

        Comment updatedComment = new Comment();
        updatedComment.setId(id);
        updatedComment.setUsername("user2");
        updatedComment.setText("Обновленный комментарий");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Comment> entity = new HttpEntity<>(updatedComment, headers);

        ResponseEntity<Void> putResponse = restTemplate.exchange(getBaseUrl() + "/comments/" + id, HttpMethod.PUT, entity, Void.class);
        assertTrue(putResponse.getStatusCode().is2xxSuccessful(), "Ошибка обновления комментария");

        ResponseEntity<Comment> getResponse = restTemplate.getForEntity(getBaseUrl() + "/comments/" + id, Comment.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode(), "Ошибка получения обновленного комментария");
        Comment retrieved = getResponse.getBody();
        assertNotNull(retrieved, "Полученный комментарий равен null");
        assertEquals("Обновленный комментарий", retrieved.getText(), "Текст комментария не обновлен");
    }

    @Test
    void testDeleteComment() {
        Comment newComment = new Comment();
        newComment.setUsername("user3");
        newComment.setText("Комментарий для удаления");
        ResponseEntity<Comment> postResponse = restTemplate.postForEntity(getBaseUrl() + "/comments", newComment, Comment.class);
        Comment created = postResponse.getBody();
        assertNotNull(created, "Созданный комментарий равен null");
        Long id = created.getId();
        assertNotNull(id, "ID комментария не заполнен");

        restTemplate.delete(getBaseUrl() + "/comments/" + id);

        ResponseEntity<Comment> getResponse = restTemplate.getForEntity(getBaseUrl() + "/comments/" + id, Comment.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode(), "Комментарий не был удален");
    }

    @Test
    void testGetAllComments() {
        Comment comment1 = new Comment();
        comment1.setUsername("userA");
        comment1.setText("Комментарий A");

        Comment comment2 = new Comment();
        comment2.setUsername("userB");
        comment2.setText("Комментарий B");

        restTemplate.postForEntity(getBaseUrl() + "/comments", comment1, Comment.class);
        restTemplate.postForEntity(getBaseUrl() + "/comments", comment2, Comment.class);

        ResponseEntity<Comment[]> response = restTemplate.getForEntity(getBaseUrl() + "/comments", Comment[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Ошибка при получении списка комментариев");
        Comment[] comments = response.getBody();
        assertNotNull(comments, "Список комментариев равен null");
        assertTrue(comments.length >= 2, "Ожидается минимум 2 комментария");
    }

    @Test
    void testUpdateNonExistingComment() {
        Long nonExistingId = 999999L;
        Comment updateComment = new Comment();
        updateComment.setId(nonExistingId);
        updateComment.setUsername("userX");
        updateComment.setText("Обновление несуществующего комментария");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Comment> entity = new HttpEntity<>(updateComment, headers);

        ResponseEntity<Void> putResponse = restTemplate.exchange(getBaseUrl() + "/comments/" + nonExistingId, HttpMethod.PUT, entity, Void.class);
        assertEquals(HttpStatus.NOT_FOUND, putResponse.getStatusCode(), "Ожидался статус 404 для несуществующего комментария");
    }

    @Test
    void testDeleteNonExistingComment() {
        Long nonExistingId = 888888L;
        restTemplate.delete(getBaseUrl() + "/comments/" + nonExistingId);
        ResponseEntity<Comment> getResponse = restTemplate.getForEntity(getBaseUrl() + "/comments/" + nonExistingId, Comment.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode(), "Ожидался статус 404 для несуществующего комментария");
    }
}
