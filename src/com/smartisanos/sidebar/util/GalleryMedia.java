package com.smartisanos.sidebar.util;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * copied from GallerySmartisan ...
 */
public class GalleryMedia {
    public static class Media implements BaseColumns {
        public static final int MEDIA_TYPE_NONE = 0;
        public static final int MEDIA_TYPE_IMAGE = 1;
        public static final int MEDIA_TYPE_VIDEO = 3;

        public static final String AUTHORITY = "smartisanos_gallery";

        /**
         * id for file bucket
         */
        public static final String BUCKET_ID = "bucket_id";

        /**
         * for file path
         */
        public static final String DATA = "_data";

        /**
         * for file date taken
         */
        public static final String DATE_TAKEN = "datetaken";

        /**
         * for file media type, image or video
         */
        public static final String MEDIA_TYPE = "media_type";

        /**
         * for file status, delete or not
         */
        public static final String STATUS = "status";

        public static final int MEDIA_STATUS_NONE = 0;
        public static final int MEDIA_STATUS_DELETE = 1;

        /**
         * for panorama image
         */
        public static final String PANORAMA = "panorama";

        /**
         * for file date taken week
         */
        public static final String WEEK = "week";

        /**
         * for file date taken month
         */
        public static final String MONTH = "month";

        /**
         * for star
         */
        public static final String STAR = "star";

        public static final int STAR_NONE = 0;
        public static final int STAR_STAR = 1;

        public static final String DATE_ADDED = "date_added";

        /**
         * for camera_type
         */
        public static final String CAMERA_TYPE = "camera_type";

        public static final int CAMERA_TYPE_NONE = 0;
        public static final int CAMERA_TYPE_FRONT = 1;
        public static final int CAMERA_TYPE_REAR = 2;

        /**
         * for video_type
         */
        @Deprecated
        public static final String VIDEO_TYPE = "video_type";
        @Deprecated
        public static final int VIDEO_TYPE_NONE = 0;

        public static final String FRAME_RATE = "frame_rate";
        public static final int FRAME_RATE_NONE = -1;

        @Deprecated
        public static final String VIDEO_CODING = "video_coding";
    }

    public static final class Files extends Media {
        /**
         * Files table name
         */
        public static final String TABLE_NAME = "files";

        /**
         * Files content uri
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/files");

        /**
         * temp delete files
         */
        public static final Uri DELETE_URI = Uri.parse("content://" + AUTHORITY + "/delete_files");

        /**
         * files exclude hidden bucket files
         */
        public static final Uri OPEN_URI = Uri.parse("content://" + AUTHORITY + "/open_files");

        public static final Uri FULL_CONTINUOUS_URI = Uri.parse("content://" + AUTHORITY + "/full_continuous_files");
    }

    public static final class Bucket implements BaseColumns {
        /**
         * Bucket table name
         */
        public static final String TABLE_NAME = "bucket";

        /**
         * Bucket content uri
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + Media.AUTHORITY + "/bucket");

        /**
         * for bucket name
         */
        public static final String DISPLAY_NAME = "_display_name";

        /**
         * for bucket full path
         */
        public static final String DATA = "_data";

        /**
         * for image file count
         */
        public static final String IMAGE_COUNT = "image_count";

        /**
         * for video file count
         */
        public static final String VIDEO_COUNT = "video_count";

        /**
         * for bucket cover(image or video);
         */
        public static final String COVER_ID = "cover_id";

        /**
         * for bucket cover date taken
         */
        public static final String DATE_TAKEN = "datetaken";

        /**
         * for bucket status, scan, hide etc.
         */
        public static final String STATUS = "status";

        public static final int STATUS_NONE = 0;
        public static final int STATUS_PENDING_SCAN = 1;
        public static final int STATUS_USER_HIDDEN = 2;
        public static final int STATUS_USER_SHOWN = 3;
        public static final int STATUS_CLOUD_HIDDEN = 4;
        public static final int STATUS_CLOUD_SHOWN = 5;

        /**
         * for bucket deleting state.
         */
        public static final String DELETING = "deleting";

        /**
         * for bucket cloud synchronize switch
         * user can decide this bucket images synchronize or not.
         */
        public static final String CLOUD_STATE = "cloud_state";
        public static final int CLOUD_STATE_DEFAULT = 0;
        public static final int CLOUD_STATE_CLOUD_OPEN = 1;
        public static final int CLOUD_STATE_CLOUD_CLOSE= 2;
        public static final int CLOUD_STATE_USER_OPEN = 3;
        public static final int CLOUD_STATE_USER_CLOSE = 4;

        public static final String CLOUD_SYNC = "cloud_sync";
        public static final int CLOUD_SYNC_DEFAULT = 0;
        public static final int CLOUD_SYNC_COMPLETE = 1;
    }

    /**
     * single value set by cloud gallery account and switch icon.
     */
    public static final class CloudSwitch {
        public static final Uri CONTENT_URI = Uri.parse("content://" + Media.AUTHORITY + "/cloud_switch");

        public static final String TABLE_NAME = "cloud_switch";
        public static final String SWITCH = "switch";

        public static final int CLOSE = 0;
        public static final int OPEN = 1;
    }

    public static final class ContinuousShoot implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + Media.AUTHORITY + "/continuous_shoot");

        public static final String TABLE_NAME = "continuous_shoot";

        /**
         * media id of image which saved in DCIM/Camera
         */
        public static final String MID_KEY = "mid_key";

        /**
         * media ids of images which saved in hidden folder and indexed by mid_key
         */
        public static final String MID_VALUE = "mid_value";
    }
}
