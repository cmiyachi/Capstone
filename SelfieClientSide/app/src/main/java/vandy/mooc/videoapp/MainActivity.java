package vandy.mooc.videoapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit.mime.TypedFile;

import static vandy.mooc.videoapp.VideoOpsFragment.*;



public class MainActivity extends ListActivity {
    VideoOpsFragment mVideoOpsFragment;
    private int selectedListItem = -1;
    ArrayAdapter<Video> adapter;

    private String mCurrentPhotoPath;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private String mCurrentSelfieName;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final long INTERVAL_TWO_MINUTES = 2 * 60 * 1000L;
    private static long selfie_no = 0;
    private static final String selfieName = "mySelfie";

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
        // startActivity(new Intent(this, NewVideoActivity.class));
        dispatchTakePictureIntent();
    }

    public void downloadButton(View view) {
        if (selectedListItem == -1) {
            Toast.makeText(this, "No video selected", Toast.LENGTH_SHORT).show();
        } else {

            Video video = (Video) getListAdapter().getItem(selectedListItem);
            mVideoOpsFragment.getVideoData(video.getId(), video.getName());


        }
    }


    public void unlikeButton(View view) {
        if (selectedListItem == -1)
        {Toast.makeText(this, "No video selected" , Toast.LENGTH_SHORT).show();}
        else {
            Video video = (Video) getListAdapter().getItem(selectedListItem);
          mVideoOpsFragment.unlikeVideo(video);
        }
    }
    /*
    private File createImageFile() throws IOException {
        // Create an image file name
        mCurrentSelfieName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File imageFile = File.createTempFile(
                mCurrentSelfieName,
                ".jpg",
                getExternalFilesDir(null));

        mCurrentPhotoPath = "file:" + imageFile.getAbsolutePath();
        return imageFile;
    } **/

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
       // mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        mCurrentPhotoPath = image.getCanonicalPath();
        Log.d(LOG_TAG,mCurrentPhotoPath + "************************************");
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }
            catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("ERROR:", ex.getMessage());
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG,"*******************Inside results");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Rename temporary file as yyyyMMdd_HHmmss.jpg
            File photoFile = new File(mCurrentPhotoPath);
         //   File selfieFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), mCurrentSelfieName + ".jpg");
          //  photoFile.renameTo(selfieFile);

            Log.d(LOG_TAG, "*******************************path to file " + photoFile.getPath() + "***" + photoFile.getName());


            selfie_no++;
            String self_no = Long.toString(selfie_no);
            Video v = new Video(selfieName+self_no,0, photoFile.getPath());
            v.setId(selfie_no);
           // mVideoOpsFragment.addVideo(v);
            mVideoOpsFragment.setVideoData(v.getId(), new TypedFile("image/jpg", photoFile));

        }
        else {
            File photoFile = new File(mCurrentPhotoPath);
            photoFile.delete();
        }
    }


}





