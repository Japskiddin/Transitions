package ru.androidtools.transitions;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;

public class PagerAdapter extends android.support.v4.view.PagerAdapter {
  interface PagerClickListener {
    void setStartPostTransition(View view);
  }

  private List<Integer> photoList;
  private Context mContext;
  private PagerClickListener listener;
  private int current;

  PagerAdapter(Context context, List<Integer> photoList, int current, PagerClickListener listener) {
    mContext = context;
    this.photoList = new ArrayList<>(photoList);
    this.listener = listener;
    this.current = current;
  }

  @Override @NonNull public Object instantiateItem(@NonNull ViewGroup collection, int position) {
    int photo = photoList.get(position);
    LayoutInflater inflater = LayoutInflater.from(mContext);
    View v = inflater.inflate(R.layout.item, collection, false);
    ImageView img = v.findViewById(R.id.image);
    img.setImageResource(photo);

    collection.addView(v);

    String name = mContext.getString(R.string.transition_name, position);
    img.setTag(position);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      img.setTransitionName(name);
      if (position == current) {
        listener.setStartPostTransition(img);
      }
    }

    return v;
  }

  @Override
  public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
    collection.removeView((View) view);
  }

  @Override public int getCount() {
    return photoList.size();
  }

  @Override public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
    return view == object;
  }
}