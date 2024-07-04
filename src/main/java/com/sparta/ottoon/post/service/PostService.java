package com.sparta.ottoon.post.service;

import com.sparta.ottoon.auth.entity.User;
import com.sparta.ottoon.auth.entity.UserStatus;
import com.sparta.ottoon.auth.repository.UserRepository;
import com.sparta.ottoon.common.exception.CustomException;
import com.sparta.ottoon.common.exception.ErrorCode;
import com.sparta.ottoon.like.repository.LikeRepository;
import com.sparta.ottoon.post.dto.PostRequestDto;
import com.sparta.ottoon.post.dto.PostResponseDto;
import com.sparta.ottoon.post.entity.Post;
import com.sparta.ottoon.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    @Transactional
    public PostResponseDto save(PostRequestDto postRequestDto,String username) {
        User user = findByUserByUsername(username);
        Post post = postRequestDto.toEntity();
        post.updateUser(user);
        post = postRepository.save(post);

        Long likeCount = likeRepository.countByPost(post);

        return PostResponseDto.toDto("게시글 등록 완료", 200, post, likeCount);
    }

    @Transactional(readOnly = true)
    public PostResponseDto findById(long postId) {
        Post post = findPostById(postId);
        Long likecount = likeRepository.countByPost(post);

        return PostResponseDto.toDto("부분 게시글 조회 완료", 200, post, likecount);
    }

    @Transactional(readOnly = true)
    public List<PostResponseDto> getAll(int page) {
        Sort sort = Sort.by(Sort.Direction.DESC, "isTop").and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(page, 5, sort);
        Page<PostResponseDto> postPage = postRepository.findAll(pageable).map(post -> {
            Long likeCount = likeRepository.countByPost(post);
            return PostResponseDto.toDto("전체 게시글 조회 완료", 200, post, likeCount);
        });

        return postPage
                .stream()
                .toList();
    }

    @Transactional
    public PostResponseDto update(long postId, PostRequestDto postRequestDto, String username) {
        Post post = findPostById(postId);
        User user = findByUserByUsername(username);

        // 본인 계정 혹은 관리자 계정이면 게시글 수정 가능
        if (user.getId().equals(post.getUser().getId()) || user.getStatus().equals(UserStatus.ADMIN)) {
            post.update(postRequestDto.getContents());
            Long likeCount = likeRepository.countByPost(post);

            return PostResponseDto.toDto("게시글 수정 완료", 200, post, likeCount);
        } else {

            throw new CustomException(ErrorCode.BAD_AUTH_PUT);
        }
    }

    @Transactional
    public void delete(long postId, String username) {
        Post post = findPostById(postId);
        User user = findByUserByUsername(username);

        // 본인 계정 혹은 관리자 계정이면 게시글 삭제 가능
        if (user.getId().equals(post.getUser().getId()) || user.getStatus().equals(UserStatus.ADMIN)) {
            postRepository.delete(post);
            PostResponseDto.toDeleteResponse("게시글 삭제 완료", 200);
        } else {
            throw new CustomException(ErrorCode.BAD_AUTH_DELETE);
        }
    }

    private User findByUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Post findPostById(long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.BAD_POST_ID));
    }
}
