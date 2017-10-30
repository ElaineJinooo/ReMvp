package com.remvp.library.bean;

import java.io.Serializable;
import java.util.List;

public class BaseListResponse<T extends List> extends BaseResponse<T> implements Serializable {
    private String page;
    private String page_count;

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getPage_count() {
        return page_count;
    }

    public void setPage_count(String page_count) {
        this.page_count = page_count;
    }

    @Override
    public String toString() {
        return "BaseListResponse{" +
                "page='" + page + '\'' +
                ", page_count='" + page_count + '\'' +
                '}';
    }
}
