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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar toolbar;
    TextView show;
    Button btnClear, btnUpd;
    EditText txtBcd, txtHinban, txtLotno, txtCarbon;
    String m_cbncod;
    Handler handler;
    String ip;
    int myPort;
    // サーバと通信するスレッド
    ClientThread clientThread;
    NfcTags nfcTags = null;
    //インスタンス化無しで使える
    ProcessCommand pc;
    private static final int SETTING = 8888;
    //入力チェック用配列
    //EditText arrEditText[];
    //現在フォーカスチェック用
    EditText arrCantag[];
    EditText arrLotno[];
    List<String> arrKokban = new ArrayList<String>();
    //バイブ
    Vibrator vib;
    private long m_vibPattern_read[] = {0, 200};
    private long m_vibPattern_error[] = {0, 500, 200, 500};

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
        //バーコードリーダー対応
        addTCL();
        //NFCタグ
        this.nfcTags = new NfcTags(this);

        initPage();
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
        else if (cmd.equals(pc.KOB.getString())) {
            setInfoToTextview(excmd);
            focusToNextControl();
        }
        else if (cmd.equals(pc.CBN.getString())) {
            setInfoAfterCantagScan(excmd);
            btnUpd.setEnabled(true);
        }
        else if (cmd.equals(pc.BAC.getString())) {
            //バイブ エラー
            vib.vibrate(m_vibPattern_error, -1);
            //缶タグスキャンに戻る
            backBeforeCantagScan();
            show.setText(excmd);
        }
        else if (cmd.equals(pc.UPD.getString())) {
            /*initPage();
            setShowMessage(1);*/
            //登録完了後はダイアログを表示し、アプリを終了させる
            show.setText("");
            new AlertDialog.Builder(this)
                    .setTitle("確認")
                    .setMessage("登録完了しました。")
                    .setPositiveButton("終了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // OK button pressed
                            finishAndRemoveTask();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
        else if (cmd.equals(pc.MSG.getString())) {
            if (!excmd.equals("")) {
                show.setText(excmd);
            }
        }
        else if (cmd.equals(pc.CNN.getString())) {
            setShowMessage(0);
        }
        else if (cmd.equals(pc.CLR.getString())) {
            //バイブ エラー
            vib.vibrate(m_vibPattern_error, -1);
            show.setText(excmd);
            initPage();
            //txtBcd.setText("");
        }
        else if (cmd.equals(pc.ERR.getString())) {
            //バイブ エラー
            vib.vibrate(m_vibPattern_error, -1);
            show.setText(excmd);
        }
    }

    //タグテキストのコマンド値によって分岐
    private void selectMotionTagText(String sMsg) {
        String cmd = pc.COMMAND_LENGTH.getCmdText(sMsg);
        String excmd  = pc.COMMAND_LENGTH.getExcludeCmdText(sMsg);

        if (cmd.equals(pc.CAN.getString())) {
            //フォーカスが缶タグの場合は画面に反映させる
            View crr = this.getCurrentFocus();
            for (EditText editText: arrCantag) {
                if (crr == editText) {
                    //缶タグ重複チェック
                    if (isScannedCantag(sMsg)) {
                        editText.setText(sMsg);
                        focusToNextControl();
                        //カーボンコードチェックを行う
                        String txt;
                        txt = pc.CBN.getString();
                        txt += m_cbncod;
                        txt += "," + sMsg;
                        sendMsgToServer(txt);
                    }
                }
            }
        }
        else {
            show.setText("タグテキストエラー！");
        }
    }

    private void initPage() {
        //登録ボタンを無効化
        btnUpd.setEnabled(false);
        //EditText初期化
        txtBcd.setText("");
        txtHinban.setText("");
        txtLotno.setText("");
        txtCarbon.setText("");
        m_cbncod = "";
        //ラベル項目初期化
        for (int i = 0; i < arrCantag.length; i++) {
            arrCantag[i].setText("");

            //!!! 注意 : (1),(2)の順番は変更しない。フォーカスが当たるようになってしまう。
            //(1)タップされてもキーボードを出さなくする
            arrCantag[i].setRawInputType(InputType.TYPE_CLASS_TEXT);
            arrCantag[i].setTextIsSelectable(true);
            //(2)フォーカスが当たらなくする
            arrCantag[i].setFocusableInTouchMode(false);
            arrCantag[i].setFocusable(false);
        }
        for (int i = 0; i < arrLotno.length; i++) {
            arrLotno[i].setText("");
        }

        //工管番号配列をクリア
        arrKokban.clear();

        //
        txtBcd.setFocusableInTouchMode(true);
        txtBcd.setFocusable(true);
        txtBcd.requestFocus();
    }

    //次のコントロールにフォーカスを当てる
    private void focusToNextControl(){
        for (int i = 0; i < arrCantag.length; i++) {
            if (TextUtils.isEmpty(arrCantag[i].getText().toString())) {
                if (i == 0) {
                    arrCantag[i].setFocusableInTouchMode(true);
                    arrCantag[i].setFocusable(true);
                    arrCantag[i].requestFocus();
                    setShowMessage(i + 10);
                    return;
                }
                else {
                    arrCantag[i - 1].setFocusableInTouchMode(false);
                    arrCantag[i - 1].setFocusable(false);

                    arrCantag[i].setFocusableInTouchMode(true);
                    arrCantag[i].setFocusable(true);
                    arrCantag[i].requestFocus();
                    setShowMessage(i + 10);
                    return;
                }
            }
        }
        //最終まで値のセットが終わっている場合
        int max = arrCantag.length - 1;
        arrCantag[max].setFocusableInTouchMode(false);
        arrCantag[max].setFocusable(false);
        setShowMessage(99);
    }

    //缶タグタッチ後、サーバから得られた工管番号をセットする
    private void setInfoAfterCantagScan(String info){
        for (int i = 0; i < arrLotno.length; i++) {
            if (TextUtils.isEmpty(arrLotno[i].getText().toString())) {
                String[] items = info.split(",");

                arrKokban.add(items[0]);

                arrLotno[i].setText(items[1]);
                //最終行にセット後のみ、メッセージを変える
                if (i == arrLotno.length - 1) {
                    setShowMessage(99);
                }
                else {
                    setShowMessage(11);
                }
                break;
            }
        }
    }

    //カーボンが不一致だった場合、一行クリアする
    private void backBeforeCantagScan(){
        for (int i = 0; i < arrLotno.length; i++) {
            if (TextUtils.isEmpty(arrLotno[i].getText().toString())) {
                arrCantag[i].setText("");
                arrCantag[i].setFocusableInTouchMode(true);
                arrCantag[i].setFocusable(true);
                arrCantag[i].requestFocus();
                return;
            }
        }
        /*
        //最終まで値のセットが終わっている場合
        int max = arrCantag.length - 1;
        arrCantag[max].setText("");
        arrCantag[max].setFocusableInTouchMode(true);
        arrCantag[max].setFocusable(true);
        arrCantag[max].requestFocus();
        */
    }

    //サーバから取得した情報（品番ロット）を表示
    private void setInfoToTextview(String info) {
        String[] items = info.split(",");

        for (int i = 0; i < items.length; i++) {
            //品番
            if (i == 0) { txtHinban.setText(items[i]); }
            //ロットNo
            else if (i == 1) { txtLotno.setText(items[i]); }
            //カーボンコード
            else if (i == 2) { m_cbncod = items[i]; }
            //カーボン
            else if (i == 3) { txtCarbon.setText(items[i]); }
        }
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
                                    sendMsgToServer(pc.UPD.getString() + createUpdtext());
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
                                    setShowMessage(0);
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
        //生成
        txt += txtBcd.getText().toString() + "@";
        for (int i = 0; i < arrCantag.length; i++) {
            if (TextUtils.isEmpty(arrCantag[i].getText().toString())) {
                break;
            }
            else {
                txt += arrCantag[i].getText().toString() + ",";
                txt += arrKokban.get(i) + ",";
            }
        }
        txt = txt.substring(0, txt.length() - 1);
        return txt;
    }

    //缶タグ重複チェック
    private boolean isScannedCantag(String sMsg) {
        for (EditText editText: arrCantag) {
            if (editText.getText().toString().equals(sMsg)) {
                show.setText(sMsg + "は読み込み済みの缶です。");
                return false;
            }
        }
        return true;
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
                show.setText("工管No.をスキャンしてください。");
                break;
            case 1:
                show.setText("登録完了しました。\n工管No.をスキャンしてください。");
                break;
            case 10:
                show.setText("缶タグをタッチしてください。");
                break;
            case 11:
                show.setText("次の缶タグをタッチするか、\n登録してください。");
                break;
            case 88:
                show.setText("サーバ接続エラー。");
                break;
            case 99:
                show.setText("全てOKです。\n登録してください。");
                btnUpd.setEnabled(true);
                break;
            default:
                show.setText("code" + order);
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

    private void setViews() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("引当て");
        setSupportActionBar(toolbar);

        show = (TextView) findViewById(R.id.show);
        //Button
        btnClear = (Button) findViewById(R.id.btnClear);
        btnUpd = (Button) findViewById(R.id.btnUpd);
        //EditText
        txtBcd = (EditText) findViewById(R.id.txtBcd);
        txtHinban = (EditText) findViewById(R.id.txtHinban);
        txtLotno = (EditText) findViewById(R.id.txtLotno);
        txtCarbon = (EditText) findViewById(R.id.txtCarbon);
        //----入力チェック用配列にセット
        /*arrEditText = new EditText[]{(EditText) findViewById(R.id.txtC1), (EditText) findViewById(R.id.txtK1)
                                    ,(EditText) findViewById(R.id.txtC2), (EditText) findViewById(R.id.txtK2)
                                    ,(EditText) findViewById(R.id.txtC3), (EditText) findViewById(R.id.txtK3)
                                    ,(EditText) findViewById(R.id.txtC4), (EditText) findViewById(R.id.txtK4)
                                    ,(EditText) findViewById(R.id.txtC5), (EditText) findViewById(R.id.txtK5)
                                    ,(EditText) findViewById(R.id.txtC6), (EditText) findViewById(R.id.txtK6)};*/
        //----現在フォーカスチェック用
        //cantag
        arrCantag = new EditText[]{(EditText) findViewById(R.id.txtC1)
                                    ,(EditText) findViewById(R.id.txtC2)
                                    ,(EditText) findViewById(R.id.txtC3)
                                    ,(EditText) findViewById(R.id.txtC4)
                                    ,(EditText) findViewById(R.id.txtC5)
                                    ,(EditText) findViewById(R.id.txtC6)};
        //Lotno
        arrLotno = new EditText[]{(EditText) findViewById(R.id.txtK1)
                                    ,(EditText) findViewById(R.id.txtK2)
                                    ,(EditText) findViewById(R.id.txtK3)
                                    ,(EditText) findViewById(R.id.txtK4)
                                    ,(EditText) findViewById(R.id.txtK5)
                                    ,(EditText) findViewById(R.id.txtK6)};
        /*
        //Changeイベントを実装
        for (EditText editText: arrLotno) {
            editText.addTextChangedListener(watchHandler);
        }
        */

        //クリックイベント
        btnClear.setOnClickListener(this);
        btnUpd.setOnClickListener(this);
        //登録ボタンを無効化
        btnUpd.setEnabled(false);
        txtBcd.requestFocus();
    }

    /*
    //ラベルKokbanを読んだときの処理（匿名クラス）
    private TextWatcher watchHandler = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().equals("")) { return; }
            //カーボンコードチェックを行う
            String txt;
            txt = pc.CBN.getString();
            txt += m_cbncod;
            txt += "," + s.toString();

            sendMsgToServer(txt);
        }
    };
    */

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

    //EditText Listener
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
                if (txtBcd.getText().length() >= 6) {
                    //工程管理Noが6文字以上になったら、工程管理番号問い合わせをサーバーに送信する
                    String cmd = pc.KOB.getString();
                    sendMsgToServer(cmd + txtBcd.getText().toString());
                    //キーボードをしまう
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(txtBcd.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });
    }
}
