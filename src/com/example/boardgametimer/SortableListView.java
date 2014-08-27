package com.example.boardgametimer;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.boardgametimer.Player;

public class SortableListView extends ListView {

    private final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 15;
    private final int MOVE_DURATION = 150;
    private final int LINE_THICKNESS = 15;
    
    public ArrayList<Player> playerList;
    
    private int lastEventY = -1;
    
    private int downY = -1;
    private int downX = -1;
    
    private int totalOffset = 0;
    
    private boolean cellIsMobile = false;
    private boolean isMobileScrolling = false;
    private int smoothScrollAmountAtEdge = 0;
    
    private final int INVALID_ID = -1;
    private long aboveItemId = INVALID_ID;
    private long mobileItemId = INVALID_ID;
    private long belowItemId = INVALID_ID;
    
    private BitmapDrawable hoverCell;
    private Rect hoverCellCurrentBounds;
    private Rect hoverCellOriginaBounds;
    
    private final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;
    
    private boolean isWaitingForScrollFinish = false;
    private int scrollState = OnScrollListener.SCROLL_STATE_IDLE;
    
    private boolean itemClicked;
    
    public SortableListView(Context context) {
        super(context);
        init(context);
    }

    public SortableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SortableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    
    public void init(Context context) {
        setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
                if(!itemClicked) {
                    totalOffset = 0;
                    
                    //I don't really know if this is needed, might be because of scrolling..?
                    int position = pointToPosition(downX, downY);
                    int itemNum = position - getFirstVisiblePosition();
                    
                    View selectedView = getChildAt(itemNum);
                    mobileItemId = getAdapter().getItemId(position);
                    hoverCell = getAndAddHoverView(selectedView);
                    selectedView.setVisibility(INVISIBLE);
                    
                    cellIsMobile = true;
                    
                    updateNeighbourViewsForId(mobileItemId);
                    
                    return true;
                }
                itemClicked = false;
                return false;
            }
        });
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
                itemClicked = true;
                parent.showContextMenuForChild(v);
            }
        });
        setOnScrollListener(scrollListener);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        smoothScrollAmountAtEdge = (int)(SMOOTH_SCROLL_AMOUNT_AT_EDGE / metrics.density);
    }
    
    private BitmapDrawable getAndAddHoverView(View v) {
        int w = v.getWidth();
        int h = v.getHeight();
        int top = v.getTop();
        int left = v.getLeft();
        
        Bitmap bmp = getBitmapWithBorder(v);
        
        BitmapDrawable drawable = new BitmapDrawable(getResources(), bmp);
        
        hoverCellOriginaBounds = new Rect(left, top, left + w, top + h);
        hoverCellCurrentBounds = new Rect(hoverCellOriginaBounds);
        
        drawable.setBounds(hoverCellCurrentBounds);
        
        return drawable;
    }
    
    private Bitmap getBitmapWithBorder(View v) {
        Bitmap bitmap = getBitmapFromView(v);
        Canvas canvas = new Canvas(bitmap);
        
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(LINE_THICKNESS);
        paint.setColor(Color.BLACK);
        
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawRect(rect, paint);
        
        return bitmap;
    }
    
    private Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }
    
    private void updateNeighbourViewsForId(long itemId) {
        int position = getPositionForId(itemId);
        PlayerArrayAdapter adapter = ((PlayerArrayAdapter)getAdapter());
        aboveItemId = adapter.getItemId(position - 1);
        belowItemId = adapter.getItemId(position + 1);
    }
    
    public View getViewForId(long itemId) {
        int firstVisiblePosition = getFirstVisiblePosition();
        PlayerArrayAdapter adapter = ((PlayerArrayAdapter)getAdapter());
        for(int i=0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            int position = firstVisiblePosition + i;
            long id = adapter.getItemId(position);
            if (id == itemId) {
                return v;
            }
        }
        return null;
    }
    
    public int getPositionForId (long itemId) {
        View v = getViewForId(itemId);
        if (v == null) {
            return -1;
        }
        else {
            return getPositionForView(v);
        }
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (hoverCell != null) {
            hoverCell.draw(canvas);
        }
    }
    
    @Override
    public boolean onTouchEvent (MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                downX = (int)event.getX();
                downY = (int)event.getY();
                activePointerId = event.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                if (activePointerId == INVALID_POINTER_ID) {
                    break;
                }
                
                int pointerIndex = event.findPointerIndex(activePointerId);
                lastEventY = (int) event.getY(pointerIndex);
                int deltaY = lastEventY - downY;
                
                if(cellIsMobile) {
                    hoverCellCurrentBounds.offsetTo(hoverCellOriginaBounds.left, hoverCellOriginaBounds.top + deltaY + totalOffset);
                    hoverCell.setBounds(hoverCellCurrentBounds);
                    invalidate();
                    
                    handleCellSwitch();
                    
                    isMobileScrolling = false;
                    handleMobileCellScroll();
                    
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                touchEventsEnded();
                break;
            case MotionEvent.ACTION_CANCEL:
                touchEventsCancelled();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    touchEventsEnded();
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }
    
    private void handleCellSwitch() {
        final int deltaY = lastEventY - downY;
        int deltaYTotal = hoverCellOriginaBounds.top + totalOffset + deltaY;

        View belowView = getViewForId(belowItemId);
        View mobileView = getViewForId(mobileItemId);
        View aboveView = getViewForId(aboveItemId);
        
        boolean isBelow = (belowView != null) && (deltaYTotal > belowView.getTop());
        boolean isAbove = (aboveView != null) && (deltaYTotal < aboveView.getTop());
        
        
        if (isBelow || isAbove) {
            final long switchItemId = isBelow ? belowItemId : aboveItemId;
            
            View switchView = isBelow ? belowView : aboveView;
            final int originalItem = getPositionForView(mobileView);

            if (switchView == null) {
                updateNeighbourViewsForId(mobileItemId);
                return;
            }
            
            swapElements(playerList, originalItem, getPositionForView(switchView));
            
            ((BaseAdapter)getAdapter()).notifyDataSetChanged();
            
            downY = lastEventY;
            
            final int switchViewStartTop = switchView.getTop();
            
            mobileView.setVisibility(View.INVISIBLE);
            switchView.setVisibility(View.VISIBLE);
            
            updateNeighbourViewsForId(mobileItemId);
            
            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                
                @Override
                public boolean onPreDraw() {
                    observer.removeOnPreDrawListener(this);
                    View switchView = getViewForId(switchItemId);
                    totalOffset += deltaY;
                    
                    int switchViewNewTop = switchView.getTop();
                    int delta = switchViewStartTop - switchViewNewTop;
                    
                    switchView.setTranslationY(delta);
                    
                    ObjectAnimator animator = ObjectAnimator.ofFloat(switchView, View.TRANSLATION_Y, 0);
                    animator.setDuration(MOVE_DURATION);
                    animator.start();
                    
                    return true;
                }
            });
        }
    }
    
    private void swapElements(ArrayList<Player> list, int indexOne, int indexTwo) {
        Player temp = list.get(indexOne);
        list.set(indexOne, list.get(indexTwo));
        list.set(indexTwo, temp);
    }
    
    private void touchEventsEnded() {
        final View mobileView = getViewForId(mobileItemId);
        if (cellIsMobile || isWaitingForScrollFinish) {
            cellIsMobile = false;
            isWaitingForScrollFinish = false;
            isMobileScrolling = false;
            activePointerId = INVALID_POINTER_ID;
        
        
            if (scrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                isWaitingForScrollFinish = true;
                return;
            }
            
            hoverCellCurrentBounds.offsetTo(hoverCellOriginaBounds.left, mobileView.getTop());
            ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(hoverCell, "bounds", boundEvaluator, hoverCellCurrentBounds);
            hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidate();
                }
            });
            hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setEnabled(false);
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    aboveItemId = INVALID_ID;
                    mobileItemId = INVALID_ID;
                    belowItemId = INVALID_ID;
                    mobileView.setVisibility(VISIBLE);
                    hoverCell = null;
                    setEnabled(true);
                    invalidate();
                }
            });
            hoverViewAnimator.start();
        } else {
            touchEventsCancelled();
        }
    }
    
    private void touchEventsCancelled() {
        View mobileView = getViewForId(mobileItemId);
        if(cellIsMobile) {
            aboveItemId = INVALID_ID;
            mobileItemId = INVALID_ID;
            belowItemId = INVALID_ID;
            mobileView.setVisibility(VISIBLE);
            hoverCell = null;
            invalidate();
        }
        cellIsMobile = false;
        isMobileScrolling = false;
        activePointerId = INVALID_POINTER_ID;
    }
    
    
    private final static TypeEvaluator<Rect> boundEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction),
                            interpolate(startValue.top, endValue.top, fraction),
                            interpolate(startValue.right, endValue.right, fraction),
                            interpolate(startValue.bottom, endValue.bottom, fraction));
        }
        public int interpolate(int start, int end, float fraction) {
            return (int)(start + fraction * (end - start));
        }
    };
    
    private void handleMobileCellScroll() {
        isMobileScrolling = handleMobileCellScroll(hoverCellCurrentBounds);
    }
    
    public boolean handleMobileCellScroll(Rect r) {
        int offset = computeVerticalScrollOffset();
        int height = getHeight();
        int extent = computeVerticalScrollExtent();
        int range = computeVerticalScrollRange();
        int hoverViewTop = r.top;
        int hoverHeight = r.height();
        
        if (hoverViewTop <= 0 && offset > 0) {
            smoothScrollBy(-smoothScrollAmountAtEdge, 0);
            return true;
        }
        
        if (hoverViewTop + hoverHeight >= height && (offset + extent) < range) {
            smoothScrollBy(smoothScrollAmountAtEdge, 0);
            return true;
        }
        return false;
    }
    
    public void setPlayerList(ArrayList<Player> list) {
        playerList = list;
    }
    
    private AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {
        
        private int previousFirstVisibleItem = -1;
        private int previousVisibleItemCount = -1;
        private int currentFirstVisibleItem;
        private int currentVisibleItemCount;
        private int currentScrollState;
        
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            currentScrollState = scrollState;
            isScrollCompleted();
            
        }
        
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                int visibleItemCount, int totalItemCount) {
            currentFirstVisibleItem = firstVisibleItem;
            currentVisibleItemCount = visibleItemCount;
            
            previousFirstVisibleItem = (previousFirstVisibleItem == -1) ? currentFirstVisibleItem : previousFirstVisibleItem;
            previousVisibleItemCount = (previousVisibleItemCount == -1) ? currentVisibleItemCount : previousVisibleItemCount;
            
            checkAndHandleFirstVisibleCellChange();
            checkAndHandleLastVisibleCellChange();
            
            previousFirstVisibleItem = currentFirstVisibleItem;
            previousVisibleItemCount = currentVisibleItemCount;
            
        }
        
        private void isScrollCompleted() {
            if (currentVisibleItemCount > 0 && currentScrollState == SCROLL_STATE_IDLE) {
                if (cellIsMobile && isMobileScrolling) {
                    handleMobileCellScroll();
                }
                else if (isWaitingForScrollFinish) {
                    touchEventsEnded();
                }
            }
        }
        
        public void checkAndHandleFirstVisibleCellChange() {
            if (currentFirstVisibleItem != previousFirstVisibleItem) {
                if (cellIsMobile && mobileItemId != INVALID_ID) {
                    updateNeighbourViewsForId(mobileItemId);
                    handleCellSwitch();
                }
            }
        }
        
        public void checkAndHandleLastVisibleCellChange() {
            int currentLastVisibleItem = currentFirstVisibleItem + currentVisibleItemCount;
            int previousLastVisibleItem = previousFirstVisibleItem + previousVisibleItemCount;
            if (currentLastVisibleItem != previousLastVisibleItem) {
                if(cellIsMobile && mobileItemId != INVALID_ID) {
                    updateNeighbourViewsForId(mobileItemId);
                    handleCellSwitch();
                }
            }
        }
    };

}
