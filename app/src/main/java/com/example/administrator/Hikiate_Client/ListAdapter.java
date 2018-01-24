package com.example.administrator.Hikiate_Client;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

// リスト表示制御用クラス
class ListAdapter extends ArrayAdapter<Data> {
    private LayoutInflater inflater;
    // values/colors.xmlより設定値を取得するために利用。
    private Context mContext;
    private int mDisplayMode;

    public ListAdapter(Context context, List<Data> objects, int mode) {
        super(context, 0, objects);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mDisplayMode = mode;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        List<TextView> textViews = new ArrayList<>();
        // layout/XXX.xmlを紐付ける
        if (mDisplayMode == 1) {
            if (view == null) {
                view = inflater.inflate(R.layout.raw_discharge, parent, false);
            }
            final Data data = this.getItem(position);
            TextView tvData1 = (TextView) view.findViewById(R.id.raw1);
            TextView tvData2 = (TextView) view.findViewById(R.id.raw2);
            TextView tvData3 = (TextView) view.findViewById(R.id.raw3);
            textViews.add(tvData1);
            textViews.add(tvData2);
            textViews.add(tvData3);
            if (data != null) {
                //No.
                tvData1.setText(data.getNumber());
                //排出粉
                tvData2.setText(data.getZainmk());
                //缶タグ
                tvData3.setText(data.getCanTag());
            }
        }
        //kenryo
        else if (mDisplayMode == 3) {
            if (view == null) {
                view = inflater.inflate(R.layout.raw_kenryo, parent, false);
            }
            final Data data = this.getItem(position);
            TextView tvData1 = (TextView) view.findViewById(R.id.raw1);
            TextView tvData2 = (TextView) view.findViewById(R.id.raw2);
            textViews.add(tvData1);
            textViews.add(tvData2);
            if (data != null) {
                //No.
                tvData1.setText(data.getNumber());
                //缶タグ
                tvData2.setText(data.getCanTag());
            }
        }
        //hokan
        else if (mDisplayMode == 4) {
            if (view == null) {
                view = inflater.inflate(R.layout.raw_hokan, parent, false);
            }
            final Data data = this.getItem(position);
            TextView tvData1 = (TextView) view.findViewById(R.id.raw1);
            TextView tvData2 = (TextView) view.findViewById(R.id.raw2);
            TextView tvData3 = (TextView) view.findViewById(R.id.raw3);
            textViews.add(tvData1);
            textViews.add(tvData2);
            textViews.add(tvData3);
            if (data != null) {
                //No.
                tvData1.setText(data.getNumber());
                //排出粉
                tvData2.setText(data.getCanTag());
                //缶タグ
                tvData3.setText(data.getHantei());
            }
        }


        //偶数行の場合の背景色を設定
        if (position % 2 == 0) {
            for (TextView t : textViews) {
                t.setBackgroundColor(ContextCompat.getColor(mContext, R.color.data1));
            }
        }
        //奇数行の場合の背景色を設定
        else {
            for (TextView t : textViews) {
                t.setBackgroundColor(ContextCompat.getColor(mContext, R.color.data2));
            }
        }

        return view;
    }
}
