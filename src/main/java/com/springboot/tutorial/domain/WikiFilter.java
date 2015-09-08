package com.springboot.tutorial.domain;

public class WikiFilter {
	
	public enum WikiSearchType{
		SEARCH_BY_KEY,
		SEARCH_BY_VALUE,
		SEARCH_BY_KEY_AND_VALUE
	}
	
	private String searchValue;
	private WikiSearchType searchType = WikiSearchType.SEARCH_BY_KEY_AND_VALUE;
	
	public String getSearchValue() {
		return searchValue;
	}
	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}
	public WikiSearchType getSearchType() {
		return searchType;
	}
	public void setSearchType(WikiSearchType searchType) {
		this.searchType = searchType;
	}

}
