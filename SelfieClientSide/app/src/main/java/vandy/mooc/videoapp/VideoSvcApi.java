package vandy.mooc.videoapp;

import java.util.Collection;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Streaming;
import retrofit.mime.TypedFile;

public interface VideoSvcApi {


    public class VideoStatus {

        public enum VideoState {
            READY, PROCESSING
        }

        private VideoState state;

        public VideoStatus(VideoState state) {
            super();
            this.state = state;
        }


    }


    String TOKEN_PATH = "/oauth/token";

    String VIDEO_SVC_PATH = "/video";

    String VIDEO_DATA_PATH = "/video/id/data";

    @GET(VIDEO_SVC_PATH)
    Collection<Video> getVideoList();

    @GET(VIDEO_SVC_PATH + "/{id}")
    Video getVideoById(@Path("id") long id);

    @POST(VIDEO_SVC_PATH)
    Video addVideo(@Body Video v);

    @POST(VIDEO_SVC_PATH + "/{id}/like")
    Void likeVideo(@Path("id") long id, @Body String body);

    @POST(VIDEO_SVC_PATH + "/{id}/unlike")
    Void unlikeVideo(@Path("id") long id, @Body String body);

    @GET(VIDEO_SVC_PATH + "/{id}/liked")
    Collection<String> getUsersWhoLikedVideo(@Path("id") long id);

    @Multipart
    @POST(VIDEO_DATA_PATH)
    VideoStatus setVideoData(@Path("id") long id, @Part("data") TypedFile videoData);
}
