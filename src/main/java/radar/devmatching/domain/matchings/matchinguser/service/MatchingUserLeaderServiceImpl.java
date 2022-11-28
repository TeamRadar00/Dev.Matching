package radar.devmatching.domain.matchings.matchinguser.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import radar.devmatching.domain.matchings.matching.entity.Matching;
import radar.devmatching.domain.matchings.matching.repository.MatchingRepository;
import radar.devmatching.domain.matchings.matchinguser.entity.MatchingUser;
import radar.devmatching.domain.matchings.matchinguser.entity.MatchingUserRole;
import radar.devmatching.domain.matchings.matchinguser.repository.MatchingUserRepository;
import radar.devmatching.domain.user.entity.User;
import radar.devmatching.domain.user.repository.UserRepository;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MatchingUserLeaderServiceImpl implements MatchingUserLeaderService {

	private final MatchingUserRepository matchingUserRepository;

	private final UserRepository userRepository;
	private final MatchingRepository matchingRepository;

	public boolean checkExistMatchingUser(Long matchingId, Long userId) {
		return matchingUserRepository.existsByMatchingIdAndUserId(matchingId, userId);
	}

	@Transactional
	public void createMatchingUserLeader(Long userId, Long matchingId) {
		// 엔티티를 이렇게 생성하는 방법도 있더라고 잘 작동하면 이렇게 만들까 고민중
		// User user = matchingUserConverter.toUser(userId);
		// Matching matching = matchingUserConverter.toMatching(matchingId);
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException());
		Matching matching = matchingRepository.findById(matchingId).orElseThrow(() -> new RuntimeException());

		MatchingUser matchingUser = new MatchingUser(MatchingUserRole.LEADER, user, matching);

		matchingUserRepository.save(matchingUser);
	}

}