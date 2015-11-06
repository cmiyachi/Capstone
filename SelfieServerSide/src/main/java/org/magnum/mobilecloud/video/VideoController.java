/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video;


import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.repository.VideoFileManager;
import org.magnum.mobilecloud.video.repository.VideoStatus;
import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import marvin.gui.MarvinImagePanel;
import marvin.image.MarvinImage;
import marvin.image.MarvinImageMask;
import marvin.io.MarvinImageIO;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinPluginLoader;

@Controller
public class VideoController {
	// Note: storage for video ratings,
	//	where key is video id and value list of users voted
	//  for the video
	HashMap<Long, List<String>> m_videoRatings;
	private long number = 1; 
	
	@Autowired
	private VideoRepository m_videoRepository;
	
	private VideoFileManager videoDataMgr;
	
	public VideoController() {
		m_videoRatings = new HashMap<Long, List<String>>();
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return (Collection<Video>)m_videoRepository.findAll();
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method = RequestMethod.GET)
	public @ResponseBody Video getVideoById(@PathVariable("id") long id, HttpServletResponse response) {
		Video savedVideo = m_videoRepository.findById(id);
		if (savedVideo == null) {
			try {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		return savedVideo;
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video video, Principal principal, HttpServletResponse response) {
		// Note: set up owner and like properties
		video.setOwner(principal.getName());
		video.setLikes(0);
		
		System.out.println("I'm adding a video ************************");
		
		// Note: let's check the existence of the video and the owner
		Video savedVideo = m_videoRepository.findById(video.getId());
		if (savedVideo != null) {
			if (savedVideo.getOwner().compareTo(principal.getName()) != 0) {
				// only owner can overwrite the video
				try {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		}
		Video return_video = save(video);  // sets the URL when it saves it
		Video another_video =  m_videoRepository.save(video);
		return return_video; 
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method = RequestMethod.POST)
	public Void likeVideo(@PathVariable("id") long id, Principal principal, HttpServletResponse response) {
		Video savedVideo = getVideoById(id, response);
		if (savedVideo == null) {
			return null;
		}
		String filepath = videoDataMgr.getFilePath(savedVideo); 
		System.out.println("FilePath ************************" +filepath);
		System.out.flush();
		
		/**
		if (!doesRatingExist(savedVideo, principal.getName())) {
			savedVideo.setLikes(savedVideo.getLikes() + 1);
			m_videoRatings.get(savedVideo.getId()).add(principal.getName());
			
			m_videoRepository.save(savedVideo);
		}
		else {
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		**/
		return null;
		
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method = RequestMethod.POST)
	public Void unlikeVideo(@PathVariable("id") long id, Principal principal, HttpServletResponse response) {
		Video savedVideo = getVideoById(id, response);
		if (savedVideo == null) {
			return null;
		}
		String filepath = videoDataMgr.getFilePath(savedVideo); 
		System.out.println("FilePath ************************" +filepath);
		/**
		if (doesRatingExist(savedVideo, principal.getName())) {
			savedVideo.setLikes(savedVideo.getLikes() - 1);
			m_videoRatings.get(savedVideo.getId()).remove(principal.getName());
			
			m_videoRepository.save(savedVideo);
		}
		else {
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		**/
		return null;
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method = RequestMethod.GET)
	public @ResponseBody Collection<String> getUsersWhoLikedVideo(@PathVariable("id") long id, HttpServletResponse response) {
		Video savedVideo = getVideoById(id, response);
		if (savedVideo == null) {
			return null;
		}
		
		if (!m_videoRatings.containsKey(savedVideo.getId())) {
			try {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
		List<String> users = (Vector<String>)m_videoRatings.get(savedVideo.getId());
		
		Vector<String> liked = new Vector<String>();
		for(String user : users) {
			liked.add(user);
		}
		return liked;
		
	}
	
	private boolean doesRatingExist(Video video, String userName) {
		if (!m_videoRatings.containsKey(video.getId())) {			
			m_videoRatings.put(video.getId(), new Vector<String>());
		}
		else {
			List<String> users = m_videoRatings.get(video.getId());
			for(String user : users) {
				if (user.compareTo(userName) == 0) {					
					return true;
				}
			}
		}
		
		return false;
	}
	
	// Receives a POST to /video/{id} with "data" which is multipart binary data
			// of the video.  This video is stored using existing code with the Video Manager.
			// The return is converted to JSON by Spring because of the @ResponseBody
			@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.POST)
			public  @ResponseBody VideoStatus setVideoData(@PathVariable("id") long id, @RequestParam("data") MultipartFile data,  Principal principal, HttpServletResponse response)
			{
				
				System.out.println("inside setting video data ************************");
				Video video = new Video();
				// Note: set up owner and like properties
				video.setOwner(principal.getName());
				video.setLikes(0);
				video.setName("Selfie"+Long.toString(number++)+".jpg");
				System.out.println("I'm adding a video ************************");
				
				
				Video return_video = save(video);  // sets the URL when it saves it


				
				
				Video savedVideo = getVideoById(id, response);
				if (savedVideo == null) {
					return null;
				}
				
				else
				{
					try {
						videoDataMgr = VideoFileManager.get();
						System.out.println("************ savedVideo "+savedVideo.getUrl());
						if (data == null)
							System.out.println("***************** data is NULL *********");
	                    saveSomeVideo(savedVideo, data);
	                 } catch (IOException e1) {
	                   e1.printStackTrace();
	                 }
				}
				
				
				VideoStatus vs = new VideoStatus(VideoStatus.VideoState.READY); 
				return vs;
			}

			// Receives GET requests to /video and returns the video associated with the id. 
			// Note the the Servlet response is used to send the binary data back
			@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.GET)
			public HttpServletResponse  getVideoData(@PathVariable("id") long id, HttpServletResponse response)throws IOException
			{
				videoDataMgr = VideoFileManager.get();
				// look for video associated with the id
				Video savedVideo = getVideoById(id, response);
				if (savedVideo == null) {
					return null;
				}
				else {

					serveSomeVideo(savedVideo, response);

				}

				return response;
				
			}
			// PRIVATE HELPER FUNCTIONS
			// These functions were provided in the ReadMe file for the assignement and I used them
			// almost "as is".  
			
			
			// Create a URL of a video based on the id
			private String getDataUrl(long videoId){
	            String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
	            return url;
	        }

			// get the base URL for this server running
	     	private String getUrlBaseForLocalServer() {
			   HttpServletRequest request = 
			       ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			   String base = 
			      "http://"+request.getServerName() 
			      + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
			   return base;
			}
	     	
	     	
	     	// This section is used to great a long for the id that can only change atomoically. 
	     	 private static final AtomicLong currentId = new AtomicLong(0L);
	     	
	     	
	     	 // Save the video by creating an id and obtaining the URL
	       	public Video save(Video entity) {
	     		checkAndSetId(entity);
	     		String url = getDataUrl(entity.getId());
	     		entity.setUrl(url);
	     		m_videoRepository.save(entity);  //put(entity.getId(), entity);
	     		return entity;
	     	}

	       	// Create the ID of the video
	     	private void checkAndSetId(Video entity) {
	     		if(entity.getId() == 0){
	     			entity.setId(currentId.incrementAndGet());
	     		}
	     	}
	     	
	     	// These helper functions save the video and serve the video up, using the Video Manager
	     	// provided to us. 
	     	
	     	public void saveSomeVideo(Video v, MultipartFile videoData) throws IOException {
	     		System.out.println("*********** v is "+v.getName());
	     		System.out.println("*********** data is "+videoData.getName());
	     	     videoDataMgr.saveVideoData(v, videoData.getInputStream());
	     	}
	     	
	     	public void serveSomeVideo(Video v, HttpServletResponse response) throws IOException {
	     	     videoDataMgr.copyVideoData(v, response.getOutputStream());
	     	}
}
