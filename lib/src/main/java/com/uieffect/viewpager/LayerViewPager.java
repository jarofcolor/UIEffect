package com.uieffect.viewpager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class LayerViewPager extends ViewGroup {

    public static final int MAX_LIMIT = 3;
    public static final int START_POSITION = Integer.MAX_VALUE / 2;
    private CustomViewPager mViewPager;
    private LayerAdapter mLayerAdapter;
    private List<ItemInfo> mItemInfos;
    private LayerPagerAdapter mLayerPagerAdapter;
    private boolean isAutoScroll;

    private MainHandler mMainHandler;
    private boolean isScrolled;

    public LayerViewPager(Context context) {
        this(context, null);
    }

    public LayerViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LayerViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setUp();
    }

    private void setUp() {
        mItemInfos = Collections.synchronizedList(new ArrayList<ItemInfo>());


        mViewPager = new CustomViewPager(getContext(), mItemInfos);
        addView(mViewPager);

        mViewPager.setPageTransformer(true, new LayerPageTransformer(mItemInfos));
        mViewPager.setAdapter(mLayerPagerAdapter = new LayerPagerAdapter(this, mItemInfos));
        mViewPager.setOffscreenPageLimit(MAX_LIMIT);

        mViewPager.setCurrentItem(START_POSITION);
        mMainHandler = new MainHandler(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() != 1) throw new RuntimeException("The child count must be 1");


        mViewPager.measure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mViewPager.layout(0, 0, mViewPager.getMeasuredWidth(), mViewPager.getMeasuredHeight());
    }

    public void setLayerAdapter(LayerAdapter layerAdapter) {
        this.mLayerAdapter = layerAdapter;
        mLayerAdapter.setLayerViewPager(this);
        notifyDataSetChanged();
        sendAutoScrollMsg();
    }

    public LayerAdapter getLayerAdapter() {
        return mLayerAdapter;
    }


    private static class ItemInfo {
        int tag;
        View view;
        float position;

        public ItemInfo(int tag, View view) {
            this.tag = tag;
            this.view = view;
        }
    }

    private static class CustomViewPager extends ViewPager {
        List<ItemInfo> itemInfos;

        public CustomViewPager(Context context, List<ItemInfo> itemInfos) {
            super(context);
            this.itemInfos = itemInfos;
            setChildrenDrawingOrderEnabled(true);
        }

        @Override
        protected int getChildDrawingOrder(int childCount, int i) {
            List<View> tidyViews = getTidyViews();
            View orderView = tidyViews.get(i);
            return indexOfChild(orderView);
        }

        @Override
        protected void onPageScrolled(int position, float offset, int offsetPixels) {
            super.onPageScrolled(position, offset, offsetPixels);
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
//            super.dispatchDraw(canvas);

            final long drawingTime = getDrawingTime();
            List<View> tidyViews = getTidyViews();
            for (View view : tidyViews) {
                if (view.getVisibility() == VISIBLE)
                    drawChild(canvas, view, drawingTime);
            }
        }

        private List<View> getTidyViews() {
            int childCount = itemInfos.size();
            List<ItemInfo> list = new ArrayList<>(itemInfos);
            Comparator<ItemInfo> comparator = new Comparator<ItemInfo>() {
                @Override
                public int compare(ItemInfo o1, ItemInfo o2) {
                    return Math.abs(o1.position) - Math.abs(o2.position) > 0 ? -1 : 1;
                }
            };
            Collections.sort(list, comparator);
            List<View> views = new ArrayList<>(childCount);
            for (ItemInfo info : list) {
                views.add(info.view);
            }
            return views;
        }
    }

    private static class LayerPageTransformer implements ViewPager.PageTransformer {
        private final static float MAX_SCALE = 0.73f;
        List<ItemInfo> itemInfos;

        public LayerPageTransformer(List<ItemInfo> itemInfos) {
            this.itemInfos = itemInfos;
        }

        @Override
        public void transformPage(View view, float position) {
            float absPosition = Math.abs(position);

            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();
            view.setPivotX(pageWidth / 2);
            view.setPivotY(pageHeight / 2);
            int maxValue = MAX_LIMIT * 2 + 1;
            float targetFactor = (maxValue - absPosition) / (maxValue * 1.0f) * MAX_SCALE;
            view.setScaleX(targetFactor);
            view.setScaleY(targetFactor);

            int base = position < 0 ? -1 : 1;
            float translationX = position * pageWidth - (float) (base * (-0.5 * Math.pow(absPosition, 2) + 7.5 * absPosition) * MAX_SCALE * dpToPx(180) / 14);

            view.setTranslationX(-translationX);
            view.setVisibility(absPosition >= MAX_LIMIT ? INVISIBLE : VISIBLE);

            view.setAlpha(Math.abs(MAX_LIMIT - absPosition));

            for (ItemInfo info : itemInfos) {
                if (info.view == view) {
                    info.position = position;
                }
            }
        }

        private float dpToPx(float dp) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
        }
    }


    private static class LayerPagerAdapter extends PagerAdapter {

        LayerViewPager layerViewPager;
        List<ItemInfo> itemInfos;


        public LayerPagerAdapter(LayerViewPager layerViewPager, List<ItemInfo> itemInfos) {
            this.layerViewPager = layerViewPager;
            this.itemInfos = itemInfos;
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @NotNull
        @Override
        public Object instantiateItem(@NotNull ViewGroup container, int position) {
            LayerAdapter layerAdapter = layerViewPager.getLayerAdapter();
            if (layerAdapter != null && layerAdapter.getCount() > 0) {
                int realPosition = position % layerAdapter.getCount();
                View view = layerAdapter.getView(container, realPosition);
                itemInfos.add(new ItemInfo(position, view));
                return view;
            }else {
                throw new RuntimeException("call #instantiateItem() result not null");
            }
        }

        @Override
        public void destroyItem(@NotNull ViewGroup container, int position, Object object) {
            LayerAdapter layerAdapter = layerViewPager.getLayerAdapter();
            if (layerAdapter != null) {
                layerAdapter.destroyView(container, position, (View) object);
                ItemInfo targetInfo = null;
                for (ItemInfo info : itemInfos) {
                    if (info.tag == position) {
                        targetInfo = info;
                        break;
                    }
                }
                if (targetInfo != null) {
                    itemInfos.remove(targetInfo);
                }
            }
        }
    }

    private void notifyDataSetChanged() {
        mLayerPagerAdapter.notifyDataSetChanged();
        sendAutoScrollMsg();
    }

    public static abstract class LayerAdapter {
        private LayerViewPager layerViewPager;

        public abstract int getCount();

        public abstract View getView(ViewGroup container, int position);

        public void destroyView(ViewGroup container, int position, View view) {
            container.removeView(view);
        }

        public final void notifyDataSetChanged() {
            layerViewPager.notifyDataSetChanged();
        }

        void setLayerViewPager(LayerViewPager layerViewPager) {
            this.layerViewPager = layerViewPager;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean isScrolled = true;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_UP:
                isScrolled = false;
                break;
        }
        if (this.isScrolled != isScrolled) {
            this.isScrolled = isScrolled;
            sendAutoScrollMsg();
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setAutoScroll(boolean autoScroll) {
        if (isAutoScroll == autoScroll) return;
        isAutoScroll = autoScroll;
        sendAutoScrollMsg();
    }

    private void sendAutoScrollMsg() {
        mMainHandler.removeMessages(MainHandler.MSG_SCROLL);
        if (isAutoScroll && !isScrolled && mLayerAdapter != null && mLayerAdapter.getCount() != 0) {
            Message message = Message.obtain();
            message.what = MainHandler.MSG_SCROLL;
            mMainHandler.sendMessageDelayed(message, MainHandler.DURATION);
        }
    }

    public void setCurrentItem(int currentItem) {
        mViewPager.setCurrentItem(currentItem);
    }

    public int getCurrentItem() {
        return mViewPager.getCurrentItem();
    }

    private static class MainHandler extends Handler {
        final static int MSG_SCROLL = 0x11;
        final static int DURATION = 5000;
        WeakReference<LayerViewPager> viewPager;

        public MainHandler(LayerViewPager viewPager) {
            this.viewPager = new WeakReference<>(viewPager);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SCROLL:
                    LayerViewPager realViewPager = viewPager.get();
                    if (realViewPager == null) return;
                    realViewPager.setCurrentItem(realViewPager.getCurrentItem() + 1);
                    realViewPager.sendAutoScrollMsg();
                    break;
            }
        }
    }

}
