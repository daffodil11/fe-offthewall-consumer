package com.slick.offthewall;

import java.util.Date;

public class Art {

    private final String url;
    private final String blurb;
    private final int artistId;
    private final Date createdAt;

    public Art (String url, String blurb, int artistId, Date createdAt) {
        this.url = url;
        this.blurb = blurb;
        this.artistId = artistId;
        this.createdAt = createdAt;
    }
}
