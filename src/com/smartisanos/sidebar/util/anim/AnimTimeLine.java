package com.smartisanos.sidebar.util.anim;

import com.smartisanos.sidebar.util.LOG;

public class AnimTimeLine {
    private static final LOG log = LOG.getInstance(AnimTimeLine.class);
    private static final boolean DBG_ANIM = true;
    static {
        if (!DBG_ANIM) {log.close();}
    }


}