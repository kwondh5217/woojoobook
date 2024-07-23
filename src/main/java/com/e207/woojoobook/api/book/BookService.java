package com.e207.woojoobook.api.book;

import java.util.List;

import org.springframework.stereotype.Service;

import com.e207.woojoobook.api.book.request.BookFindRequest;
import com.e207.woojoobook.api.book.response.BookListResponse;
import com.e207.woojoobook.client.BookSearchClient;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BookService {
	private final BookSearchClient bookSearchClient;

	public BookListResponse findBookList(BookFindRequest request) {
		String keyword = request.keyword().trim();
		int page = request.page();
		int size = 20;

		if (keyword.isEmpty()) {
			return new BookListResponse(List.of());
		}

		return bookSearchClient.findBookByKeyword(keyword, page, size);
	}
}