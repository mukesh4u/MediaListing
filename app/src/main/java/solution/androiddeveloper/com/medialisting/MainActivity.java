package solution.androiddeveloper.com.medialisting;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import solution.androiddeveloper.com.medialisting.recyclerListView.MediaRVAdapter;

/**
 * Created by Mukesh on 12/20/2015.
 */

public class MainActivity extends AppCompatActivity {

    /**
     * Cursor used to access the results from querying for images on the SD card.
     */
    private Cursor cursor;
    /*
     * Column index for the Thumbnails Image IDs.
     */
    private int columnIndex;
    private static final String TAG = "RecyclerViewExample";

    private List<MediaFileInfo> mediaList = new ArrayList<MediaFileInfo>();

    private RecyclerView mRecyclerView;

    private MediaRVAdapter adapter;

    String type = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         /* Allow activity to show indeterminate progressbar */
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });



        /* Initialize recyclerview */
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        type = "images";
        new MediaAsyncTask().execute(type);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        ArrayList<MediaFileInfo> mediaList = new ArrayList<MediaFileInfo>();
        //noinspection SimplifiableIfStatement
        if (id == R.id.video) {
            String name = null;
            type = "video";
            new MediaAsyncTask().execute(type);
            return true;
        } else if (id == R.id.audio) {
            type = "audio";
            new MediaAsyncTask().execute(type);
            return true;
        } else if (id == R.id.image) {
            type = "images";
            new MediaAsyncTask().execute(type);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void parseAllVideo(String type) {
        try {
            String name = null;
            String[] thumbColumns = {MediaStore.Video.Thumbnails.DATA,
                    MediaStore.Video.Thumbnails.VIDEO_ID};

            int video_column_index;
            String[] proj = {MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.SIZE};
            Cursor videocursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    proj, null, null, null);
            int count = videocursor.getCount();
            Log.d("No of video", "" + count);
            for (int i = 0; i < count; i++) {
                MediaFileInfo mediaFileInfo = new MediaFileInfo();
                video_column_index = videocursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                videocursor.moveToPosition(i);
                name = videocursor.getString(video_column_index);

                mediaFileInfo.setFileName(name);

                int column_index = videocursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                videocursor.moveToPosition(i);
                String filepath = videocursor.getString(column_index);

                mediaFileInfo.setFilePath(filepath);
                mediaFileInfo.setFileType(type);
                mediaList.add(mediaFileInfo);
                // id += " Size(KB):" +
                // videocursor.getString(video_column_index);


            }
            videocursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseAllImages(String type) {
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, // Which columns to return
                    null,       // Return all rows
                    null,
                    null);


            int size = cursor.getCount();

            /*******  If size is 0, there are no images on the SD Card. *****/

            if (size == 0) {

            } else {

                int thumbID = 0;
                while (cursor.moveToNext()) {

                    int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                    /**************** Captured image details ************/

                    /*****  Used to show image on view in LoadImagesFromSDCard class ******/
                    String path = cursor.getString(file_ColumnIndex);

                    String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());

                    MediaFileInfo mediaFileInfo = new MediaFileInfo();
                    mediaFileInfo.setFilePath(path);
                    mediaFileInfo.setFileName(fileName);
                    mediaFileInfo.setFileType(type);
                    mediaList.add(mediaFileInfo);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseAllAudio(String type) {
        try {
            String TAG = "Audio";
            Cursor cur = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    null);

            if (cur == null) {
                // Query failed...
                Log.e(TAG, "Failed to retrieve music: cursor is null :-(");

            }
            else if (!cur.moveToFirst()) {
                // Nothing to query. There is no music on the device. How boring.
                Log.e(TAG, "Failed to move cursor to first row (no query results).");

            }else {
                Log.i(TAG, "Listing...");
                // retrieve the indices of the columns where the ID, title, etc. of the song are

                // add each song to mItems
                do {
                    int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                    int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
                    int albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                    int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
                    int idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);
                    int filePathIndex = cur.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    Log.i(TAG, "Title column index: " + String.valueOf(titleColumn));
                    Log.i(TAG, "ID column index: " + String.valueOf(titleColumn));

                    Log.i("Final ", "ID: " + cur.getString(idColumn) + " Title: " + cur.getString(titleColumn) + "Path: " + cur.getString(filePathIndex));
                    MediaFileInfo audio = new MediaFileInfo();
                    audio.setFileName(cur.getString(titleColumn));
                    audio.setFilePath(cur.getString(filePathIndex));
                    audio.setFileType(type);
                    mediaList.add(audio);

                } while (cur.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MediaAsyncTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            //setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            String type = params[0];
            try {
                mediaList = new ArrayList<MediaFileInfo>();
                if(type.equalsIgnoreCase("video")) {
                    parseAllVideo(type);
                    result =1;
                } else if(type.equalsIgnoreCase("audio")) {
                    parseAllAudio(type);
                    result =1;
                } else if(type.equalsIgnoreCase("images")) {
                    parseAllImages(type);
                    result =1;
                }
            }catch (Exception e) {
                e.printStackTrace();
                result =0;
            }

            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {

           // setProgressBarIndeterminateVisibility(false);

            /* Download complete. Lets update UI */
            if (result == 1) {
                adapter = new MediaRVAdapter(MainActivity.this, mediaList);
                mRecyclerView.setAdapter(adapter);
            } else {
                Log.e(TAG, "Failed to fetch data!");
            }
        }
    }

}
