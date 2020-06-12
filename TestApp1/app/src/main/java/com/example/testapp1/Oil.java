package com.example.testapp1;

import java.io.Serializable;

//주유소 객체
public class Oil implements Serializable {
    private String code;//코드
    private String title;//상호
    private String adr;//주소
    private String tel;//번호
    private String x;//x좌표
    private String y;//y좌표
    private int h1=0, g=0, h2=0, s=0, b=0;//유류 가격(h1:휘발유, g:경유, h2:고급휘발유, s:실내용등유, b:자동차부탄)

    //코드
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    //상호명
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    //주소
    public String getAdress() {
        return adr;
    }

    public void setAdress(String adr) {
        this.adr = adr;
    }

    //번호
    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    //x좌표
    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    //y좌표
    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    //유류 가격
//(값얻기) 휘발유                                 경유                                고급휘발유                           실내용등유                          자동차부탄
    public int getH1() { return h1; }   public int getG() { return g; }  public int getH2() { return h2; } public int getS() { return s; }  public int getB() { return b; }
//(값설정) 휘발유                                             경유                                          고급휘발유
    public void setH1(int h1) {
        this.h1 = h1;
    }   public void setG(int g) {
        this.g = g;
    }   public void setH2(int h2) {
        this.h2 = h2;
    }
    //실내등유                                      자동차부탄(LPG)
    public void setS(int s) {
        this.s = s;
    }   public void setB(int b) {
        this.b = b;
    }
}
