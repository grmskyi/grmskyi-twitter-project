package com.example.userservice.services.interaction;

import com.example.userservice.models.Comment;
import com.example.userservice.models.Like;
import com.example.userservice.repositories.CommentRepository;
import com.example.userservice.repositories.LikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public Comment addComment(String postId, String userId, String content) {
        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .content(content)
                .publicationDate(LocalDateTime.now())
                .build();

        log.info("Comment created: {}", comment);
        return commentRepository.save(comment);
    }

    public void deleteComment(String commentId, String userId) {
        commentRepository.deleteByIdAndUserId(commentId, userId);
        log.info("Comment deleted: {}", commentId);
    }

    public Like addLike(String postId, String userId) {
        if (!likeRepository.existsByPostIdAndUserId(postId, userId)) {
            Like like = Like.builder()
                    .postId(postId)
                    .userId(userId)
                    .build();
            log.info("Like created: {}", like);
            return likeRepository.save(like);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has already liked this post");
        }
    }

    public void removeLike(String postId, String userId) {
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            log.info("Like deleted by: {} and: {}", postId, userId);
            likeRepository.deleteByPostIdAndUserId(postId, userId);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Like does not exist and cannot be removed");
        }
    }
}