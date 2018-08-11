package ru.androidtools.transitions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SourceActivity extends Activity {
  private ArrayList<Integer> photoList = new ArrayList<>();
  private RecyclerView recyclerView;
  private int exitPosition;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_source);

    recyclerView = findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    getPhotos();
    recyclerView.setAdapter(new RecyclerAdapter(photoList, new RecyclerAdapter.ClickListener() {
      @Override public void onItemClick(ImageView image) {
        Intent intent = new Intent(SourceActivity.this, DestinationActivity.class);
        intent.putExtra("current", (int) image.getTag());
        intent.putIntegerArrayListExtra("list", photoList);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          ActivityOptions options =
              ActivityOptions.makeSceneTransitionAnimation(SourceActivity.this, image,
                  image.getTransitionName());
          startActivity(intent, options.toBundle());
        } else {
          startActivity(intent);
        }
      }
    }));
  }

  //Test data
  private void getPhotos() {
    photoList.add(R.drawable.img0);
    photoList.add(R.drawable.img1);
    photoList.add(R.drawable.img2);
    photoList.add(R.drawable.img3);
    photoList.add(R.drawable.img4);
    photoList.add(R.drawable.img5);
    photoList.add(R.drawable.img6);
    photoList.add(R.drawable.img7);
    photoList.add(R.drawable.img8);
    photoList.add(R.drawable.img9);
    photoList.add(R.drawable.img10);
    photoList.add(R.drawable.img11);
    photoList.add(R.drawable.img12);
    photoList.add(R.drawable.img13);
  }

  public void onActivityReenter(int resultCode, Intent data) {
    super.onActivityReenter(resultCode, data);
    if (resultCode == RESULT_OK && data != null) {
      exitPosition = data.getIntExtra("exit_position", 0);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        View viewAtPosition = layoutManager.findViewByPosition(exitPosition);
        // Прокрутка к позиции, если вьюха для текущей позиции не null (т.е.
        // не является частью дочерних элементов layout-менеджера) или если
        // она видна не полностью.
        if (viewAtPosition == null || layoutManager.isViewPartiallyVisible(viewAtPosition, false,
            true)) {
          // layoutManager.scrollToPosition(exitPosition);
          if (BuildConfig.DEBUG) {
            Log.d("onActivityReenter", " карточка не видна,скролим");
          }
          layoutManager.scrollToPosition(exitPosition);
          setTransitionOnView();
        }
        // карточка видна, нужно поставить колбек
        else {
          setTransitionOnView();
        }
      }
    }
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP) private static class CustomSharedElementCallback
      extends SharedElementCallback {
    private View mView;

    /**
     * Set the transtion View to the callback, this should be called before starting the transition so the View is not null
     */
    public void setView(View view) {
      mView = view;
    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
      // Clear all current shared views and names
      names.clear();
      sharedElements.clear();

      if (mView != null) {
        // костыль NullPointerException: at android.support.v4.view.ViewCompat.getTransitionName
        // Store new selected view and name
        String transitionName = ViewCompat.getTransitionName(mView);
        if (BuildConfig.DEBUG) {
          Log.d("onActivityReenter",
              "CustomSharedElementCallback transitionName = " + transitionName);
        }
        names.add(transitionName);
        sharedElements.put(transitionName, mView);
      }
    }
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP) private void setTransitionOnView() {
    final CustomSharedElementCallback callback = new CustomSharedElementCallback();
    if (BuildConfig.DEBUG) {
      Log.d("onActivityReenter", "setTransitionOnView setExitSharedElementCallback");
    }
    setExitSharedElementCallback(callback);

    // Listen for the transition end and clear all registered callback

    getWindow().getSharedElementExitTransition().addListener(new Transition.TransitionListener() {
      @Override public void onTransitionStart(Transition transition) {
      }

      @Override public void onTransitionPause(Transition transition) {
      }

      @Override public void onTransitionResume(Transition transition) {
      }

      @Override public void onTransitionEnd(Transition transition) {
        if (BuildConfig.DEBUG) {
          Log.d("onActivityReenter", "onTransitionEnd");
        }
        removeCallback();
      }

      @Override public void onTransitionCancel(Transition transition) {
        if (BuildConfig.DEBUG) {
          Log.d("onActivityReenter", "onTransitionCancel");
        }
        removeCallback();
      }

      private void removeCallback() {
        if (BuildConfig.DEBUG) {
          Log.d("onActivityReenter", "setTransitionOnView removeCallback");
        }
        getWindow().getSharedElementExitTransition().removeListener(this);
        setExitSharedElementCallback(null);
      }
    });

    // Pause transition until the selected view is fully drawn
    postponeEnterTransition();

    // Listen for the RecyclerView pre draw to make sure the selected view is visible,
    //  and findViewHolderForAdapterPosition will return a non null ViewHolder
    recyclerView.getViewTreeObserver()
        .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
          @Override public boolean onPreDraw() {
            recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

            RecyclerView.ViewHolder holder =
                recyclerView.findViewHolderForAdapterPosition(exitPosition);
            if (holder instanceof RecyclerAdapter.MyViewHolder) {
              if (BuildConfig.DEBUG) {
                Log.d("onActivityReenter", "setTransitionOnView callback.setView");
              }
              callback.setView(((RecyclerAdapter.MyViewHolder) holder).image);
            }
            // если список задач для картинок пустой
            //if (iconThread.mRequestMap.size() == 0)
            // Continue the transition
            //{
            startPostponedEnterTransition();
            //} else
            // слушаем  колбек из потока и стартуем анимацию
            //{
            //  listenThreadEmptyMessage = true;
            //}
            if (BuildConfig.DEBUG) {
              Log.d("onActivityReenter",
                  "setTransitionOnView startPostponedEnterTransition  iconThread=");
            }
            return true;
          }
        });
  }
}
