package com.pkang.findtranslations.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;

import androidx.annotation.Nullable;

import com.pkang.findtranslations.R;

/**
 * Custom TextView that holds letters in the crossword puzzle. Holds required layout logic.
 */
public class CrosswordLetterView extends androidx.appcompat.widget.AppCompatTextView {
    public CrosswordLetterView(Context context) {
        super(context);
        setup();
    }

    public CrosswordLetterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public CrosswordLetterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    private void setup() {
        setTextColor(Color.WHITE);
        setBackground(getContext().getDrawable(R.drawable.crossword_letter_background));

        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.setMargins(2,2,2,2);
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            //if greater than Lollipop, let weights / layout gravity set the fill. Lollipop is handled in onMeasure
            layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED,GridLayout.FILL,1f);
            layoutParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED,GridLayout.FILL,1f);
            layoutParams.setGravity(Gravity.FILL);
            layoutParams.height = 0;
            layoutParams.width = 0;
        } else {
            layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            layoutParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            layoutParams.setGravity(Gravity.CENTER_VERTICAL);
        }
        setLayoutParams(layoutParams);
        setGravity(Gravity.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            //Lollipop needs a little help figuring out letter sizes/position
            CrosswordPuzzleLayout parent = (CrosswordPuzzleLayout) getParent();
            int rows = parent.getRowCount();
            int cols = parent.getColumnCount();

            int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
            int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

            int idealWidth = measureDimension(parentWidth / cols, widthMeasureSpec);
            int idealHeight = measureDimension(parentHeight / rows, heightMeasureSpec);

            idealWidth = View.resolveSize(idealWidth, widthMeasureSpec);
            idealHeight = View.resolveSize(idealHeight, heightMeasureSpec);

            setMeasuredDimension(measureDimension(idealWidth, widthMeasureSpec),
                    measureDimension(idealHeight, heightMeasureSpec));
        }
    }


    private int measureDimension(int desiredSize, int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = desiredSize;
        }
        return result;
    }
}
