package vandy.mooc.videoapp;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.mime.TypedFile;

public class VideoOpsFragment extends Fragment {


    static VideoOpsFragment instance;
    public static final String TAG = "VideoOps";


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
                    Log.d("Exception&&&&&",e.getMessage());
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
