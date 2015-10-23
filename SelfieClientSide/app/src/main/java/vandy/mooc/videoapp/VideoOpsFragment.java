package vandy.mooc.videoapp;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class VideoOpsFragment extends Fragment {


    static VideoOpsFragment instance;
    public static final String TAG = "VideoOps";
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

    Set<Video> videos = new HashSet<Video>();
    WeakReference<MainActivity> mActivity;
    VideoSvcApi mVideoSvcApi;
    boolean loggedIn = false;

    public VideoOpsFragment() {
        super();
        instance = this;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);


    }

    void login(final String login, final String password) {
            new AsyncTask<Void, Void, VideoSvcApi>() {
                @Override
                protected VideoSvcApi doInBackground(Void... params) {
                    mVideoSvcApi = new SecuredRestBuilder()
                            .setLoginEndpoint(Constants.TEST_URL + VideoSvcApi.TOKEN_PATH)
                            .setUsername(login)
                            .setPassword(password)
                            .setClientId(Constants.CLIENT_ID)
                            .setClient(new OkClient(UnsafeHttpsClient.getUnsafeOkHttpClient()))
                            .setEndpoint(Constants.TEST_URL).setLogLevel(RestAdapter.LogLevel.FULL).build()
                            .create(VideoSvcApi.class);

                    Log.i(TAG, "mVideoSvcApi=" + mVideoSvcApi);
                    return null;
                }

                @Override
                protected void onPostExecute(VideoSvcApi videoSvcApi) {
                    getAvailableVideos();
                }
            }.execute();

    }


    public void likeVideo (final Video video) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    mVideoSvcApi.likeVideo(video.getId(), "");
                } catch (Exception e) {
                    toastError(e.getMessage());
                }


                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                getAvailableVideos();
            }

        }.execute();

    }

    public void unlikeVideo (final Video video) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    mVideoSvcApi.unlikeVideo(video.getId(), "");
                } catch (Exception e) {
                    toastError(e.getMessage());
                }


                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                getAvailableVideos();
            }

        }.execute();



    }


    public void addVideo (final Video video) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Video v = new Video();
                try {
                   v = mVideoSvcApi.addVideo(video);
                } catch (Exception e) {
                    toastError(e.getMessage());
                    Log.d("Ex from Add Video", e.getMessage());
                }


                return null;
            }
            /**
            @Override
            protected void onPostExecute(Void aVoid) {
                getAvailableVideos();
            }
            **/
        }.execute();






    }

    public void setVideoData (final long id, final TypedFile f) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    mVideoSvcApi.setVideoData(id, f);
                } catch (Exception e) {
                    toastError(e.getMessage());
                    Log.d("Exception&&&&&", e.getMessage());
                }


                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                getAvailableVideos();
            }
        }.execute();
    }

    public void getVideoData (final long id, final String videoName) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    Response response=  mVideoSvcApi.getVideoData(id);
                    // Check if the Video Service returned success.
                    if (response.getStatus() == 200) {
                        File file = VideoStorageUtils.storeVideoInExternalDirectory(mActivity.get().adapter.getContext(), response, videoName);

                        // Video successfully downloaded .
                        Log.d(TAG,STATUS_DOWNLOAD_SUCCESSFUL);
                    } else
                        // Video couldn't be downloaded.
                        Log.d(TAG,STATUS_DOWNLOAD_ERROR);
                } catch (Exception e) {
                    toastError(e.getMessage());
                    Log.d("Ex for getting data ",e.getMessage());
                }


                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                getAvailableVideos();
            }
        }.execute();




    }


    public void getAvailableVideos() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    videos.clear();
                    videos.addAll(mVideoSvcApi.getVideoList());
                } catch (Exception e) {
                    toastError(e.getMessage());
                }


                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updateAdapter();
            }
        }.execute();




    }

    private void updateAdapter() {
        mActivity.get().adapter.setNotifyOnChange(false);
        mActivity.get().adapter.clear();
        mActivity.get().adapter.addAll(videos);
        mActivity.get().adapter.notifyDataSetChanged();
        mActivity.get().adapter.getContext();

        mActivity.get().runOnUiThread(new Runnable() {
            @Override
            public void run() {


            }
        });


    }

    private void toastError(final String s) {
        mActivity.get().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity.get().getBaseContext(), s, Toast.LENGTH_SHORT).show();
            }
        });

    }






}
