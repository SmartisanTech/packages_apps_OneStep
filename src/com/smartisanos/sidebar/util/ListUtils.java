package com.smartisanos.sidebar.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListUtils {
    public static <T> List<T> getDistinctList (List<T> list, Comparator<? super T> comparator) {
        if(list == null || list.size() <= 1){
            return list;
        }
        Collections.sort(list, comparator);
        for(int i = 1; i < list.size(); ++ i){
            if(comparator.compare(list.get(i - 1), list.get(i)) == 0){
                list.remove(i);
                i --;
            }
        }
        return list;
    }
}
