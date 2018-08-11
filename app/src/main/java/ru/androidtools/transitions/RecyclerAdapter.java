package ru.androidtools.transitions;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
  interface ClickListener {
    void onItemClick(ImageView image);
  }

  private ClickListener listener;
  private List<Integer> photoList;

  RecyclerAdapter(List<Integer> photoList, ClickListener listener) {
    this.photoList = new ArrayList<>(photoList);
    this.listener = listener;
  }

  @NonNull @Override
  public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_view, parent, false);
    return new MyViewHolder(view);
  }

  @Override public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
    final int photo = photoList.get(position);
    holder.image.setImageResource(photo);
    holder.image.setTag(position);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      holder.image.setTransitionName(
          holder.image.getContext().getString(R.string.transition_name, position));
    }
    holder.image.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        listener.onItemClick(holder.image);
      }
    });
  }

  @Override public int getItemCount() {
    return photoList.size();
  }

  static class MyViewHolder extends RecyclerView.ViewHolder {
    ImageView image;

    MyViewHolder(View v) {
      super(v);
      image = v.findViewById(R.id.image);
    }
  }
}
