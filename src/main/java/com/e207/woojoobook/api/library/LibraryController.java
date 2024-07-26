package com.e207.woojoobook.api.library;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.e207.woojoobook.api.library.request.LibraryCreateRequest;
import com.e207.woojoobook.api.library.request.LibraryUpdateRequest;
import com.e207.woojoobook.api.library.response.LibraryListResponse;
import com.e207.woojoobook.api.library.response.LibraryResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/libraries")
@RestController
public class LibraryController {
	private final LibraryService libraryService;

	@GetMapping
	public ResponseEntity<LibraryListResponse> findList() {
		return ResponseEntity.status(HttpStatus.OK).body(libraryService.findList());
	}

	@GetMapping("/{id}")
	public ResponseEntity<LibraryResponse> findById(@PathVariable Long id) {
		return ResponseEntity.status(HttpStatus.OK).body(libraryService.find(id));
	}

	@PostMapping("/categories")
	public ResponseEntity<LibraryResponse> create(@Valid @RequestBody LibraryCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(libraryService.create(request));
	}

	@PutMapping("/categories/{id}")
	public ResponseEntity<LibraryResponse> update(@PathVariable Long id,
		@RequestBody LibraryUpdateRequest request) {
		return ResponseEntity.status(HttpStatus.OK).body(libraryService.update(id, request));
	}

	@PutMapping("/categories/{from}/{to}")
	public ResponseEntity<?> swapOrderNumber(@PathVariable Long from, @PathVariable Long to) {
		libraryService.swapOrderNumber(from, to);
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@DeleteMapping("/categories/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		libraryService.delete(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}