package solution.androiddeveloper.com.medialisting.recyclerListView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import solution.androiddeveloper.com.medialisting.MediaFileInfo;
import solution.androiddeveloper.com.medialisting.R;

/**
 * Created by Mukesh on 12/20/2015.
 */
public class MediaRVAdapter extends RecyclerView.Adapter<MediaListRowHolder> {


    private List<MediaFileInfo> itemList;

    private Context mContext;

    public MediaRVAdapter(Context context, List<MediaFileInfo> itemList) {
        this.itemList = itemList;
        this.mContext = context;
    }

    @Override
    public MediaListRowHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, null);
        MediaListRowHolder mh = new MediaListRowHolder(v);

        return mh;
    }

    @Override
    public void onBindViewHolder(MediaListRowHolder mediaListRowHolder, int i) {
        try{
            MediaFileInfo item = itemList.get(i);
            mediaListRowHolder.title.setText(Html.fromHtml(item.getFileName()));
            Uri uri = Uri.fromFile(new File(item.getFilePath()));
            if(item.getFileType().equalsIgnoreCase("video")) {
                Bitmap bmThumbnail = ThumbnailUtils.
                        extractThumbnail(ThumbnailUtils.createVideoThumbnail(item.getFilePath(),
                                MediaStore.Video.Thumbnails.MINI_KIND), 80, 50);
                if(bmThumbnail != null) {
                    mediaListRowHolder.thumbnail.setImageBitmap(bmThumbnail);
                }
            } else if (item.getFileType().equalsIgnoreCase("audio")) {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(item.getFilePath());
                try{
                    if (mmr != null) {
                        byte[] art = mmr.getEmbeddedPicture();
                        Bitmap bmp = BitmapFactory.decodeByteArray(art,0,art.length);
                        if(bmp != null) {
                            bmp= ThumbnailUtils.extractThumbnail(bmp,80,50);
                            mediaListRowHolder.thumbnail.setImageBitmap(bmp);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else {
                Picasso.with(mContext).load(uri)
                        .error(R.drawable.placeholder)
                        .placeholder(R.drawable.placeholder)
                        .centerCrop()
                        .resize(80, 50)
                        .into(mediaListRowHolder.thumbnail);

            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }
}