package com.aliessa.movieapp.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by Ali Essa on 6/11/2017
 */

public class WidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetRemoteViewsFactory(this.getApplicationContext());
    }
}
