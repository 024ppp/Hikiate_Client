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

    public ListAdapter(Context context, List<Data> objects) {
        super(context, 0, objects);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        List<TextView> textViews = new ArrayList<>();
        // layout/raw_XXX.xmlを紐付ける
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
