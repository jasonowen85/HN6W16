package com.adayo.service.sourcemngservice.Utils;

import com.adayo.proxy.share.ShareDataManager;

import org.json.JSONException;
import org.json.JSONObject;

public class SrcMngOperateShareInfo {
    private static final String TAG = SrcMngOperateShareInfo.class.getSimpleName();
    private int mShareInfoId;
    private String mKey;
    private Object mValue;

    /**
     * 构造函数
     * @param key
     * @param shareInfoId
     */
    public SrcMngOperateShareInfo(String key, int shareInfoId)
    {
        mKey = key;
        mShareInfoId = shareInfoId;
    }

    /**
     * 获取shareInfo中的值
     * @return
     */
    public Object getmValue()
    {
        final String s = ShareDataManager.getShareDataManager().getShareData(mShareInfoId);

        if (s == null)
        {
            return null;
        }

        JSONObject obj = null;
        try
        {
            obj = new JSONObject(s);
            if (obj != null && obj.has(mKey))
            {
                mValue = obj.get(mKey);
            }
        }catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }

        return mValue;
    }
}
