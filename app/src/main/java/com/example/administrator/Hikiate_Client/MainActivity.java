package com.example.administrator.Hikiate_Client;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar toolbar;
    TextView show, txtKeiryo;
    Button btnClear, btnUpd;
    EditText txtBcd;
    Handler handler;
    Runnable runnable;
    String ip;
    int myPort;
    // サーバと通信するスレッド
    ClientThread clientThread;
    NfcTags nfcTags = null;
    //インスタンス化無しで使える
    ProcessCommand pc;
    private static final int SETTING = 8888;
    //バイブ
    Vibrator vib;
    private long m_vibPattern_read[] = {0, 200};
    private long m_vibPattern_error[] = {0, 200, 200, 500};
    private String mVkon;
    //タイマーの間隔
    private long mDelay = 1000;
    //リスト用のデータを準備
    List<Data> mDataList = new ArrayList<>();
    private int mDisplayMode;
    private View mView;
    //設定値
    int mSettingValue;

    // 背景のレイアウト
    private LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ハンドラー
        handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                // サブスレッドからのメッセージ
                if (msg.what == 0x123) {
                    // 表示する
                    String sMsg = msg.obj.toString();
                    //show.append("\n PCから受信-" + sMsg);
                    selectMotionWhenReceiving(sMsg);
                }
            }
        };
        //接続先を取得
        SharedPreferences prefs = getSharedPreferences("ConnectionData", Context.MODE_PRIVATE);
        ip = prefs.getString("ip", "");
        myPort = prefs.getInt("myPort", 0);
        clientThread = new ClientThread(handler, ip, myPort, true);
        // サーバ接続スレッド開始
        new Thread(clientThread).start();

        //バイブ
        vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        // view取得
        setViews();
        //NFCタグ
        this.nfcTags = new NfcTags(this);

        //test
        setShowMessage(0);
    }

    private void changeMode(int mode, String info) {
        String title;
        int layoutId;

        mDisplayMode = mode;
        //
        switch (mode) {
            case 1:
                title = "排出";
                layoutId = R.layout.discharge;
                break;
            case 2:
                title = "比重";
                layoutId = R.layout.hijuu;
                break;
            case 3:
                title = "検量";
                layoutId = R.layout.kenryo;
                break;
            case 4:
                title = "保管";
                layoutId = R.layout.hokan;
                break;
            case 5:
                title = "缶クリア";
                layoutId = R.layout.clear;
                break;
            default:
                return;
        }

        //工程名表示
        toolbar.setTitle(title);
        // 変更したいレイアウトを取得する
        LinearLayout layout = (LinearLayout)findViewById(R.id.layout);
        mainLayout = (LinearLayout) findViewById(R.id.layout);
        // レイアウトのビューをすべて削除する
        layout.removeAllViews();
        // レイアウトを変更する
        getLayoutInflater().inflate(layoutId, layout);
        //変更後のレイアウトをいじるための準備
        View view = getLayoutInflater().inflate(layoutId, layout);
        mView = view;

        switch (mode) {
            case 1:
                //リスト内容更新
                displayGetListItem(info);
                setInfoToTextview(mVkon + "," + mDataList.size());
                show.setText("No.1の" + "缶タグをタッチしてください。");
                break;
            case 2:
                setShowMessage(88);
                break;
            case 3:
                txtKeiryo = (TextView) view.findViewById(R.id.txtKeiryo);
                txtBcd = (EditText) view.findViewById(R.id.txtIdo);
                addTCL();
                txtBcd.requestFocus();
                show.setText("移動票No.をスキャンしてください。");
                break;
            case 4:
                txtBcd = (EditText) view.findViewById(R.id.txtKokban);
                addTCL();
                txtBcd.requestFocus();
                show.setText("工管No.をスキャンしてください。");
                break;
            case 5:
                setShowMessage(88);
                break;
            default:
                return;
        }
    }

    //List表示(汎用)
    private void displayGetListItem(String info) {
        int listId;
        String[] infoset;
        String[] items;

        if (mDisplayMode == 1) { listId = R.id.listCan;}
        else if (mDisplayMode == 3) { listId = R.id.listKen;}
        else if (mDisplayMode == 4) { listId = R.id.listHok;}
        else { return;}

        ListView listView = (ListView) mView.findViewById(listId);

        //データ格納
        infoset = info.split("@");
        for (int i = 0; i < infoset.length; i++) {
            if (infoset[i].equals("")) {
                continue;
            }

            Data data = new Data();
            items = infoset[i].split(",");
            //格納処理
            data.setData(mDisplayMode, items);
            mDataList.add(data);
        }
        //----画面に出力
        ListAdapter adapter = new ListAdapter(this, mDataList, mDisplayMode);
        listView.setAdapter(adapter);
    }

    private void setInfoToTextview(String info) {
        TextView t0, t1, t2, t3, t4, t5, t6, t7;
        String[] items = info.split(",");

        switch (mDisplayMode) {
            case 1:
                t0 = (TextView)mView.findViewById(R.id.txtVko);
                t1 = (TextView)mView.findViewById(R.id.txtCankei);
                for (int i = 0; i < items.length; i++) {
                    if (i == 0) { t0.setText(items[i]); }
                    else if (i == 1) { t1.setText(items[i]); }
                }
                break;
            case 3:
                t0 = (TextView)mView.findViewById(R.id.txtZainmk);
                t1 = (TextView)mView.findViewById(R.id.txtCansuu);
                t2 = (TextView)mView.findViewById(R.id.txtJuryo);
                for (int i = 0; i < items.length; i++) {
                    //配合粉
                    if (i == 0) { t0.setText(items[i]); }
                    //缶数
                    else if (i == 1) {
                        t1.setText(items[i]);
                        //缶数をここで抽出しているので、便乗
                        String txt = "";
                        for (int j = 0; j < Integer.parseInt(items[i]); j++) {
                            txt += "@" + String.valueOf(j + 1);
                        }
                        displayGetListItem(txt);
                    }
                    //設定値
                    else if (i == 2) {
                        t2.setText(items[i]);
                        mSettingValue = Integer.valueOf(items[i]);
                    }
                }
                break;
            case 4:
                t0 = (TextView)mView.findViewById(R.id.txtHinban);
                t1 = (TextView)mView.findViewById(R.id.txtLotno);
                t2 = (TextView)mView.findViewById(R.id.txtHai1);
                t3 = (TextView)mView.findViewById(R.id.txtHai2);
                t4 = (TextView)mView.findViewById(R.id.txtHai3);
                t5 = (TextView)mView.findViewById(R.id.txtCan1);
                t6 = (TextView)mView.findViewById(R.id.txtCan2);
                t7 = (TextView)mView.findViewById(R.id.txtCan3);
                for (int i = 0; i < items.length; i++) {
                    if (i == 0) { t0.setText(items[i]); }
                    else if (i == 1) { t1.setText(items[i]); }
                    else if (i == 2) { t2.setText(items[i]); }
                    else if (i == 3) { t3.setText(items[i]); }
                    else if (i == 4) { t4.setText(items[i]); }
                    else if (i == 5) { t5.setText(items[i]); }
                    else if (i == 6) { t6.setText(items[i]); }
                    else if (i == 7) { t7.setText(items[i]); }
                }
                break;
        }
    }

    private void cantagScannedCheck(String can) {
        for (Data data : mDataList) {
            if (mDisplayMode == 4) {
                //缶タグが入ってる場合はスキップ
                if (!data.getCanTag().equals("")) {
                    //缶タグがスキャン済みの場合はアラート
                    if (data.getCanTag().equals(can)) {
                        data.setHantei("OK");
                        break;
                    }
                    continue;
                }
                break;
            }
            else {
                //缶タグが入ってる場合はスキップ
                if (!data.getCanTag().equals("")) {
                    //缶タグがスキャン済みの場合はアラート
                    if (data.getCanTag().equals(can)) {
                        show.setText("缶タグ(" + can + ")はスキャン済みです。");
                        //バイブ エラー
                        vib.vibrate(m_vibPattern_error, -1);
                        return;
                    }
                    continue;
                }
                data.setCanTag(can);
                break;
            }
        }

        // リストにデータを受け渡す
        int listId;
        if (mDisplayMode == 1) { listId = R.id.listCan;}
        else if (mDisplayMode == 3) { listId = R.id.listKen;}
        else if (mDisplayMode == 4) { listId = R.id.listHok;}
        else { return;}

        ListView listView = (ListView) findViewById(listId);
        ListAdapter adapter = new ListAdapter(this, mDataList, mDisplayMode);
        listView.setAdapter(adapter);

        //登録可能かチェックして、登録ボタンを有効化する
        confirmRegisterable();
    }

    //登録可能かどうかチェック。検量モード時のみ計量値取得モードへの移行
    private void confirmRegisterable() {
        int number = 0;
        for (Data data : mDataList) {
            if (mDisplayMode == 4) {
                if (data.getHantei().equals("")) {
                    show.setText("缶タグをタッチしてください。");
                    btnUpd.setEnabled(false);
                    return;
                }
            }
            else {
                number++;
                if (data.getCanTag().equals("")) {
                    show.setText("No." + String.valueOf(number) + "の缶タグをタッチしてください。");
                    btnUpd.setEnabled(false);
                    return;
                }
            }
        }
        if (mDisplayMode == 3) {
            //検量モード時は、計量値定期取得を実行する
            getMeasuringValueRegularly();
        }
        else {
            //登録可能状態にする
            setShowMessage(99);
        }
    }

    //受信した文字列のコマンド値によって分岐（switch文ではenum使えず...if文汚し）
    private void selectMotionWhenReceiving(String sMsg) {
        String cmd = pc.COMMAND_LENGTH.getCmdText(sMsg);
        String excmd  = pc.COMMAND_LENGTH.getExcludeCmdText(sMsg);

        if (cmd.equals(pc.SAG.getString())) {
            //作業者名をセット
            if (!excmd.equals("")) {
                setShowMessage(0);
            }
        }
        else if (cmd.equals(pc.VKO.getString())) {
            changeMode(1, excmd);
        }
        else if (cmd.equals(pc.IDO.getString())) {
            setInfoToTextview(excmd);
            setShowMessage(88);
        }
        else if (cmd.equals(pc.KOB.getString())) {
            String[] buf = excmd.split("#");
            setInfoToTextview(buf[0]);
            displayGetListItem(buf[1]);
            setShowMessage(88);
        }
        else if (cmd.equals(pc.DUP.getString())
                || cmd.equals(pc.HUP.getString())
                || cmd.equals(pc.KUP.getString())
                || cmd.equals(pc.SUP.getString())
                ) {
            MyToast.makeText(this, "登録完了しました。", Toast.LENGTH_SHORT, 32f).show();
            initPage();
        }
        else if (cmd.equals(pc.MSV.getString())) {
            checkMeasuringValue(excmd);
        }
        else if (cmd.equals(pc.MSG.getString())) {
            if (!excmd.equals("")) {
                show.setText(excmd);
            }
        }
        else if (cmd.equals(pc.ERR.getString())) {
            //バイブ エラー
            vib.vibrate(m_vibPattern_error, -1);
            show.setText(excmd);
            if (mDisplayMode == 3) {
                //検量モードで缶スキャンミスをした場合
                initListView();
            }
        }
    }

    //タグテキストのコマンド値によって分岐
    private void selectMotionTagText(String sMsg) {
        String cmd = pc.COMMAND_LENGTH.getCmdText(sMsg);
        String excmd  = pc.COMMAND_LENGTH.getExcludeCmdText(sMsg);

        if (cmd.equals(pc.VKO.getString())) {
            if (mDisplayMode == 0) {
                //TAGテキストをそのまま送信
                sendMsgToServer(sMsg);
                mVkon = excmd;
            }
        }
        else if (cmd.equals(pc.CAN.getString())) {
            if (mDisplayMode != 0) {
                if (mDisplayMode == 2) {
                    //比重
                    sendMsgToServer(pc.HUP.getString() + sMsg);
                }
                else if (mDisplayMode == 5) {
                    //缶クリア
                    sendMsgToServer(pc.CLR.getString() + sMsg);
                }
                else {
                    //その他
                    cantagScannedCheck(sMsg);
                }
            }
        }
        else if (cmd.equals(pc.OPE.getString())) {
            if (mDisplayMode == 0) {
                switch (excmd) {
                    case "HIJUU":
                        changeMode(2, "");
                        break;
                    case "KENRYO":
                        changeMode(3, "");
                        break;
                    case "HOKAN":
                        changeMode(4, "");
                        break;
                    case "CLEAR":
                        changeMode(5, "");
                        break;
                }
            }
        }
        else if (cmd.equals(pc.KOB.getString())) {
            //TAGテキストをそのまま送信
            sendMsgToServer(sMsg);
        }
        else {
            show.setText("タグテキストエラー！");
        }
    }

    //計量値取得を定期実行
    private void getMeasuringValueRegularly() {
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                //計量値取得コマンド
                sendMsgToServer(pc.MSV.getString());
                handler.postDelayed(this, mDelay);
            }
        };
        handler.post(r);
        runnable = r;
    }
    //取得した計量値を表示、設定値での判定を行う
    private void checkMeasuringValue(String info) {
        double value = Double.valueOf(info);
        double min = mSettingValue * 0.9;
        double max = mSettingValue * 1.1;

        //計量値を判定。よければ登録ボタン有効化
        if (min < value && value < max) {
            txtKeiryo.setBackground(ContextCompat.getDrawable(this, R.drawable.ok));
            setShowMessage(99);
        }
        else if (min > value) {
            txtKeiryo.setBackground(ContextCompat.getDrawable(this, R.drawable.low));
            btnUpd.setEnabled(false);
            show.setText("設定値以下です。");
        }
        else if (value > max) {
            txtKeiryo.setBackground(ContextCompat.getDrawable(this, R.drawable.high));
            btnUpd.setEnabled(false);
            show.setText("設定値以上です。");
        }
        txtKeiryo.setText(info);
    }

    private void initPage() {
        //登録ボタンを無効化
        btnUpd.setEnabled(false);
        setShowMessage(0);

        //リスト用データを初期化
        mDataList = new ArrayList<>();
        //PLC自動取得を解除
        if (mDisplayMode == 3) {
            handler.removeCallbacks(runnable);
        }
        //TOPに戻る
        mDisplayMode = 0;
        toolbar.setTitle("まとめ配合TOP");
        LinearLayout layout = (LinearLayout)findViewById(R.id.layout);
        layout.removeAllViews();
        getLayoutInflater().inflate(R.layout.nfcimag, layout);
    }

    private void initListView() {
        int listId;
        switch (mDisplayMode) {
            case 3:
                for (Data data : mDataList) {
                    data.setCanTag("");
                }
                listId = R.id.listKen;
                //PLC自動取得を解除
                handler.removeCallbacks(runnable);
                txtKeiryo.setText("");
                txtKeiryo.setBackground(ContextCompat.getDrawable(this, R.drawable.frame_item));
                btnUpd.setEnabled(false);
                show.setText(show.getText().toString() + "\nNo.1の缶タグをタッチしてください。");
                break;
            default:
                return;
        }
        // リストにデータを受け渡す
        ListView listView = (ListView) findViewById(listId);
        ListAdapter adapter = new ListAdapter(this, mDataList, mDisplayMode);
        listView.setAdapter(adapter);
    }

    @Override
    //クリック処理の実装
    public void onClick(View v) {
        if (v != null) {
            switch (v.getId()) {
                case R.id.btnUpd :
                    //Dialog(OK,Cancel Ver.)
                    new AlertDialog.Builder(this)
                            .setTitle("確認")
                            .setMessage("登録しますか？")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // OK button pressed
                                    switch (mDisplayMode) {
                                        case 1:
                                            sendMsgToServer(pc.DUP.getString() + createUpdtext());
                                            break;
                                        case 3:
                                            sendMsgToServer(pc.KUP.getString() + createUpdtext());
                                            break;
                                        case 4:
                                            sendMsgToServer(pc.SUP.getString() + createUpdtext());
                                            break;
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    break;

                case R.id.btnClear :
                    //Dialog(OK,Cancel Ver.)
                    new AlertDialog.Builder(this)
                            .setTitle("確認")
                            .setMessage("クリアしてよろしいですか？")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // OK button pressed
                                    initPage();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    break;

            }
        }
    }

    //登録ボタン押下時にサーバに送る更新値の生成
    private String createUpdtext() {
        String txt = "";
        switch (mDisplayMode) {
            case 1:
                for (Data data : mDataList) {
                    txt += "@";
                    txt += data.getCanTag() + ",";
                    txt += data.getVkonno() + ",";
                    txt += data.getMeiban();
                }
                break;
            case 3:
                for (Data data : mDataList) {
                    txt += "@";
                    txt += data.getCanTag() + ",";
                    txt += txtBcd.getText().toString() + ",";
                    txt += txtKeiryo.getText().toString();
                }
                break;
            case 4:
                for (Data data : mDataList) {
                    txt += "@";
                    txt += data.getCanTag();
                }
                break;
        }
        return txt;
    }

    @Override
    //タグを読み込んだ時に実行される
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String tagText = "";
        tagText = this.nfcTags.getTagText(intent);
        if (!tagText.equals("")) {
            selectMotionTagText(tagText);
        }
        //バイブ
        vib.vibrate(m_vibPattern_read, -1);
    }

    //サーバへメッセージを送信する
    private void sendMsgToServer(String sMsg) {
        try {
            // メッセージ送信
            Message msg = new Message();
            msg.what = 0x345;   //？
            msg.obj = sMsg;
            clientThread.revHandler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setShowMessage(int order) {
        switch (order) {
            case 0:
                show.setText("OPEタグをタッチしてください。");
                break;
            case 1:
                show.setText("機械Noをタッチしてください。");
                break;
            case 2:
                show.setText("タッチしてください。");
                break;
            case 3:
                show.setText("箱をタッチしてください。");
                break;
            case 88:
                show.setText("缶タグをタッチしてください。");
                break;
            case 99:
                show.setText("全てOKです。\n登録してください。");
                btnUpd.setEnabled(true);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SETTING:
                Toast.makeText(this, "設定が完了しました。", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    //TextView Listener
    private void addTCL() {
        //バーコードリーダー対応
        txtBcd.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
            }
            @Override
            public void afterTextChanged(Editable s) {
                //DEBUG
                //Log.d("test", "afterTextChanged");
                if (txtBcd.getText().length() >= 6) {
                    //工程管理Noが6文字以上になったら、工程管理番号問い合わせをサーバーに送信する
                    String cmd = "";
                    if (mDisplayMode == 3) { cmd = pc.IDO.getString();}
                    else if (mDisplayMode == 4) { cmd = pc.KOB.getString();}

                    sendMsgToServer(cmd + txtBcd.getText().toString());
                    //キーボードをしまう
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(txtBcd.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    //out focus
                    mainLayout.requestFocus();
                }
            }
        });
    }

    private void setViews() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("まとめ配合TOP");
        setSupportActionBar(toolbar);

        show = (TextView) findViewById(R.id.show);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnUpd = (Button) findViewById(R.id.btnUpd);
        //クリックイベント
        btnClear.setOnClickListener(this);
        btnUpd.setOnClickListener(this);
        //登録ボタンを無効化
        btnUpd.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PendingIntent pendingIntent = this.createPendingIntent();
        // Enable NFC adapter
        this.nfcTags.enable(this, pendingIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Disable NFC adapter
        this.nfcTags.disable(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.nfcTags = null;
        try {
            clientThread.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            //Dialog(OK,Cancel Ver.)
            new AlertDialog.Builder(this)
                    .setTitle("確認")
                    .setMessage("終了してもよろしいですか？")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // OK button pressed
                            finishAndRemoveTask();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //設定画面呼び出し
            Intent intent = new Intent(this, Setting.class);
            int requestCode = SETTING;
            startActivityForResult(intent, requestCode);
            return true;
        }
        else if (id == R.id.action_reconnection) {
            show.setText("再接続に失敗しました。\n無線LAN状況を確認してください。");
            //再接続を行う
            clientThread = new ClientThread(handler, ip, myPort, false);
            // サーバ接続スレッド開始
            new Thread(clientThread).start();
        }
        else if (id == R.id.action_finish) {
            //Dialog(OK,Cancel Ver.)
            new AlertDialog.Builder(this)
                    .setTitle("確認")
                    .setMessage("終了してもよろしいですか？")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // OK button pressed
                            finishAndRemoveTask();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }
}
