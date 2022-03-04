package com.normurodov_nazar.adminformath.MyD;

import androidx.annotation.IdRes;

public class DrawerItem {
    @IdRes final int titleId;
    @IdRes final int imageId;


    /**
     *
     * @param title string resource id
     * @param id image resource id
     */
    public DrawerItem(int title, int id) {
        this.titleId = title;
        this.imageId = id;
    }

    public int getTitleId() {
        return titleId;
    }

    public int getImageId() {
        return imageId;
    }
}
