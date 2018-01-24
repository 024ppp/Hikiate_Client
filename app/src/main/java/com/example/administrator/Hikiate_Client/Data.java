package com.example.administrator.Hikiate_Client;

// データ格納用クラス
class Data {
    private String number = "";
    private String zainmk = "";
    private String kokban = "";
    private String cansuu = "";
    private String vkonno = "";
    private String meiban = "";
    private String canTag = "";
    private String hantei = "";

    public void setData(int mode, String[] items) {
        for (int i = 0; i < items.length; i++) {
            switch (mode) {
                //排出
                case 1:
                    switch (i) {
                        case 0: setNumber(items[i]);
                            break;
                        case 1: setZainmk(items[i]);
                            break;
                        case 2: setKokban(items[i]);
                            break;
                        case 3: setCansuu(items[i]);
                            break;
                        case 4: setVkonno(items[i]);
                            break;
                        case 5: setMeiban(items[i]);
                            break;
                        default:
                    }
                    break;

                //検量
                case 3:
                    switch (i) {
                        case 0: setNumber(items[i]);
                            break;
                        default:
                    }
                    break;
                //保管
                case 4:
                    switch (i) {
                        case 0: setNumber(items[i]);
                            break;
                        case 1: setCanTag(items[i]);
                            break;
                        default:
                    }
                    break;
                default:
            }
        }
    }

    public void setNumber(String tmp) {
        this.number = tmp;
    }
    public String getNumber() {
        return number;
    }

    public void setZainmk(String tmp) {
        this.zainmk = tmp;
    }
    public String getZainmk() {
        return zainmk;
    }

    public void setKokban(String tmp) {
        this.kokban = tmp;
    }
    public String getKokban() {
        return kokban;
    }

    public void setCansuu(String tmp) {
        this.cansuu = tmp;
    }
    public String getCansuu() {
        return cansuu;
    }

    public void setVkonno(String tmp) {
        this.vkonno = tmp;
    }
    public String getVkonno() {
        return vkonno;
    }

    public void setMeiban(String tmp) {
        this.meiban = tmp;
    }
    public String getMeiban() {
        return meiban;
    }

    public void setCanTag(String tmp) {
        this.canTag = tmp;
    }
    public String getCanTag() {
        return canTag;
    }

    public void setHantei(String tmp) {
        this.hantei = tmp;
    }
    public String getHantei() {
        return hantei;
    }

}