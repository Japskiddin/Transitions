package ru.androidtools.transitions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewTreeObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DestinationActivity extends Activity implements PagerAdapter.PagerClickListener {
  private int current;
  private List<Integer> photoList;
  private ViewPager viewPager;
  private PagerAdapter pagerAdapter;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      postponeEnterTransition();
    }

    setContentView(R.layout.activity_destination);

    if (getIntent().getExtras() != null) {
      current = getIntent().getIntExtra("current", 0);
      photoList = new ArrayList<>(getIntent().getIntegerArrayListExtra("list"));
    }

    viewPager = findViewById(R.id.viewpager);
    pagerAdapter = new PagerAdapter(this, photoList, current, this);
    viewPager.setAdapter(pagerAdapter);
    viewPager.setCurrentItem(current);
  }

  @TargetApi(21) @Override public void setStartPostTransition(final View view) {
    view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
      @Override public boolean onPreDraw() {
        view.getViewTreeObserver().removeOnPreDrawListener(this);
        startPostponedEnterTransition();
        return false;
      }
    });
  }

  @Override public void finishAfterTransition() {
    int pos = viewPager.getCurrentItem();
    Intent intent = new Intent();
    intent.putExtra("exit_position", pos);
    setResult(RESULT_OK, intent);
    if (current != pos) {
      View view = viewPager.findViewWithTag(pos);
      setSharedElementCallback(view);
    }
    super.finishAfterTransition();
  }

  @TargetApi(21) private void setSharedElementCallback(final View view) {
    setEnterSharedElementCallback(new SharedElementCallback() {
      @Override
      public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
        names.clear();
        sharedElements.clear();
        names.add(view.getTransitionName());
        sharedElements.put(view.getTransitionName(), view);
      }
    });
  }
}
