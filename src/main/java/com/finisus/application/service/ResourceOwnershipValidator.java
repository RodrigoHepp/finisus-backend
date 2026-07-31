package com.finisus.application.service;

import com.finisus.domain.DomainException;

public final class ResourceOwnershipValidator {

	private ResourceOwnershipValidator() {
	}

	public static void assertOwner(Long resourceOwnerId, Long currentUserId) {
		if (resourceOwnerId == null || currentUserId == null || !resourceOwnerId.equals(currentUserId)) {
			throw new DomainException("error.access.denied");
		}
	}
}
