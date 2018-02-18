package com.example.user.dailytv.ListData;

import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by user on 2018-01-09.
 */

public class MarkerClusterItem implements ClusterItem {

    private LatLng latlng;
    private String title;
    private Drawable imageDrawable_cluster;
    private Drawable imageDrawable_listview;


    public String type;
    public String nickname;
    public String viwernumber;
    public String publisherid;
    public String videourl;
    public String longdate;

    public String getLongdate() {
        return longdate;
    }

    public void setLongdate(String longdate) {
        this.longdate = longdate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getViwernumber() {
        return viwernumber;
    }

    public void setViwernumber(String viwernumber) {
        this.viwernumber = viwernumber;
    }

    public String getPublisherid() {
        return publisherid;
    }

    public void setPublisherid(String publisherid) {
        this.publisherid = publisherid;
    }

    public String getVideourl() {
        return videourl;
    }

    public void setVideourl(String videourl) {
        this.videourl = videourl;
    }

    public MarkerClusterItem(LatLng latlng, String title, Drawable imageDrawable_cluster)
    {
        this.latlng=latlng;
        this.title=title;
        this.imageDrawable_cluster=imageDrawable_cluster;

    }

    public LatLng getLatlng() {
        return latlng;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Drawable getImageDrawable_cluster() {
        return imageDrawable_cluster;
    }

    public void setImageDrawable_cluster(Drawable imageDrawable_cluster) {
        this.imageDrawable_cluster = imageDrawable_cluster;
    }

    public Drawable getImageDrawable_listview() {
        return imageDrawable_listview;
    }

    public void setImageDrawable_listview(Drawable imageDrawable_listview) {
        this.imageDrawable_listview = imageDrawable_listview;
    }

    @Override
    public LatLng getPosition() {
        return latlng;
    }
}
