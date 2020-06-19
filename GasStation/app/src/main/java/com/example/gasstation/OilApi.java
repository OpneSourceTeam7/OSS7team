package com.example.gasstation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.Tm128;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class OilApi {

    Context mContext;

    private static final String TAG = "apiTest"; // 로그캣 태그


    //오피넷에서 발급받은 인증키
    String apiKey="F774200525";
    ArrayList<Oil> allItems=new ArrayList<Oil>();//모든 주유소
    ArrayList<Oil> regionItem=new ArrayList<Oil>();//지역화페 가맹점인 주유소

    public OilApi( Context context) {
        mContext = context;
    }

    //주유소 지역화폐가맹점 확인
    private void isRegion(){

                Resources res= mContext.getResources();

                XmlResourceParser xpp = res.getXml(R.xml.gapyung);//가평

                StringBuffer buffer = new StringBuffer();


                try {

                    int eventType = xpp.getEventType();

                    String tagName;
                    Oil oil = new Oil();

                    while (eventType != XmlPullParser.END_DOCUMENT) {

                        switch (eventType) {
                            case XmlPullParser.START_DOCUMENT:
                                break;

                            case XmlPullParser.START_TAG:
                                tagName = xpp.getName();
                                if (tagName.equals("CMPNM_NM")) {
                                    //buffer.append("이름: ");
                                    xpp.next();
                                    oil.setTitle(xpp.getText());
                                    //Log.d(TAG, oil.getTitle());
                                } else if (tagName.equals("REFINE_ROADNM_ADDR")) {
                                    //buffer.append("주소: ");
                                    xpp.next();
                                    oil.setAdress(xpp.getText());
                                } else if (tagName.equals("TELNO")) {
                                    //buffer.append("번호: ");
                                    xpp.next();
                                    oil.setTel(xpp.getText());
                                } else if (tagName.equals("REFINE_WGS84_LAT")) {
                                    //buffer.append("x좌표: ");
                                    xpp.next();
                                    if (xpp.getText() != null)
                                        oil.setX(Double.parseDouble(xpp.getText()));
                                } else if (tagName.equals("REFINE_WGS84_LOGT")) {
                                    //buffer.append("y좌표: ");
                                    xpp.next();
                                    if (xpp.getText() != null)
                                        oil.setY(Double.parseDouble(xpp.getText()));
                                } else if (tagName.equals("PRODCD")) {
                                    //기름 가격
                                    xpp.next();
                                } else {
                                    xpp.next();
                                }
                                break;

                            case XmlPullParser.TEXT:
                                break;

                            case XmlPullParser.END_TAG:
                                tagName = xpp.getName();
                                if (tagName.equals("row")) {
                                    regionItem.add(oil);
                                    oil = new Oil();
                                }
                                break;
                        }
                        eventType = xpp.next();
                    }//while ..


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }

                DecimalFormat form = new DecimalFormat("#.##");

                for(int i=0;i < regionItem.size();i++) {
                    for(int j=0;j < allItems.size();j++) {
                        if( regionItem.get(i).getAdress()==null | allItems.get(j).getRegion() ) continue;
                        //if( form.format(regionItem.get(i).getX()).equals(form.format(allItems.get(j).getX())) & form.format(regionItem.get(i).getY()).equals(form.format(allItems.get(j).getY())) ) {
                        //if( String.format("%.3f",regionItem.get(i).getX()).equals(String.format("%.3f",allItems.get(j).getX())) & String.format("%.3f",regionItem.get(i).getY()).equals(String.format("%.3f",allItems.get(j).getY())) ) {
                        if( regionItem.get(i).getAdress().equals(allItems.get(j).getAdress()) ){
                        //Log.d(TAG, "전국:" + allItems.get(j).getTitle() + " 지역:" + regionItem.get(i).getTitle());
                            allItems.get(j).setRegion(true);
                        }
                    }
                }

    }

    //주변 주유소 검색
    private ArrayList<String> oilSearchRadius(Double x, Double y) {


        GeoPoint in_pt = new GeoPoint(y, x);
        GeoPoint katec_pt = GeoTrans.convert(GeoTrans.GEO, GeoTrans.KATEC, in_pt);

        //Log.d(TAG, "x값:" + katec_pt.x + "  y값:" + katec_pt.y);

        ArrayList<String> oilcode=new ArrayList<String>();//주유소 코드를 저장할 배열리스트


        int radius = 5000; //반경(m) //최대 :5000
        String[] prodcd = {"B027", "D047"};// 기름종류 / 휘발유:B027, 경유:D047, 고급휘발유: B034, 실내등유: C004, 자동차부탄: K015)

        for(int pNum=0;pNum < prodcd.length; pNum++) {
            //https://www.opinet.co.kr/api/aroundAll.do?code=" + apiKey + "&x=" + x + ."8&y=" + y + "&radius=" + radius + "&sort=1&prodcd=B027&out=xml
            String adress = "https://www.opinet.co.kr/api/aroundAll.do?" +
                    "code=" + apiKey //api키
                    + "&x=" + katec_pt.x //x값
                    + "8&y=" + katec_pt.y //y값
                    + "&radius=" + radius //반경
                    + "&sort=1&prodcd=" + prodcd[pNum] //기름종류
                    + "&out=xml";
            //주소 뒤에 [? key=Value & key = value id= aaa & pw= 1234] 이게 GET방식

            //유류 가격 정보 adress="http://www.opinet.co.kr/api/avgAllPrice.do?out=xml&code=F774200525";
            // 주유소 상세 정보 adress="http://www.opinet.co.kr/api/detailById.do?code=" + apikey + "&id=" + 주유소id + "&out=xml";
            //위 xml문서의 주소(address)에 스트림을 연결하여 데이터를 읽어오기


            try {
                //URL객체생성
                URL url = new URL(adress);

                //Stream 열기
                InputStream is = url.openStream(); //바이트스트림
                //문자스트림으로 변환
                InputStreamReader isr = new InputStreamReader(is);

                //읽어들인 XML문서를 파싱해주는 객체 생성
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(isr);

                //xpp를 이용해서 xml문서를 분석

                //xpp.next();   //XmlPullParser는 시작부터 문서의 시작점에 있으므로 next해주면 START_DOCUMENT를 못만난다.
                int eventType = xpp.getEventType();

                String tagName;

                while (eventType != XmlPullParser.END_DOCUMENT) {

                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            break;

                        case XmlPullParser.START_TAG:
                            tagName = xpp.getName();
                            if (tagName.equals("OIL")) {
                                //검색한 주유소의 코드번호 추출
                            } else if (tagName.equals("UNI_ID")) {
                                xpp.next();
                                //oilcode 리스트에 추가
                                oilcode.add(xpp.getText());
                            } else {
                                xpp.next();
                            }
                            break;

                        case XmlPullParser.TEXT:
                            break;

                        case XmlPullParser.END_TAG:
                            tagName = xpp.getName();
                            if (tagName.equals("OIL")) {
                            }
                            break;
                    }

                    eventType = xpp.next();
                }//while ..

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }

        }//..for

        return oilcode;
    }

    public ArrayList<Oil> searchGas(Double xx, Double yy) {
        //네트워크를 통해서 xml문서를 읽어오기..


                allItems.clear();

                //검색한 주유소들의 코드를 가지고 주변 주유소 상세검색
                ArrayList<String> oils= oilSearchRadius(xx, yy);
                int i=0;
                while(i<oils.size()){
                    String adress = "https://www.opinet.co.kr/api/detailById.do?code=" + apiKey + "&id=" + oils.get(i) + "&out=xml";


                    try {
                        //URL객체생성
                        URL url= new URL(adress);

                        //Stream 열기
                        InputStream is= url.openStream(); //바이트스트림
                        //문자스트림으로 변환
                        InputStreamReader isr=new InputStreamReader(is);

                        //읽어들인 XML문서를 파싱해주는 객체 생성
                        XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
                        XmlPullParser xpp=factory.newPullParser();
                        xpp.setInput(isr);

                        //xpp를 이용해서 xml문서를 분석

                        //xpp.next();   //XmlPullParser는 시작부터 문서의 시작점에 있으므로 next해주면 START_DOCUMENT를 못만난다.
                        int eventType= xpp.getEventType();

                        String tagName;
                        Oil oil = new Oil();
                        String oilCode= null;
                        String x="",y="";

                        while(eventType!=XmlPullParser.END_DOCUMENT){

                            switch (eventType){
                                case XmlPullParser.START_DOCUMENT:
                                    break;

                                case XmlPullParser.START_TAG:
                                    tagName=xpp.getName();
                                    if(tagName.equals("OIL")){

                                    }else if(tagName.equals("UNI_ID")){
                                        //buffer.append("코드: ");
                                        xpp.next();
                                        oil.setCode(xpp.getText());
                                    }else if(tagName.equals("OS_NM")){
                                        //buffer.append("이름: ");
                                        xpp.next();
                                        oil.setTitle(xpp.getText());
                                        Log.d(TAG, oil.getTitle());
                                    }else if(tagName.equals("NEW_ADR")){
                                        //buffer.append("주소: ");
                                        xpp.next();
                                        oil.setAdress(xpp.getText());
                                    }else if(tagName.equals("TEL")){
                                        //buffer.append("번호: ");
                                        xpp.next();
                                        oil.setTel(xpp.getText());
                                    }else if(tagName.equals("GIS_X_COOR")){
                                        //buffer.append("x좌표: ");
                                        xpp.next();
                                        x= xpp.getText();
                                    }else if(tagName.equals("GIS_Y_COOR")){
                                        //buffer.append("y좌표: ");
                                        xpp.next();
                                        y=xpp.getText();
                                    }else if(tagName.equals("PRODCD")) {
                                        //기름 가격
                                        xpp.next();
                                        oilCode = xpp.getText();
                                    } else if(tagName.equals("PRICE")){
                                        //휘발유:B027, 경유:D047, 고급휘발유: B034, 실내등유: C004, 자동차부탄(LPG): K015
                                        if(oilCode.equals("B027")){ //휘발유
                                            xpp.next();
                                            oil.setH1(Integer.parseInt(xpp.getText()));
                                        }else if(oilCode.equals("D047")){ //경유
                                            xpp.next();
                                            oil.setG(Integer.parseInt(xpp.getText()));
                                        }else if(oilCode.equals("B034")){ //고급휘발유
                                            xpp.next();
                                            oil.setH2(Integer.parseInt(xpp.getText()));
                                        }else if(oilCode.equals("C004")){ //실내등유
                                            xpp.next();
                                            oil.setS(Integer.parseInt(xpp.getText()));
                                        }else if(oilCode.equals("K015")){ //LPG
                                            xpp.next();
                                            oil.setB(Integer.parseInt(xpp.getText()));
                                        }
                                    }else {
                                        xpp.next();
                                    }
                                    break;

                                case XmlPullParser.TEXT:
                                    break;

                                case XmlPullParser.END_TAG:
                                    tagName = xpp.getName();
                                    if(tagName.equals("OIL")){
                                        Tm128 tm = new Tm128(Double.parseDouble(x), Double.parseDouble(y));
                                        LatLng x2= tm.toLatLng();
                                        oil.setX(x2.latitude);
                                        oil.setY(x2.longitude);
                                        allItems.add(oil);
                                    }
                                    break;
                            }

                            eventType=xpp.next();
                        }//while ..


                    } catch (MalformedURLException e) { e.printStackTrace();} catch (IOException e) {e.printStackTrace();} catch (XmlPullParserException e) {e.printStackTrace();}


                    i++;
                } // olis while 끝..
                isRegion();
                return allItems;

    }


}
