package com.example.userservice.contollers;

import com.example.userservice.configs.RabbitMQDataUserReceiverConfig;
import com.example.userservice.models.Comment;
import com.example.userservice.models.Like;
import com.example.userservice.services.interaction.InteractionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InteractionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InteractionService interactionService;

    @MockBean
    RabbitMQDataUserReceiverConfig rabbitMQDataUserReceiverConfig;

    @Test
    void testPostComment() throws Exception {
        System.out.println("Starting integration test: testPostComment");

        String postId = "123";
        String userId = "user1";
        String content = "This is a test comment.";

        Comment mockComment = new Comment();
        mockComment.setId("comment1");
        mockComment.setPostId(postId);
        mockComment.setUserId(userId);
        mockComment.setContent(content);

        Mockito.when(interactionService.addComment(postId, userId, content)).thenReturn(mockComment);

        MvcResult result = mockMvc.perform(post("/api/v1/interactions/comment/{postId}", postId)
                        .param("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Comment responseComment = objectMapper.readValue(jsonResponse, Comment.class);

        assertEquals(mockComment.getId(), responseComment.getId());
        assertEquals(mockComment.getPostId(), responseComment.getPostId());
        assertEquals(mockComment.getUserId(), responseComment.getUserId());
        assertEquals(mockComment.getContent(), responseComment.getContent());
        System.out.println("Comment posted successfully: " + responseComment.getId());
    }

    @Test
    void testDeleteComment() throws Exception {
        System.out.println("Starting integration test: testDeleteComment");

        String commentId = "comment1";
        String userId = "user1";

        Mockito.doNothing().when(interactionService).deleteComment(commentId, userId);

        MvcResult result = mockMvc.perform(delete("/api/v1/interactions/comment/{commentId}", commentId)
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        System.out.println("Comment deleted successfully: " + commentId);
    }

    @Test
    void testAddLike() throws Exception {
        System.out.println("Starting integration test: testAddLike");

        String postId = "123";
        String userId = "user1";

        Like mockLike = new Like();
        mockLike.setId("like1");
        mockLike.setPostId(postId);
        mockLike.setUserId(userId);

        Mockito.when(interactionService.addLike(postId, userId)).thenReturn(mockLike);

        MvcResult result = mockMvc.perform(post("/api/v1/interactions/like/{postId}", postId)
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Like responseLike = objectMapper.readValue(jsonResponse, Like.class);

        assertEquals(mockLike.getId(), responseLike.getId());
        assertEquals(mockLike.getPostId(), responseLike.getPostId());
        assertEquals(mockLike.getUserId(), responseLike.getUserId());
        System.out.println("Like added successfully: " + responseLike.getId());
    }

    @Test
    void testRemoveLike() throws Exception {
        System.out.println("Starting integration test: testRemoveLike");

        String postId = "123";
        String userId = "user1";

        Mockito.doNothing().when(interactionService).removeLike(postId, userId);

        MvcResult result = mockMvc.perform(delete("/api/v1/interactions/like/{postId}", postId)
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        System.out.println("Like removed successfully from post: " + postId);
    }
}