package com.example.administrator.Hikiate_Client;

/**
 * Created by Administrator on 2017/05/25.
 */

public enum ProcessCommand {
    COMMAND_LENGTH("COMMAND_LENGTH", 3),
    SAG("SAG", 1001),
    VKO("VKO", 1002),
    IDO("IDO", 1003),
    KOB("KOB", 1004),
    CAN("CAN", 1005),
    CLR("CLR", 1006),
    MSV("MSV", 1007),
    OPE("OPE", 5555),
    DUP("DUP", 6001),
    HUP("HUP", 6002),
    KUP("KUP", 6003),
    SUP("SUP", 6004),
    REC("REC", 7777),
    MSG("MSG", 8888),
    ERR("ERR", 9999)
    ;

    private final String text;
    private final int id;

    private ProcessCommand(final String text,  final int id) {
        this.text = text;
        this.id = id;
    }

    public String getString() {
        return this.text;
    }

    public int getInt() {
        return this.id;
    }

    //COMMAND_LENGTHからの呼び出しにのみ対応
    public String getCmdText(String txt) {
        String cmd = "";
        if (this.text.equals("COMMAND_LENGTH")) {
            cmd = txt.substring(0, this.id);
        }
        return cmd;
    }

    public String getExcludeCmdText(String txt) {
        String excmd = "";
        if (this.text.equals("COMMAND_LENGTH")) {
            excmd = txt.substring(this.id);
        }
        return excmd;
    }

    public boolean isSameString(String txt) {
        if (this.text.equals(txt)) {
            return true;
        }
        else {
            return false;
        }
    }
}
