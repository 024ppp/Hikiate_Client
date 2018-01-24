package com.example.administrator.Hikiate_Client;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class SelectSagyo extends Activity implements View.OnClickListener {
    ListView lv;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sagyo);

        Intent intent = getIntent();
        String names = intent.getStringExtra("name");
        String[] members = names.split(",");

        //String[] members = { names, "相田みつを", "石原さとみ", "宇野けんたろう", "江川卓",
        //        "オリバー・カーン", "河北麻友子", "木村拓哉", "蔵座敷", "県民共済", "コイキング" };

        lv = (ListView) findViewById(R.id.list);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1, members);

        lv.setAdapter(adapter);

        //リスト項目がクリックされた時の処理
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);

                // 返すデータ(Intent&Bundle)の作成
                Intent data = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("key.StringData", item);
                //bundle.putInt("key.intData", 123456789);
                data.putExtras(bundle);

                // setResult() で bundle を載せた
                // 送るIntent dataをセットする

                // 第一引数は…Activity.RESULT_OK,
                // Activity.RESULT_CANCELED など
                setResult(RESULT_OK, data);

                // finish() で終わらせて
                // Intent data を送る
                finish();
            }
        });

        lv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //リスト項目が選択された時の処理
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), item + " selected",
                        Toast.LENGTH_LONG).show();
            }
            //リスト項目がなにも選択されていない時の処理
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(), "no item selected",
                        Toast.LENGTH_LONG).show();
            }
        });

        //リスト項目が長押しされた時の処理
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), item + " long clicked",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        });

        //画面On/Off対応
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        // ブロードキャストリスナー
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    if (action.equals(Intent.ACTION_SCREEN_ON)) {
                        // 画面ON時
                        Log.d(TAG, "SCREEN_ON");
                    } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                        // 画面OFF時
                        setCanceled();
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, filter);
    }

    //メイン画面に戻る
    private void setCanceled(){
        // 返すデータ(Intent&Bundle)の作成
        Intent data = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("key.StringData", "");
        data.putExtras(bundle);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onUserLeaveHint(){
        //ホームボタンが押された時や、他のアプリが起動した時に呼ばれる
        //戻るボタンが押された場合には呼ばれない
        //（注）ホームボタン押下時はアプリが終了できないため、無効化
        //setCanceled();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            setCanceled();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    //クリック処理の実装
    public void onClick(View v) {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // 登録したレシーバを解除する
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

}
