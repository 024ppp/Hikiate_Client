package com.example.administrator.Hikiate_Client;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

//Viewpagerの中の要素
public class Fragment extends android.support.v4.app.Fragment implements View.OnClickListener, View.OnKeyListener {
    private final static String POSITION = "POSITION";
    private int mPosition;
    private String m_WorkerName = "";
    int[] pages = { R.layout.activity_select_sagyo, R.layout.gamen1};
    ArrayList<EditText> editTexts = new ArrayList<EditText>();
    ListView lv;
    EditText txtSagyo;
    TextView emptyTextView;

    public static Fragment newInstance(int position) {
        Fragment frag = new Fragment();
        Bundle b = new Bundle();
        b.putInt(POSITION, position);
        frag.setArguments(b);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        int position = getArguments().getInt(POSITION);
        //position保持
        mPosition = position;
        View view = inflater.inflate(pages[position], null);

        //コントロール設定
        if (position == 0) {
            setListView(view);
        }
        else {
            setControls(view, position);
            //初期化
            initFragmentPage();
        }
        return view;
    }

    //手入力対応
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        try {
            if (v != null) {
                //イベントを取得するタイミングには、ボタンが押されてなおかつエンターキーだったときを指定
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //DEBUG
                    Log.d("OnKey", "Enter : " + Integer.toString(v.getId()));
                    //エラーになるため、エンターの度にGetする
                    MainActivity mainActivity = (MainActivity) getActivity();
                    //ページ分岐
                    switch (mPosition){
                        case 1:
                            for (int i = 0; i < editTexts.size(); i++) {
                                if (checkFocused(i)) {
                                    pressedEnter(mainActivity, i);
                                    break;
                                }
                            }
                            break;
                    }
                    //キーボードをしまう
                    InputMethodManager imm = (InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
            else {
                return false;
            }

        } catch (ClassCastException e) {
            throw new ClassCastException("activity が OnOkBtnClickListener を実装していません.");
        }
    }

    @Override
    //クリック処理の実装
    public void onClick(View v) {
    }

    //手入力対応の本処理
    private void pressedEnter(MainActivity mainActivity, int num) {
        EditText editText = editTexts.get(num);
        String msg = editText.getText().toString();
        //フォーカス中のEditTextをクリア
        editText.setText("");
        //ページ分岐
        switch (mPosition){
            case 1:

                break;
        }
    }

    public void setTextOrder(String txt){
        EditText editText = null;

        //現在、空白のEditTextの中で一番上にあるものを取得する
        for (EditText edt : editTexts) {
            if (TextUtils.isEmpty(edt.getText().toString())) {
                editText = edt;
                break;
            }
        }
        //テキストをセット
        if (editText != null) {
            editText.setText(txt);
            //値が入ったものは、選択できないようにする
            editText.setFocusableInTouchMode(false);
            editText.setFocusable(false);
        }

        //次の空白のEditTextにフォーカスを移動する
        for (EditText edtNext : editTexts) {
            if (TextUtils.isEmpty(edtNext.getText().toString())) {
                edtNext.setFocusableInTouchMode(true);
                edtNext.setFocusable(true);
                edtNext.requestFocus();
                break;
            }
        }
    }

    public boolean checkFocused(int i) {
        EditText editText = editTexts.get(i);
        return editText.isFocused();
    }

    public boolean checkIsEmpty(int i) {
        if (i < 50) {
            EditText editText = editTexts.get(i);
            return TextUtils.isEmpty(editText.getText());
        }
        else {
            return TextUtils.isEmpty((txtSagyo.getText()));
        }
    }

    //各EditTextから、更新時に必要な情報を取得する
    public String createUpdText() {
        String txt = "";

        switch (mPosition) {
            case 1:
                //
                for (int i = 1; i < editTexts.size(); i++) {
                    if (txt.equals("")) {
                        txt += editTexts.get(i).getText();
                    }
                    else {
                        txt += "," + editTexts.get(i).getText();
                    }
                }
                break;
        }
        return txt;
    }

    //取得した作業者名をListViewにセット
    public void setListWorkerName(String names) {
        String[] members = names.split(",");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_expandable_list_item_1, members);
        lv.setAdapter(adapter);
        m_WorkerName = names;
    }

    //選択された作業者名をセット
    public void setSelectedWorkerName(String name) {
        if (mPosition == 1) {
            txtSagyo.setText(name);
        }
    }

    //ListViewの初期設定、クリックイベントの設定
    private void setListView(View view) {
        lv = (ListView) view.findViewById(R.id.list);
        //ListView で表示リスト項目の無い場合のビューを指定
        emptyTextView = (TextView) view.findViewById(R.id.emptyTextView);
        lv.setEmptyView(emptyTextView);
        //リスト項目がクリックされた時の処理
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                //MainActivity経由で作業者名をセット
                MainActivity mainActivity = (MainActivity) getActivity();

            }
        });
        if (!m_WorkerName.equals("")) {
            setListWorkerName(m_WorkerName);
        }
    }

    private void setControls(View view, int position){
        int[] txtId = null;

        switch(position) {
            case 1:
                txtId = new int[] {R.id.txtKikai
                        //, R.id.txtKokan, R.id.txtKotkbn, R.id.txtCov, R.id.txtBox
                };
                //txtSagyoのみ特別扱い
                txtSagyo = (EditText) view.findViewById(R.id.txtSagyo);
                txtSagyo.setFocusableInTouchMode(false);
                txtSagyo.setFocusable(false);
                break;
        }

        //EditText
        for (int id : txtId) {
            EditText editText = (EditText) view.findViewById(id);
            //タグスキャン時の幅崩れ対策
            editText.setWidth(editText.getWidth());
            //手入力対応
            editText.setOnKeyListener(this);
            editTexts.add(editText);
        }
    }

    public void initFragmentPage() {
        EditText editText = null;

        switch(mPosition) {
            case 0:
                return;
            case 1:
                txtSagyo.setText("");
                break;
        }

        //EditText初期化
        for (int i = 0; i < editTexts.size(); i++) {
            editText = editTexts.get(i);
            editText.setText("");
            /*
            //保留：手入力の必要あり
            //タップされてもキーボードを出さなくする
            editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
            editText.setTextIsSelectable(true);
            */

            if (i == 0) {
                editText.setFocusableInTouchMode(true);
                editText.setFocusable(true);
            }
            else {
                editText.setFocusableInTouchMode(false);
                editText.setFocusable(false);
            }
        }

        //ページ最初のコントロールにフォーカスをあてる
        editText = editTexts.get(0);
        editText.requestFocus();
    }
}
