package com.mcart.search.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchResponseTest {

	@Test
	void computeTotalPages_zeroHits() {
		assertEquals(0, SearchResponse.computeTotalPages(0, 10));
	}

	@Test
	void computeTotalPages_nonPositiveSize() {
		assertEquals(0, SearchResponse.computeTotalPages(100, 0));
		assertEquals(0, SearchResponse.computeTotalPages(100, -1));
	}

	@Test
	void computeTotalPages_exactMultiple() {
		assertEquals(2, SearchResponse.computeTotalPages(20, 10));
	}

	@Test
	void computeTotalPages_partialLastPage() {
		assertEquals(3, SearchResponse.computeTotalPages(21, 10));
	}

	@Test
	void computeTotalPages_capsAtIntegerMax() {
		long huge = (long) Integer.MAX_VALUE + 10L;
		assertEquals(Integer.MAX_VALUE, SearchResponse.computeTotalPages(huge, 1));
	}
}
