package com.e207.woojoobook.domain.userbook;

import static com.e207.woojoobook.domain.book.QBook.*;
import static com.e207.woojoobook.domain.userbook.QUserbook.*;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@Repository
public class UserbookQueryRepository {

	private static final BooleanExpression ALL_TRADE_EXPRESSION = userbook.tradeStatus.in(TradeStatus.RENTAL_AVAILABLE,
		TradeStatus.EXCHANGE_AVAILABLE, TradeStatus.RENTAL_EXCHANGE_AVAILABLE);
	private static final BooleanExpression RENTAL_TRADE_EXPRESSION = userbook.tradeStatus.in(
		TradeStatus.RENTAL_AVAILABLE, TradeStatus.RENTAL_EXCHANGE_AVAILABLE);
	private static final BooleanExpression EXCHANGE_TRADE_EXPRESSION = userbook.tradeStatus.in(
		TradeStatus.EXCHANGE_AVAILABLE, TradeStatus.RENTAL_EXCHANGE_AVAILABLE);

	private final JPAQueryFactory queryFactory;

	public UserbookQueryRepository(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	public Page<Userbook> findTradeablePage(TradeableUserbookCondition condition, Pageable pageable) {
		BooleanExpression[] tradeableExpressions = {
			isKeywordInTitleOrAuthor(condition.keyword()),
			hasRegisterType(condition.registerType()),
			canExecuteTrade(condition.registerType()),
			isAreaCodeInList(condition.areaCodeList())
		};
		return findPageWithExpressions(pageable, tradeableExpressions);
	}

	public Page<Userbook> findMyExchangablePage(MyExchangableUserbookCondition condition, Pageable pageable) {
		BooleanExpression[] myExchangableExpressions = {
			hasRegisterType(RegisterType.EXCHANGE),
			canExecuteTrade(RegisterType.EXCHANGE),
			isOwnedByUser(condition.userId())
		};
		return findPageWithExpressions(pageable, myExchangableExpressions);
	}

	public Page<Userbook> findMyPage(MyUserbookCondition condition, Pageable pageable) {
		BooleanExpression[] myUserbookExpressions = {
			isOwnedByUser(condition.userId()),
			isTradeStatus(condition.tradeStatus()),
			isNotInactive()
		};
		return findPageWithExpressions(pageable, myUserbookExpressions);
	}

	private Page<Userbook> findPageWithExpressions(Pageable pageable, BooleanExpression... expressions) {
		List<Userbook> content = this.queryFactory.selectFrom(userbook)
			.join(userbook.book, book)
			.where(expressions)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long count = this.countWithExpressions(expressions);
		return new PageImpl<>(content, pageable, count);
	}

	private Long countWithExpressions(BooleanExpression... expressions) {
		return this.queryFactory.select(userbook.count())
			.from(userbook)
			.where(expressions)
			.fetchOne();
	}

	private BooleanExpression isKeywordInTitleOrAuthor(String keyword) {
		return StringUtils.hasText(keyword) ?
			book.title.contains(keyword.trim()).or(book.author.contains(keyword.trim())) : null;
	}

	private BooleanExpression isAreaCodeInList(List<String> codeList) {
		return codeList.isEmpty() ? null : userbook.areaCode.in(codeList);
	}

	private BooleanExpression canExecuteTrade(RegisterType registerType) {
		if (Objects.isNull(registerType)) {
			return ALL_TRADE_EXPRESSION;
		}

		return switch (registerType) {
			case RENTAL -> RENTAL_TRADE_EXPRESSION;
			case EXCHANGE -> EXCHANGE_TRADE_EXPRESSION;
			default -> ALL_TRADE_EXPRESSION;
		};
	}

	private BooleanExpression hasRegisterType(RegisterType registerType) {
		return Objects.isNull(registerType) ? null :
			userbook.registerType.eq(registerType).or(userbook.registerType.eq(RegisterType.RENTAL_EXCHANGE));
	}

	private BooleanExpression isOwnedByUser(Long userId) {
		return userId == null ? null : userbook.user.id.eq(userId);
	}

	private BooleanExpression isNotInactive() {
		return userbook.registerType.ne(RegisterType.INACTIVE).and(userbook.tradeStatus.ne(TradeStatus.UNAVAILABLE));
	}

	private BooleanExpression isTradeStatus(TradeStatus tradeStatus) {
		return userbook.tradeStatus.eq(tradeStatus);
	}
}
