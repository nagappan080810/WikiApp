package com.springboot.tutorial.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.springboot.tutorial.domain.WikiFilter;
import com.springboot.tutorial.domain.WikiFilter.WikiSearchType;

@Service(value="cacheService")
public class CacheService {
	
	private static final String CACHE_NAME = "wikiCache";
	
	@Value("${wiki.cache.runMode}")
	private String runCacheMode;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	private Cache wikiCache;
	
	private CacheService(){
		CacheManager cm = CacheManager.getInstance();
//		cm.addCache(CACHE_NAME);
		wikiCache = cm.getCache(CACHE_NAME);
	}
	
	@PostConstruct
	public void initializeCache(){
		System.out.println("Initialize Cache method invoked");
		System.out.println("context property source bean injection" + applicationContext);
		System.out.println("cache mode"+runCacheMode);
		if (runCacheMode.equals("CleanRun")){
			wikiCache.removeAll();
		}
	}

	public Collection<Element> addToCache(Element element){
		wikiCache.put(element);
		System.out.println(" element to be added "+element);
		return getCacheElements();
	}
	
	public Collection<Element> getCacheElements(){
		Map<Object, Element> keysWithElements = wikiCache.getAll(wikiCache.getKeys());
		Collection<Element> elements = keysWithElements.values();
		/***to be removed**/
		System.out.println("context property source bean injection" + applicationContext);
		System.out.println("cache mode"+runCacheMode);
		for (Element element1: elements){
			System.out.println("key -" + element1.getObjectKey() + " value - "+ element1.getObjectValue());
		}
		/***to be removed**/
		return elements;
	}

	public Collection<Element> searchWiki(WikiFilter wikiFilter) {
		String searchValue = "*" + wikiFilter.getSearchValue() + "*";
		Results filteredWikis = null;
		WikiSearchType searchType = wikiFilter.getSearchType();
		System.out.println("search value -"+searchValue);
		switch (searchType){
			case SEARCH_BY_KEY:{
				filteredWikis = wikiCache.createQuery().addCriteria(Query.KEY.ilike(searchValue)).includeKeys().includeValues().execute();
				break;
			}
			case SEARCH_BY_VALUE:{
				filteredWikis = wikiCache.createQuery().addCriteria(Query.VALUE.ilike(searchValue)).includeKeys().includeValues().execute();
				break;
			}
			case SEARCH_BY_KEY_AND_VALUE: {
				filteredWikis = wikiCache.createQuery().addCriteria(Query.KEY.ilike(searchValue).or(Query.VALUE.ilike(searchValue))).includeKeys().includeValues().execute();
				break;
			}
			default: {
				System.out.println("Invalid search Type ");
				break;
			}
		}
		System.out.println("wiki search result size ---"+filteredWikis.all().size());
		List<Element> elements = new ArrayList<Element>();
		for (Result filterWiki: filteredWikis.all()){
			Element element = new Element(filterWiki.getKey(), filterWiki.getValue());
			System.out.println("wiki key -"+filterWiki.getKey());
			System.out.println("wiki value -"+filterWiki.getValue());
			elements.add(element);
		}
		return elements;
	}
	
	public boolean removeCacheElement(String key){
		return  wikiCache.remove(key);
	}
}
