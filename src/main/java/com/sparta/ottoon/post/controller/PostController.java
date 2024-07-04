package com.sparta.ottoon.post.controller;

import com.sparta.ottoon.post.dto.PostRequestDto;
import com.sparta.ottoon.post.dto.PostResponseDto;
import com.sparta.ottoon.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Post API", description = "Post API 입니다")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    // 게시글 등록
    @Operation(summary = "createPost", description = "게시글 생성 기능입니다.")
    @PostMapping("/posts")
    public ResponseEntity<PostResponseDto> create(@Valid @RequestBody PostRequestDto postRequestDto,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        PostResponseDto post = postService.save(postRequestDto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    // 게시글 전체 조회
    @Operation(summary = "getPostAll", description = "게시글 전체 조회 기능입니다.")
    @GetMapping("/posts")
//    public ResponseEntity<Map<String, Object>> getAll() {
    public ResponseEntity<Map<String, Object>> getAll(@RequestParam(value = "page", defaultValue = "1") int page) {
//        List<PostResponseDto> postContents = postService.getAll();
        List<PostResponseDto> postContents = postService.getAll(page - 1);
        if (postContents.isEmpty()) {
            // 게시글이 비어있는 경우
            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", HttpStatus.OK.value());
            response.put("message", "먼저 게시글을 작성하세요.");
            return ResponseEntity.ok().body(response);
        } else {
            // 게시글이 있는 경우
            return ResponseEntity.ok().body(Collections.singletonMap("postContents", postContents));
        }
    }

    // 게시글 부분 조회
    @Operation(summary = "getPostSelect", description = "게시글 부분 조회 기능입니다.")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostResponseDto> findById(@PathVariable(name = "postId") long postId) {
        return ResponseEntity.ok().body(postService.findById(postId));
    }

    // 게시글 수정
    @Operation(summary = "updatePost", description = "게시글 수정 기능입니다.")
    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostResponseDto> update(@PathVariable(name = "postId") long postId, @Valid @RequestBody PostRequestDto postRequestDto,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok().body(postService.update(postId, postRequestDto, userDetails.getUsername()));
    }

    //게시글 삭제
    @Operation(summary = "deletePost", description = "게시글 삭제 기능입니다.")
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable(name = "postId") long postId,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        postService.delete(postId, userDetails.getUsername());
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", HttpStatus.OK.value());
        response.put("message", "게시글 삭제 완료");
        return ResponseEntity.ok().body(response);
    }
}
