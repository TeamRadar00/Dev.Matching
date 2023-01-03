package radar.devmatching.domain.post.full.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import radar.devmatching.common.exception.InvalidAccessException;
import radar.devmatching.common.exception.error.ErrorMessage;
import radar.devmatching.domain.comment.service.CommentService;
import radar.devmatching.domain.comment.service.dto.response.MainCommentResponse;
import radar.devmatching.domain.matchings.apply.repository.ApplyRepository;
import radar.devmatching.domain.matchings.apply.service.ApplyService;
import radar.devmatching.domain.post.full.service.dto.UpdatePostDto;
import radar.devmatching.domain.post.full.service.dto.response.PresentPostResponse;
import radar.devmatching.domain.post.simple.entity.SimplePost;
import radar.devmatching.domain.post.simple.service.SimplePostService;
import radar.devmatching.domain.user.entity.User;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FullPostServiceImpl implements FullPostService {

	private final SimplePostService simplePostService;
	private final ApplyService applyService;
	private final ApplyRepository applyRepository;
	private final CommentService commentService;

	/**
	 * 게시글 화면 전체를 가져온다(게시글, 신청자 수, 댓글)
	 * @param simplePostId
	 * @return
	 */
	@Override
	@Transactional
	public PresentPostResponse getPostWithComment(long simplePostId, User loginUser) {
		SimplePost findPost = simplePostService.findPostById(simplePostId);

		// 나중에 새로고침 누르면 clickCount는 안 올라가도록 설정해도 좋을듯?
		findPost.plusClickCount();
		int applyCount = applyService.getAcceptedApplyCount(simplePostId);
		boolean isAppliedLoginUser = applyRepository.findByApplySimplePostIdAndApplyUserId(simplePostId,
				loginUser.getId())
			.isPresent();
		List<MainCommentResponse> allComments = commentService.getAllComments(findPost.getFullPost().getId());

		return PresentPostResponse.of(findPost, loginUser, applyCount, isAppliedLoginUser, allComments);
	}

	/**
	 * 업데이트를 위해 기존 게시글 정보를 가져온다.
	 * @param simplePostId
	 * @param userId
	 * @return
	 */
	@Override
	public UpdatePostDto getFullPost(long simplePostId, long userId) {
		isLeaderValidation(simplePostId, userId);
		SimplePost findPost = simplePostService.findPostById(simplePostId);
		return UpdatePostDto.of(findPost);
	}

	@Override
	@Transactional
	public void updatePost(long simplePostId, long userId, UpdatePostDto updatePostDto) {
		isLeaderValidation(simplePostId, userId);
		SimplePost findPost = simplePostService.findPostById(simplePostId);
		findPost.update(updatePostDto.getTitle(), updatePostDto.getCategory(), updatePostDto.getRegion(),
			updatePostDto.getUserNum(), updatePostDto.getContent());
	}

	@Override
	@Transactional
	public void deletePost(long simplePostId, long userId) {
		isLeaderValidation(simplePostId, userId);
		simplePostService.deleteById(simplePostId);
	}

	@Override
	@Transactional
	public void closePost(long simplePostId, long userId) {
		isLeaderValidation(simplePostId, userId);
		SimplePost findPost = simplePostService.findPostById(simplePostId);
		findPost.closePost();
	}

	private void isLeaderValidation(long simplePostId, long userId) {
		SimplePost findPost = simplePostService.findById(simplePostId);
		if (!Objects.equals(findPost.getLeader().getId(), userId)) {
			throw new InvalidAccessException(ErrorMessage.NOT_LEADER);
		}
	}
}
