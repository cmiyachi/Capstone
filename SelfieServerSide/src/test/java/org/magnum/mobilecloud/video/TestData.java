package org.magnum.mobilecloud.video;

import java.io.File;
import java.util.UUID;

import org.magnum.mobilecloud.video.repository.Video;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This is a utility class to aid in the construction of
 * Video objects with random names, urls, and durations.
 * The class also provides a facility to convert objects
 * into JSON using Jackson, which is the format that the
 * VideoSvc controller is going to expect data in for
 * integration testing.
 * 
 * @author jules
 *
 */
public class TestData {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	private static final String SERVER = "http://localhost:8080";

	//private File testVideoData = new File(
	//		"src/test/resources/test.mp4");
	public File testVideoData = new File(
			"src/test/resources/testphoto.jpg");
	
	// private Video video = Video.create().withContentType("video/mp4")
	/*	private Video video = Video.create().withContentType("image/jpg")
				.withDuration(123).withSubject(UUID.randomUUID().toString())
				.withTitle(UUID.randomUUID().toString()).build(); **/
	
		public static Video pictureVideo ()
		{
			String id = UUID.randomUUID().toString();
			String title = "Picture-"+id; 
			long duration = 123; 
			String url = "http://coursera.org/some/video-"+id;
			return new Video(title, url, duration, 0); 
		}
	/**
	 * Construct and return a Video object with a
	 * rnadom name, url, and duration.
	 * 
	 * @return
	 */
	public static Video randomVideo() {
		// Information about the video
		// Construct a random identifier using Java's UUID class
		String id = UUID.randomUUID().toString();
		String title = "Video-"+id;
		String url = "http://coursera.org/some/video-"+id;
		long duration = 60 * (int)Math.rint(Math.random() * 60) * 1000; // random time up to 1hr
		return new Video(title, url, duration, 0);
	}
	
	/**
	 *  Convert an object to JSON using Jackson's ObjectMapper
	 *  
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public static String toJson(Object o) throws Exception{
		return objectMapper.writeValueAsString(o);
	}
}
