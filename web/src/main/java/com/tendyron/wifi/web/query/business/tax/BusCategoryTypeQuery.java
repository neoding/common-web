package com.tendyron.wifi.web.query.business.tax;

import com.tendyron.wifi.web.query.BaseQuery;

/**
 * Created by Neo on 2017/5/9.
 * 税务业务类型分组
 */

public class BusCategoryTypeQuery extends BaseQuery {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
