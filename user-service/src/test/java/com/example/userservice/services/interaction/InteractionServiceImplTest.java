package com.example.userservice.services.interaction;

import com.example.userservice.models.Comment;
import com.example.userservice.models.Like;
import com.example.userservice.repositories.CommentRepository;
import com.example.userservice.repositories.LikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InteractionServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private InteractionServiceImpl interactionService;

    private Comment comment;
    private Like like;

    @BeforeEach
    void setUp() {
        comment = Comment.builder()
                .postId("post123")
                .userId("user456")
                .content("This is a comment")
                .build();

        like = Like.builder()
                .postId("post123")
                .userId("user456")
                .build();
    }

    @Test
    void addComment_shouldSaveAndReturnComment() {
        System.out.println("Starting test: addComment_shouldSaveAndReturnComment");

        when(commentRepository.save(comment)).thenReturn(comment);

        Comment savedComment = interactionService.addComment("post123", "user456", "This is a comment");

        assertNotNull(savedComment);
        assertEquals("post123", savedComment.getPostId());
        assertEquals("user456", savedComment.getUserId());
        assertEquals("This is a comment", savedComment.getContent());

        verify(commentRepository).save(
                argThat(c -> c.getPostId().equals("post123") && c.getUserId().equals("user456") && c.getContent().equals("This is a comment"))
        );

        System.out.println("Comment saved and verified successfully: " + savedComment);
    }

    @Test
    void deleteComment_shouldDeleteCommentByIdAndUserId() {
        System.out.println("Starting test: deleteComment_shouldDeleteCommentByIdAndUserId");

        interactionService.deleteComment("comment789", "user456");

        verify(commentRepository).deleteByIdAndUserId("comment789", "user456");

        System.out.println("Comment deletion verified for comment ID: comment789 and user ID: user456");
    }

    @Test
    void addLike_shouldSaveAndReturnLike() {
        System.out.println("Starting test: addLike_shouldSaveAndReturnLike");

        when(likeRepository.existsByPostIdAndUserId("post123", "user456")).thenReturn(false);
        when(likeRepository.save(like)).thenReturn(like);

        Like savedLike = interactionService.addLike("post123", "user456");

        assertNotNull(savedLike);
        assertEquals("post123", savedLike.getPostId());
        assertEquals("user456", savedLike.getUserId());

        verify(likeRepository).save(
                argThat(l -> l.getPostId().equals("post123") && l.getUserId().equals("user456"))
        );

        System.out.println("Like saved and verified successfully: " + savedLike);
    }

    @Test
    void addLike_shouldThrowExceptionWhenAlreadyLiked() {
        System.out.println("Starting test: addLike_shouldThrowExceptionWhenAlreadyLiked");

        when(likeRepository.existsByPostIdAndUserId("post123", "user456")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                interactionService.addLike("post123", "user456")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("User has already liked this post", exception.getReason());

        System.out.println("Exception thrown as expected when trying to like an already liked post.");
    }

    @Test
    void removeLike_shouldDeleteLikeWhenExists() {
        System.out.println("Starting test: removeLike_shouldDeleteLikeWhenExists");

        when(likeRepository.existsByPostIdAndUserId("post123", "user456")).thenReturn(true);

        interactionService.removeLike("post123", "user456");

        verify(likeRepository).deleteByPostIdAndUserId("post123", "user456");

        System.out.println("Like deletion verified for post ID: post123 and user ID: user456");
    }

    @Test
    void removeLike_shouldThrowExceptionWhenLikeDoesNotExist() {
        System.out.println("Starting test: removeLike_shouldThrowExceptionWhenLikeDoesNotExist");

        when(likeRepository.existsByPostIdAndUserId("post123", "user456")).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                interactionService.removeLike("post123", "user456")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Like does not exist and cannot be removed", exception.getReason());

        System.out.println("Exception thrown as expected when trying to remove a non-existent like.");
    }
}