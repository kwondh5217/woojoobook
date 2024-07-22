package com.e207.woojoobook.domain.rental;

import java.time.LocalDateTime;

import com.e207.woojoobook.domain.user.User;
import com.e207.woojoobook.domain.userbook.Userbook;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Rental {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	private User user;
	@ManyToOne
	private Userbook userbook;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private RentalStatus rentalStatus;

	@Builder
	public Rental(User user, Userbook userbook) {
		this.user = user;
		this.userbook = userbook;
	}

	public void respond(boolean isApproved) {
		if (isApproved) {
			this.startDate = LocalDateTime.now();
			this.endDate = LocalDateTime.now().plusDays(7);
		} else {
			this.rentalStatus = RentalStatus.REJECTED;
		}
	}
}
