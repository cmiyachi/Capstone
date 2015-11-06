package org.magnum.mobilecloud.integration.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.magnum.mobilecloud.video.repository.VideoStatus;
import org.magnum.mobilecloud.video.repository.VideoStatus.VideoState;
import org.magnum.mobilecloud.video.TestData;
import org.magnum.mobilecloud.video.client.SecuredRestBuilder;
import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import retrofit.RetrofitError;
import retrofit.client.ApacheClient;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * A test for the Asgn2 video service
 * 
 * @author mitchell
 */
public class VideoSvcApiTest {

	private class ErrorRecorder implements ErrorHandler {

		private RetrofitError error;

		@Override
		public Throwable handleError(RetrofitError cause) {
			error = cause;
			return error.getCause();
		}

		public RetrofitError getError() {
			return error;
		}
	}

	private final String TEST_URL = "https://localhost:8443";

	private final String USERNAME1 = "admin";
	private final String USERNAME2 = "user0";
	private final String PASSWORD = "pass";
	private final String CLIENT_ID = "mobile";
	
	private File testVideoData2 = new File("src/test/resources/test.mp4");

	private VideoSvcApi videoSvc = new SecuredRestBuilder()
			.setLoginEndpoint(TEST_URL + VideoSvcApi.TOKEN_PATH)
			.setUsername(USERNAME1)
			.setPassword(PASSWORD)
			.setClientId(CLIENT_ID)
			.setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient()))
			.setEndpoint(TEST_URL).setLogLevel(LogLevel.FULL).build()
			.create(VideoSvcApi.class);

	private VideoSvcApi readWriteVideoSvcUser1 = new SecuredRestBuilder()
			.setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient()))
			.setEndpoint(TEST_URL)
			.setLoginEndpoint(TEST_URL + VideoSvcApi.TOKEN_PATH)
			// .setLogLevel(LogLevel.FULL)
			.setUsername(USERNAME1).setPassword(PASSWORD).setClientId(CLIENT_ID)
			.build().create(VideoSvcApi.class);

	private VideoSvcApi readWriteVideoSvcUser2 = new SecuredRestBuilder()
			.setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient()))
			.setEndpoint(TEST_URL)
			.setLoginEndpoint(TEST_URL + VideoSvcApi.TOKEN_PATH)
			// .setLogLevel(LogLevel.FULL)
			.setUsername(USERNAME2).setPassword(PASSWORD).setClientId(CLIENT_ID)
			.build().create(VideoSvcApi.class);

	private Video video = TestData.randomVideo();
	private Video videoPicture = TestData.pictureVideo();
	private File testPictureData = new File(
			"src/test/resources/testphoto.jpg");
	
	@Test
	public void testAddVideoData() throws Exception {
		Video received = videoSvc.addVideo(videoPicture);
		VideoStatus status = videoSvc.setVideoData(received.getId(),
				new TypedFile("image/jpg", testPictureData));
		assertEquals(VideoState.READY, status.getState());

		Response response = videoSvc.getVideoData(received.getId());
		assertEquals(200, response.getStatus());

		InputStream videoData = response.getBody().in();
		byte[] originalFile = IOUtils.toByteArray(new FileInputStream(
				testPictureData));
		byte[] retrievedFile = IOUtils.toByteArray(videoData);
		
		boolean result = false; 
		int arr_len = originalFile.length;
		for(int i = 0, j = 0, count = 0; count < arr_len; count++, i++, j++)
		    if(originalFile[i] != retrievedFile[j])
		    	result =  false;
		    else
		    	result = true;
		assertTrue(result);
		// assertTrue(Arrays.equals(originalFile, retrievedFile));
	}
	
	
/**

	@Test
	public void testAddVideoData() throws Exception {
		Video received = readWriteVideoSvcUser1.addVideo(videoPicture);
		VideoStatus status = readWriteVideoSvcUser1.setVideoData(received.getId(),
				new TypedFile("image/jpg", testVideoData));
		assertEquals(VideoState.READY, status.getState());
		
		Response response = readWriteVideoSvcUser1.getVideoData(received.getId());
		assertEquals(200, response.getStatus());
		
		InputStream videoData = response.getBody().in();
		byte[] originalFile = IOUtils.toByteArray(new FileInputStream(testVideoData));
		byte[] retrievedFile = IOUtils.toByteArray(videoData);
		assertTrue(Arrays.equals(originalFile, retrievedFile));
	}
	**/

	@Test
	public void testAddVideoMetadata() throws Exception {
		Video received = readWriteVideoSvcUser1.addVideo(video);
		assertEquals(video.getName(), received.getName());
		assertEquals(video.getDuration(), received.getDuration());
		assertTrue(received.getLikes() == 0);
		assertTrue(received.getId() > 0);
	}

	@Test
	public void testAddGetVideo() throws Exception {
		Video updated_video = readWriteVideoSvcUser1.addVideo(video);
		video.setId(updated_video.getId());
		Collection<Video> stored = readWriteVideoSvcUser1.getVideoList();
		
		assertTrue(stored.contains(video));
	}
	
	// Note: this is an additional test for the case when another user
	//  tries to overwrite the existed video
	@Test
	public void testDenyOverwritingVideoByOtherUser() throws Exception {
		Video savedVideo = readWriteVideoSvcUser1.addVideo(video);
		
		Collection<Video> stored = readWriteVideoSvcUser1.getVideoList();
		assertTrue(stored.contains(video));
		
		try {
			readWriteVideoSvcUser2.addVideo(savedVideo);
			fail("Another user can overwrite existing file.");
		}
		catch(RetrofitError e) {
			assertEquals(HttpStatus.SC_BAD_REQUEST, e.getResponse().getStatus());
		}
	}

	@Test
	public void testDenyVideoAddWithoutOAuth() throws Exception {
		ErrorRecorder error = new ErrorRecorder();

		// Create an insecure version of our Rest Adapter that doesn't know how
		// to use OAuth.
		VideoSvcApi insecurevideoService = new RestAdapter.Builder()
				.setClient(
						new ApacheClient(UnsafeHttpsClient.createUnsafeClient()))
				.setEndpoint(TEST_URL).setLogLevel(LogLevel.FULL)
				.setErrorHandler(error).build().create(VideoSvcApi.class);
		try {
			// This should fail because we haven't logged in!
			insecurevideoService.addVideo(video);

			fail("Yikes, the security setup is horribly broken and didn't require the user to authenticate!!");

		} catch (Exception e) {
			// Ok, our security may have worked, ensure that
			// we got a 401
			assertEquals(HttpStatus.SC_UNAUTHORIZED, error.getError()
					.getResponse().getStatus());
		}

		// We should NOT get back the video that we added above!
		Collection<Video> videos = readWriteVideoSvcUser1.getVideoList();
		assertFalse(videos.contains(video));
	}

	@Test
	public void testLikeCount() throws Exception {

		// Add the video
		Video v = readWriteVideoSvcUser1.addVideo(video);

		// Like the video
		readWriteVideoSvcUser1.likeVideo(v.getId());

		// Get the video again
		v = readWriteVideoSvcUser1.getVideoById(v.getId());

		// Make sure the like count is 1
		assertTrue(v.getLikes() == 1);

		// Unlike the video
		readWriteVideoSvcUser1.unlikeVideo(v.getId());

		// Get the video again
		v = readWriteVideoSvcUser1.getVideoById(v.getId());

		// Make sure the like count is 0
		assertTrue(v.getLikes() == 0);
	}

	@Test
	public void testLikedBy() throws Exception {

		// Add the video
		Video v = readWriteVideoSvcUser1.addVideo(video);

		// Like the video
		readWriteVideoSvcUser1.likeVideo(v.getId());

		Collection<String> likedby = readWriteVideoSvcUser1.getUsersWhoLikedVideo(v.getId());

		// Make sure we're on the list of people that like this video
		assertTrue(likedby.contains(USERNAME1));
		
		// Have the second user like the video
		readWriteVideoSvcUser2.likeVideo(v.getId());
		
		// Make sure both users show up in the like list
		likedby = readWriteVideoSvcUser1.getUsersWhoLikedVideo(v.getId());
		assertTrue(likedby.contains(USERNAME1));
		assertTrue(likedby.contains(USERNAME2));

		// Unlike the video
		readWriteVideoSvcUser1.unlikeVideo(v.getId());

		// Get the video again
		likedby = readWriteVideoSvcUser1.getUsersWhoLikedVideo(v.getId());

		// Make sure user1 is not on the list of people that liked this video
		assertTrue(!likedby.contains(USERNAME1));
		
		// Make sure that user 2 is still there
		assertTrue(likedby.contains(USERNAME2));
	}

	@Test
	public void testLikingTwice() throws Exception {

		// Add the video
		Video v = readWriteVideoSvcUser1.addVideo(video);

		// Like the video
		readWriteVideoSvcUser1.likeVideo(v.getId());

		// Get the video again
		v = readWriteVideoSvcUser1.getVideoById(v.getId());

		// Make sure the like count is 1
		assertTrue(v.getLikes() == 1);

		try {
			// Like the video again.
			readWriteVideoSvcUser1.likeVideo(v.getId());

			fail("The server let us like a video twice without returning a 400");
		} catch (RetrofitError e) {
			// Make sure we got a 400 Bad Request
			assertEquals(400, e.getResponse().getStatus());
		}

		// Get the video again
		v = readWriteVideoSvcUser1.getVideoById(v.getId());

		// Make sure the like count is still 1
		assertTrue(v.getLikes() == 1);
	}

	@Test
	public void testLikingNonExistantVideo() throws Exception {

		try {
			// Like the video again.
			readWriteVideoSvcUser1.likeVideo(getInvalidVideoId());

			fail("The server let us like a video that doesn't exist without returning a 404.");
		} catch (RetrofitError e) {
			// Make sure we got a 400 Bad Request
			assertEquals(404, e.getResponse().getStatus());
		}
	}

	private long getInvalidVideoId() {
		Set<Long> ids = new HashSet<Long>();
		Collection<Video> stored = readWriteVideoSvcUser1.getVideoList();
		for (Video v : stored) {
			ids.add(v.getId());
		}

		long nonExistantId = Long.MIN_VALUE;
		while (ids.contains(nonExistantId)) {
			nonExistantId++;
		}
		return nonExistantId;
	}

}
