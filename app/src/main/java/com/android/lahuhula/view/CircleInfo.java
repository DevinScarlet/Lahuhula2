package com.android.lahuhula.view;

import java.util.ArrayList;

/**
 * Created by lenovo on 2017/4/9.
 */

public class CircleInfo {
    private static final String TAG = "CircleInfo";

    public static final String VC_ID = "vc_id";
    public static final String VC_OWNER = "vc_ower";
    public static final String VC_ITEM_NO = "vc_itemno";
    public static final String VC_ITEM_NAME = "vc_itemname";
    public static final String VC_UNIT = "vc_unit";
    public static final String D_QUANITITY = "d_quantity";
    public static final String D_PRICE = "d_price";
    public static final String VC_DESCRIBE = "vc_describe";
    public static final String VC_LIKE = "vc_thumbs";
    public static final String VC_COMMENT = "vc_comment";
    public static final String DT_OPERDATE = "dt_operdate";
    public static final String VC_PIC = "vc_pic";
    public static final String VC_PIC1 = "vc_pic1";
    public static final String VC_PIC2 = "vc_pic2";
    public static final String VC_PIC3 = "vc_pic3";
    public static final String VC_PIC4 = "vc_pic4";
    public static final String VC_PIC5 = "vc_pic5";
    public static final String VC_PIC6 = "vc_pic6";
    public static final String VC_PIC7 = "vc_pic7";
    public static final String VC_PIC8 = "vc_pic8";
    public static final String VC_PIC9 = "vc_pic9";

    public String mId;
    public String mOwner;
    public String mItemNo;
    public String mItemName;
    public String mUnit;
    public String mQuantity;
    public String mPrice;
    public String mDescribe;
    public String mLike;
    public String mComment;
    public String mOperdate;
    public ArrayList<String> mPictureList = new ArrayList<String>();

    public CircleInfo() {
    }

    public CircleInfo(String id, String owner, String itemNo, String itemName, String unit, String quantity, String price,
                      String describe, String like, String comment, String date, ArrayList<String> list) {
        mId = id;
        mOwner = owner;
        mItemNo = itemNo;
        mItemName = itemName;
        mUnit = unit;
        mQuantity = quantity;
        mPrice = price;
        mDescribe = describe;
        mLike = like;
        mComment = comment;
        mOperdate = date;
        mPictureList.clear();
        if (list != null && list.size() > 0) {
            mPictureList.addAll(list);
        }
    }

    @Override
    public String toString() {
        return "CircleInfo{" +
                "mId='" + mId + '\'' +
                ", mOwner='" + mOwner + '\'' +
                ", mItemNo='" + mItemNo + '\'' +
                ", mItemName='" + mItemName + '\'' +
                ", mUnit='" + mUnit + '\'' +
                ", mQuantity='" + mQuantity + '\'' +
                ", mPrice='" + mPrice + '\'' +
                ", mDescribe='" + mDescribe + '\'' +
                ", mLike='" + mLike + '\'' +
                ", mComment='" + mComment + '\'' +
                ", mOperdate='" + mOperdate + '\'' +
                ", mPictureList=" + mPictureList +
                '}';
    }
}
