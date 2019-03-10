package be.kuleuven.softdev.haientang.newsclient;

import be.kuleuven.softdev.haientang.newsclient.model.NewsItem;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class NewsAdapter extends BaseAdapter implements OnScrollListener{

    private List<NewsItem> mList;
    private LayoutInflater mInflater;
    private ListView mListView;
    private ImageLoader imageLoader;

    private int mStart;
    private int mEnd;
    private boolean isFirstIn;

    public NewsAdapter(Context context,List<NewsItem> newsItems,ListView listView){
        mList=newsItems;
        mInflater=LayoutInflater.from(context);
        mListView=listView;
        isFirstIn = true;

        imageLoader =new ImageLoader(mListView);
        imageLoader.mUrls = new String[mList.size()]; //uUrls used to store image url
        for(int i=0;i<mList.size();i++){
            imageLoader.mUrls[i] = mList.get(i).image_URL;
        }
        mListView.setOnScrollListener(this);;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        //to see if it has been recycled or not
        if(convertView==null){
            //if not been recycled yet, call inflate function, and new tag will be set
            convertView=mInflater.inflate(R.layout.news_item,null);

            viewHolder=new ViewHolder();
            //cache the result of the findViewById function suing view holder
            viewHolder.ivImage=(ImageView) convertView.findViewById(R.id.image);
            viewHolder.ivLikes=(ImageView) convertView.findViewById(R.id.likesIcon);
            viewHolder.tvTitle=(TextView) convertView.findViewById(R.id.title);
            viewHolder.tvDate=(TextView) convertView.findViewById(R.id.date);
            viewHolder.tvlikes=(TextView) convertView.findViewById(R.id.likes);
            //store in a tag on the view
            convertView.setTag(viewHolder);//set tag
        }else{
            //if has been recycled, it already exsits and we don't need to call inflator function again---save time
            viewHolder=(ViewHolder) convertView.getTag(); //get the tag
        }

        viewHolder.ivImage.setTag(mList.get(position).image_URL);
        viewHolder.ivImage.setImageResource(R.drawable.loading);

        //show the images that has been loaded and stored
        imageLoader.showImage(viewHolder.ivImage,mList.get(position).image_URL);

        viewHolder.tvTitle.setText(mList.get(position).title);
        viewHolder.tvDate.setText(mList.get(position).date);
        viewHolder.tvlikes.setText(""+mList.get(position).likes);
        viewHolder.ivLikes.setImageResource(R.drawable.like);

        return convertView;
    }

    class ViewHolder{
        ImageView ivImage;
        TextView tvTitle;
        ImageView ivLikes;
        TextView tvDate;
        TextView tvlikes;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mStart=firstVisibleItem;
        mEnd=firstVisibleItem+visibleItemCount;

        if(isFirstIn&&visibleItemCount>0){
            imageLoader.loadImages(mStart,mEnd);
            isFirstIn=false;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        if(scrollState==SCROLL_STATE_IDLE){
            imageLoader.loadImages(mStart,mEnd);//when stop scrolling, start to load the picturea
        }else{
            imageLoader.cancelAllAsyncTask();
        }

    }
}
