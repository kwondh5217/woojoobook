package com.e207.woojoobook.api.userbook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.e207.woojoobook.api.userbook.event.ExperienceEvent;
import com.e207.woojoobook.api.userbook.event.PointEvent;
import com.e207.woojoobook.api.userbook.request.UserbookCreateRequest;
import com.e207.woojoobook.api.userbook.request.UserbookPageFindRequest;
import com.e207.woojoobook.api.userbook.request.UserbookUpdateRequest;
import com.e207.woojoobook.api.userbook.response.UserbookResponse;
import com.e207.woojoobook.domain.book.Book;
import com.e207.woojoobook.domain.book.BookReader;
import com.e207.woojoobook.domain.user.User;
import com.e207.woojoobook.domain.user.experience.ExperienceHistory;
import com.e207.woojoobook.domain.user.point.PointHistory;
import com.e207.woojoobook.domain.userbook.RegisterType;
import com.e207.woojoobook.domain.userbook.Userbook;
import com.e207.woojoobook.domain.userbook.UserbookReader;
import com.e207.woojoobook.domain.userbook.UserbookStateManager;
import com.e207.woojoobook.global.exception.ErrorCode;
import com.e207.woojoobook.global.exception.ErrorException;
import com.e207.woojoobook.global.helper.UserHelper;

@Service
public class UserbookService {

	private final Integer MAX_AREA_CODE_SIZE;

	private final UserbookReader userbookReader;
	private final BookReader bookReader;
	private final UserHelper userHelper;
	private final UserbookStateManager userbookStateManager;
	private final ApplicationEventPublisher eventPublisher;

	public UserbookService(@Value("${userbook.search.ereacode.count}") Integer MAX_AREA_CODE_SIZE,
		UserbookReader userbookReader, BookReader bookReader, UserHelper userHelper,
		UserbookStateManager userbookStateManager,ApplicationEventPublisher eventPublisher) {
		this.MAX_AREA_CODE_SIZE = MAX_AREA_CODE_SIZE;
		this.userbookReader = userbookReader;
		this.bookReader = bookReader;
		this.userHelper = userHelper;
		this.userbookStateManager = userbookStateManager;
		this.eventPublisher = eventPublisher;
	}

	@Transactional(readOnly = true)
	public Page<UserbookResponse> findUserbookPageList(UserbookPageFindRequest request, Pageable pageable) {
		if (request.areaCodeList().size() > MAX_AREA_CODE_SIZE) {
			throw new ErrorException(ErrorCode.IllegalArgument);
		}

		User user = userHelper.findCurrentUser();
		Page<Userbook> userbookListByPage = this.userbookReader.findUserbookListByPage(user, request.toCondition(),
			pageable);

		return userbookListByPage.map(UserbookResponse::of);
	}

	@Transactional
	public UserbookResponse createUserbook(UserbookCreateRequest request) {
		User user = userHelper.findCurrentUser();
		Book book = bookReader.findBookOrSave(request.isbn());

		Userbook userbook = userbookReader.createUserbook(user, book, request.registerType(), request.quality());

		this.eventPublisher.publishEvent(new PointEvent(user, PointHistory.BOOK_REGISTER));
		this.eventPublisher.publishEvent(new ExperienceEvent(user, ExperienceHistory.BOOK_REGISTER));

		return UserbookResponse.of(userbook);
	}

	@Transactional
	public UserbookResponse updateUserbook(Long userbookId, UserbookUpdateRequest request) {
		User user = userHelper.findCurrentUser();
		Userbook userbook = userbookReader.findOwnedUserbook(user, userbookId);

		RegisterType registerType = userbook.getRegisterType();
		registerType = request.canRent() ? registerType.rentalOn() : registerType.rentalOff();
		registerType = request.canExchange() ? registerType.exchangeOn() : registerType.exchangeOff();

		userbookStateManager.updateRegisterType(userbook, registerType);
		userbookStateManager.updateQualityStatus(userbook, request.quality());

		return UserbookResponse.of(userbook);
	}

	public Userbook findUserbook(Long id) {
		return userbookReader.findDomain(id);
	}
}
