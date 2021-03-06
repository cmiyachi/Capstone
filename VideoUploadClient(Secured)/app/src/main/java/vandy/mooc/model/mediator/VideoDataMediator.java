package vandy.mooc.model.mediator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter.LogLevel;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import vandy.mooc.common.Utils;
import vandy.mooc.model.mediator.webdata.SecuredRestBuilder;
import vandy.mooc.model.mediator.webdata.UnsafeHttpsClient;
import vandy.mooc.model.mediator.webdata.Video;
import vandy.mooc.model.mediator.webdata.VideoStatus;
import vandy.mooc.model.mediator.webdata.VideoStatus.VideoState;
import vandy.mooc.model.mediator.webdata.VideoSvcApi;
import vandy.mooc.utils.Constants;
import vandy.mooc.utils.VideoMediaStoreUtils;
import vandy.mooc.utils.VideoStorageUtils;
import vandy.mooc.view.SettingsActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Mediates communication between the Video Service and the local
 * storage on the Android device.  The methods in this class block, so
 * they should be called from a background thread (e.g., via an
 * AsyncTask).
 */
public class VideoDataMediator {
    /**
     * Status code to indicate that file is successfully
     * uploaded.
     */
    public static final String STATUS_UPLOAD_SUCCESSFUL =
        "Upload succeeded";
    public static final String STATUS_DOWNLOAD_SUCCESSFUL =
            "Download succeeded";
    /**
     * Status code to indicate that file upload failed 
     * due to large video size.
     */
    public static final String STATUS_UPLOAD_ERROR_FILE_TOO_LARGE =
        "Upload failed: File too big";
    
    /**
     * Status code to indicate that file upload failed.
     */
    public static final String STATUS_UPLOAD_ERROR =
        "Upload failed";
    public static final String STATUS_DOWNLOAD_ERROR =
            "Download failed";
    /**
     * Defines methods that communicate with the Video Service.
     */
    private VideoSvcApi mVideoServiceProxy;

    private Context mContext;
    
    /**
     * Constructor that initializes the VideoDataMediator.
     * 
     * @param context
     */
    public VideoDataMediator(Context context) {
    	
    	SharedPreferences prefs = 
    			PreferenceManager.getDefaultSharedPreferences(context);
    	
    	String serverProtocol = prefs
    			                .getString(SettingsActivity.KEY_PREFERENCE_PROTOCOL,
                                        "");
    	String serverIp = prefs
    			            .getString(SettingsActivity.KEY_PREFERENCE_IP_ADDRESS,
                                    "");
    	String serverPort = prefs
    			            .getString(SettingsActivity.KEY_PREFERENCE_PORT,
                                       "");
    	String userName = prefs
    			            .getString(SettingsActivity.KEY_PREFERENCE_USER_NAME,
                                       "");
    	String password = prefs
    			            .getString(SettingsActivity.KEY_PREFERENCE_PASSWORD,
                                       "");
    	
    	String serverUrl = serverProtocol
    			             + "://"
    			             + serverIp
    			             + ":"
    			             + serverPort ;
        Log.d("Inside Mediator", "this is server url "+serverUrl);

       serverUrl = "https://10.0.2.2:8443";

        mContext = context;
    	
        // Initialize the VideoServiceProxy.
        mVideoServiceProxy =
        		new SecuredRestBuilder()
    			.setLoginEndpoint(serverUrl + VideoSvcApi.TOKEN_PATH)
    			.setEndpoint(serverUrl)
    			.setUsername(userName)
    			.setPassword(password)
    			.setClientId(Constants.CLIENT_ID)
    			.setClient(new OkClient(UnsafeHttpsClient.getUnsafeOkHttpClient()))
    			.setLogLevel(LogLevel.FULL)
    			.build()
    			.create(VideoSvcApi.class);
        
    }
    
    

    /**
     * Uploads the Video having a URI.
     * 
     * @param  context
     *            URI
     *
     * @return A String indicating the status of the video upload operation.
     */
    public String uploadVideo(Context context,
                              Uri videoUri) {
        // Get the path of video file from videoUri.
        String filePath =
            VideoMediaStoreUtils.getPath(context,
                    videoUri);

        // Get the Video from Android Video Content Provider having
        // the given filePath.

        Video androidVideo = null;
        if (filePath != null)
            VideoMediaStoreUtils.getVideo(context, filePath);


        // Check if any such Video exists in Android Video Content
        // Provider.
        if (androidVideo != null) {
            // Prepare to Upload the Video data.

            // Create an instance of the file to upload.
            File videoFile = new File(filePath);

            // Check if the file size is less than the size of the
            // video that can be uploaded to the server.
            if (videoFile.length() < Constants.MAX_SIZE_MEGA_BYTE) {

                try {
                    // Add the metadata of the Video to the Video Service
                    // and get the resulting Video that contains
                    // additional meta-data (e.g., Id and ContentType)
                    // generated by the Video Service.
                    Video receivedVideo =
                        mVideoServiceProxy.addVideo(androidVideo);

                    // Check if the Server returns any Video metadata.
                    if (receivedVideo != null) {

                        // Finally, upload the Video data to the server
                        // and get the status of the uploaded video data.
                        VideoStatus status =
                            mVideoServiceProxy.setVideoData
                                (receivedVideo.getId(),
                                 new TypedFile("image/jpg", videoFile));

                        // Check if the Status of the Video or not.
                        if (status.getState() == VideoState.READY) {
                            // Video successfully uploaded.
                            return STATUS_UPLOAD_SUCCESSFUL;
                        }
                    }
                } catch (Exception e) {
                    // Error occured while uploading the video.
                    return STATUS_UPLOAD_ERROR;
                }
            } else
                // Video can't be uploaded due to large video size.
                return STATUS_UPLOAD_ERROR_FILE_TOO_LARGE;
        }
        
        // Error occured while uploading the video.
        return STATUS_UPLOAD_ERROR;
    }

    /**
     * Downloads the Video having the given Id.  This Id is the Id of
     * Video in Android Video Content Provider.
     *
     * @param videoId
     *            Id of the Video to be uploaded.
     *
     * @return A String indicating the status of the video upload operation.
     */
    public String downloadVideo(Context context, long videoId,
                                String videoName)  {

        Response response  =
                mVideoServiceProxy.getVideoData(videoId);

        // Check if the Video Service returned success.
        if (response.getStatus() == 200) {
            File file = VideoStorageUtils.storeVideoInExternalDirectory(context, response, videoName);

            // Video successfully downloaded .
            return STATUS_DOWNLOAD_SUCCESSFUL;
        } else
            // Video couldn't be downloaded.
            return STATUS_DOWNLOAD_ERROR;
    }
    /**
     * Get the List of Videos from Video Service.
     *
     * @return the List of Videos from Server or null if there is
     *         failure in getting the Videos.
     */
    public List<Video> getVideoList() {
        try {
            return (ArrayList<Video>)
                        mVideoServiceProxy.getVideoList();
        } catch (Exception e) {
           return null; 
        }
    }

    /**
     * Like a video.
     *
     * @return void.
     */
    public void likeVideo(long id) {
        mVideoServiceProxy.likeVideo(id,"", myCallback);
    }

    public void unlikeVideo(long id) {
        mVideoServiceProxy.unlikeVideo(id, "", myCallbackUnLike);
    }
    Callback myCallback = new Callback() {
        @Override
        public void success(Object o, Response response) {
            Log.d("******","Liked was called!");
            Utils.showToast(mContext, "Video liked!");
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.d("******","Liked was called FAILURE!!");
            Utils.showToast(mContext, "Video already liked!");
        }
    };

    Callback myCallbackUnLike = new Callback() {
        @Override
        public void success(Object o, Response response) {
            Log.d("******","Unlike was called!");
            Utils.showToast(mContext, "Video unliked!");
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.d("******","Unlike was called FAILURE!!");
            Utils.showToast(mContext, "Video cannot be unliked!");
        }
    };
}
