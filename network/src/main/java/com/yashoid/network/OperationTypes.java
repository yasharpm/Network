package com.yashoid.network;

import android.util.SparseArray;

import java.util.HashMap;

/**
 * Created by Yashar on 11/12/2017.
 */

public interface OperationTypes {

    int TYPE_URGENT = 0;
    int TYPE_UI_CONTENT = 1;
    int TYPE_USER_ACTION = 2;
    int TYPE_BACKGRUOND = 4;

    String SECTION_UI_CONTENT = "network_ui_content";
    String SECTION_USER_ACTION = "network_user_action";
    String SECTION_BACKGROUND = "network_background";

    int PRIORITY_DEFAULT = 0;
    int PRIORITY_HIGH = 1;
    int PRIORITY_LOW = -1;
    int PRIORITY_MAX = 2;
    int PRIORITY_MIN = -2;

}
