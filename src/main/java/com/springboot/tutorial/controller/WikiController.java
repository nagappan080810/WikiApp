package com.springboot.tutorial.controller;


import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.terracotta.shaded.lucene.util.IOUtils;
import org.thymeleaf.util.StringUtils;

import com.springboot.tutorial.domain.StickyNote;
import com.springboot.tutorial.domain.WikiFilter;
import com.springboot.tutorial.services.CacheService;

@Controller
@RequestMapping("/wiki")
public class WikiController {
	
	private static final int BUFFER_SIZE = 4096;
	
	private final CacheService cacheService;
	
	@Autowired
	public WikiController(CacheService cacheService){
		this.cacheService = cacheService;
	}
	
	@RequestMapping("/add")
	public String addToWiki(@RequestParam(value="key", required=false, defaultValue = "default") String key, 
				            @RequestParam(value="value", required=false) String value, Model model){
		System.out.println("entered into add wiki"+key);
		Element element = new Element(key, value);
		Collection<Element> wikiRecords  = cacheService.addToCache(element);
		model.addAttribute("wikiRecords", wikiRecords);
		System.out.println("elements in wiki records - "+wikiRecords);
		return "wiki";
	}
	
	@RequestMapping("/delete/{key}")
	public String deletetWikiNote(@PathVariable("key") String key, Model model){
		System.out.println("Executing the delete method");
		if (!cacheService.removeCacheElement(key)){
			System.out.println("***could not be deleted!!!***");
		}
		Collection<Element> wikiRecords  = cacheService.getCacheElements();
		model.addAttribute("wikiRecords", wikiRecords);
		model.addAttribute("allSearchTypes", WikiFilter.WikiSearchType.values());
		model.addAttribute("stickyNote", new StickyNote());
		model.addAttribute("wikiFilter", new WikiFilter());
		return "wiki";
	}
	
	@RequestMapping("/save")
	public String saveToWiki(@ModelAttribute StickyNote stickyNote, Model model){
		System.out.println("wiki element"+model.asMap().get("stickyNote"));
		System.out.println("wiki element value"+stickyNote);
		Element wikiElement = new Element(stickyNote.getTitle(), stickyNote.getContent());
		Collection<Element> wikiRecords  = cacheService.addToCache(wikiElement);
		model.addAttribute("allSearchTypes", WikiFilter.WikiSearchType.values());
		model.addAttribute("wikiRecords", wikiRecords);
		model.addAttribute("stickyNote", new StickyNote());
		model.addAttribute("wikiFilter", new WikiFilter());
		return "wiki";
	}
	
	@RequestMapping("/search")
	public String searchWiki(@ModelAttribute WikiFilter wikiFilter, Model model){
		Collection<Element> wikiRecords = cacheService.searchWiki(wikiFilter);
		model.addAttribute("wikiRecords", wikiRecords);
		model.addAttribute("wikiFilter", new WikiFilter());
		model.addAttribute("stickyNote", new StickyNote());
		model.addAttribute("allSearchTypes", WikiFilter.WikiSearchType.values());
		return "wiki";
	}
	
	@RequestMapping(value={"/view",""})
	public String viewWiki(Model model){
		System.out.println("wiki view method is invoked");
		Collection<Element> wikiRecords  = cacheService.getCacheElements();
		model.addAttribute("wikiRecords", wikiRecords);
		String searchTypeStrings[] = new String[3];
		int i = 0;
		for (WikiFilter.WikiSearchType searchType : WikiFilter.WikiSearchType.values()){
			searchTypeStrings[i++] = searchType.name();
		}
		model.addAttribute("allSearchTypes", WikiFilter.WikiSearchType.values());
		
		//if sticky note is not present, new object has been created and added..
		if (!model.containsAttribute("stickyNote")){
			model.addAttribute("stickyNote", new StickyNote());
		}
		
		//if wiki filter element is not present, new object has been created and added..
		if (!model.containsAttribute("wikiFilter")){
			model.addAttribute("wikiFilter", new WikiFilter());
		}
		
		return "wiki";
	}
	
	@RequestMapping(value={"/download"})
	public void downloadWiki(Model model, HttpServletResponse response){
		System.out.println("Downloading of wiki starts...");
		String NEWLINE_CHARACTER = "\n";
		Collection<Element> wikiRecords = cacheService.getCacheElements();
		FileWriter fileWriter = null;
		BufferedWriter bw = null;
		try {
			fileWriter = new FileWriter("wikinotes.txt");
			bw = new BufferedWriter(fileWriter);
			//write buffered output stream..
			for (Element wikiRecord: wikiRecords){
				bw.write(StringUtils.repeat("*", 100));
				bw.write(NEWLINE_CHARACTER);
				
				bw.write(StringUtils.repeat(">", 15));
				bw.write(wikiRecord.getObjectKey().toString());
				bw.write(NEWLINE_CHARACTER);
				bw.write(StringUtils.repeat("*", 100));
				bw.write(NEWLINE_CHARACTER);
				
				bw.write(wikiRecord.getObjectValue().toString());
				bw.write(NEWLINE_CHARACTER);
				
				bw.write(StringUtils.repeat("*", 100));
				bw.write(NEWLINE_CHARACTER);
				bw.write(NEWLINE_CHARACTER);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//writing to HTTP response.
		response.addHeader("Content-Disposition", "attachment; filename=wikinotes.txt");
		FileInputStream fis = null;
		OutputStream out = null;
		byte[] bytes = new byte[BUFFER_SIZE];
		int bytesRead = 0;

		try {
			fis = new FileInputStream("wikinotes.txt");
			out = response.getOutputStream();
			while((bytesRead = fis.read(bytes)) != -1){
				out.write(bytes,0, bytesRead);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally{
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
//		model.addAttribute("wikiRecords", wikiRecords);
//		model.addAttribute("wikiFilter", new WikiFilter());
//		model.addAttribute("stickyNote", new StickyNote());
//		model.addAttribute("allSearchTypes", WikiFilter.WikiSearchType.values());
//		System.out.println("Wiki download completed.");
//		return "wiki";
	}

}
