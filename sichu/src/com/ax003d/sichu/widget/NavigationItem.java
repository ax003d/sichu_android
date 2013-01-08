
package com.ax003d.sichu.widget;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.ax003d.sichu.R;

public class NavigationItem extends LinearLayout {
    private final TextView label;
    private final View selectionHandler;

    public NavigationItem(Context context) {
        this(context, null);
    }

    public NavigationItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.inflate(context, R.layout.navigation_item, this, true);
        selectionHandler = findViewById(R.id.selectionHandler);
        label = (TextView) findViewById(android.R.id.text1);
    }

    public void setLabel(CharSequence label) {
        this.label.setText(label);
    }

    public void setLabel(int resId) {
        setLabel(getResources().getText(resId));
    }

    public void setSelectionHandlerColor(int color) {
        selectionHandler.setBackgroundColor(color);
    }

    public void setSelectionHandlerColorResource(int resId) {
        setSelectionHandlerColor(getResources().getColor(resId));
    }

    public void setSelectionHandlerVisiblity(int visiblity) {
        selectionHandler.setVisibility(visiblity);
    }
}
