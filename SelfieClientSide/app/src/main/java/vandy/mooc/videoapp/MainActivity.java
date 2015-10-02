package vandy.mooc.videoapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;


import java.lang.ref.WeakReference;

import static vandy.mooc.videoapp.VideoOpsFragment.*;


public class MainActivity extends ListActivity {
    VideoOpsFragment mVideoOpsFragment;
    private int selectedListItem = -1;
    ArrayAdapter<Video> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_layout);
        mVideoOpsFragment = getOps();
        mVideoOpsFragment.mActivity = new WeakReference<MainActivity>(this);

        adapter = new ArrayAdapter<>(this, R.layout.rowlayout,
                R.id.text_title);
        setListAdapter(adapter);
        startActivity(new Intent(this, LoginActivity.class));


    }


    public void onLoginButtonClicked (View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    VideoOpsFragment getOps() {
        VideoOpsFragment result;
        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        result = (VideoOpsFragment) fm.findFragmentByTag("data");

        // create the fragment and data the first time
        if (result  == null) {
            // add the fragment
            result  = new VideoOpsFragment ();
            fm.beginTransaction().add(result , "data").commit();
            // load the data from the web

        }

        return result;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        selectedListItem = position;
    }




    public void likeButton(View view) {
        if (selectedListItem == -1)
        {Toast.makeText(this, "No video selected" , Toast.LENGTH_SHORT).show();}
        else {

            Video video = (Video) getListAdapter().getItem(selectedListItem);
            mVideoOpsFragment.likeVideo(video);


        }
    }



    public void uploadButton(View view) {
        startActivity(new Intent(this, NewVideoActivity.class));
    }


    public void unlikeButton(View view) {
        if (selectedListItem == -1)
        {Toast.makeText(this, "No video selected" , Toast.LENGTH_SHORT).show();}
        else {
            Video video = (Video) getListAdapter().getItem(selectedListItem);
          mVideoOpsFragment.unlikeVideo(video);
        }
    }
}





