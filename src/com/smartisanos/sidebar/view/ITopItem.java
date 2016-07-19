package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.util.anim.AnimTimeLine;

public interface ITopItem {
    AnimTimeLine highlight();
    AnimTimeLine dim();
    AnimTimeLine resume();
}
