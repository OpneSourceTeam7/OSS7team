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

//    ArrayList<Oil> allItems=new ArrayList<Oil>();//모든 주유소
//    ArrayList<Oil> regionItem=new ArrayList<Oil>();//지역화페 가맹점인 주유소

    public OilApi( Context context) {
        mContext = context;
    }

    //주유소 지역화폐가맹점 확인
    private void isRegion(Oil gasStation){

                Resources res= mContext.getResources();

                XmlResourceParser xpp = res.getXml(R.xml.gapyung);//가평

                StringBuffer buffer = new StringBuffer();


                try {

                    int eventType = xpp.getEventType();

                    String tagName;
                    Double x=0.0,y=0.0;

                    while (eventType != XmlPullParser.END_DOCUMENT) {

                        switch (eventType) {
                            case XmlPullParser.START_DOCUMENT:
                                break;

                            case XmlPullParser.START_TAG:
                                tagName = xpp.getName();
                                if (tagName.equals("REFINE_ROADNM_ADDR")) {
                                    //buffer.append("주소: ");
                                    xpp.next();
                                }else if (tagName.equals("REFINE_WGS84_LAT")) {
                                    //buffer.append("x좌표: ");
                                    xpp.next();
                                    if (xpp.getText() != null)
                                        x= Double.parseDouble(xpp.getText());
                                }else if (tagName.equals("REFINE_WGS84_LOGT")) {
                                    //buffer.append("y좌표: ");
                                    xpp.next();
                                    if (xpp.getText() != null)
                                        y= Double.parseDouble(xpp.getText());
                                }else {
                                    xpp.next();
                                }
                                break;

                            case XmlPullParser.TEXT:
                                break;

                            case XmlPullParser.END_TAG:
                                tagName = xpp.getName();
                                if (tagName.equals("row")) {
                                    if( String.format("%.3f",x).equals(String.format("%.3f",gasStation.getX())) & String.format("%.3f",y).equals(String.format("%.3f",gasStation.getY())) ) {
                                        gasStation.setRegion(true);
                                    }

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

    }

    //주변 주유소 검색
    public ArrayList<Oil> oilSearchRadius(Double x, Double y) {


        GeoPoint in_pt = new GeoPoint(y, x);
        GeoPoint katec_pt = GeoTrans.convert(GeoTrans.GEO, GeoTrans.KATEC, in_pt);

        //Log.d(TAG, "x값:" + katec_pt.x + "  y값:" + katec_pt.y);

        ArrayList<Oil> oils=new ArrayList<>();//주유소 코드를 저장할 배열리스트


        int radius = 5000; //반경(m) //최대 :5000
        String[] prodcd = {"B027", "D047"};// 기름종류 / 휘발유:B027, 경유:D047, 고급휘발유: B034, 실내등유: C004, 자동차부탄: K015)

        try {

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
                Oil oil = new Oil();
                String oilX="",oilY="";

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
                                //주유소 코드
                                oil.setCode(xpp.getText());
                            }else if (tagName.equals("OS_NM")) {
                                xpp.next();
                                //주유소 상호명
                                oil.setTitle(xpp.getText());
                            }else if (tagName.equals("PRICE")) {
                                xpp.next();
                                //주유소 기름 가격
                                if(pNum==0){
                                    oil.setH1(Integer.parseInt(xpp.getText()));
                                }else if(pNum==1){
                                    oil.setG(Integer.parseInt(xpp.getText()));
                                }

                            }else if (tagName.equals("GIS_X_COOR")) {
                                xpp.next();
                                //주유소 x좌표
                                oilX=xpp.getText();
                            }else if (tagName.equals("GIS_Y_COOR")) {
                                xpp.next();
                                //주유소 y좌표
                                oilY=xpp.getText();
                            }else {
                                xpp.next();
                            }
                            break;

                        case XmlPullParser.TEXT:
                            break;

                        case XmlPullParser.END_TAG:
                            tagName = xpp.getName();

                            if (tagName.equals("OIL")) {
                                Tm128 tm = new Tm128(Double.parseDouble(oilX), Double.parseDouble(oilY));
                                LatLng x2= tm.toLatLng();
                                oil.setX(x2.latitude);
                                oil.setY(x2.longitude);
                                isRegion(oil);
                                oils.add(oil);
                                oil = new Oil();
                            }
                            break;
                    }

                    eventType = xpp.next();
                }//while ..



        }//..for

            for(int i=0;i < oils.size();i++){
                for(int k=0;k<oils.size();k++){
                    if(oils.get(i).equals(oils.get(k))){
                        continue;
                    }
                    if(oils.get(i).getCode().equals(oils.get(k).getCode())){
                        oils.remove(oils.get(k));
                    }
                }
                Log.d(TAG, oils.get(i).getTitle() + i);
            }



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }


        return oils;
    }

    //주유소 상호검색
    public ArrayList<Oil> oilSearchName(String str) {

        ArrayList<Oil> stations=new ArrayList<>();//주유소 코드를 저장할 배열리스트

            String adress = "https://www.opinet.co.kr/api/searchByName.do?code="
                + apiKey
                + "&out=xml&osnm=" + str + "&area=02"; //경기도 시도코드:02

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
                String oilX="",oilY="";
                Oil oil = new Oil();

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
                                oil.setCode(xpp.getText());
                            }else if (tagName.equals("OS_NM")) {
                                xpp.next();
                                //주유소 상호명
                                oil.setTitle(xpp.getText());
                            }else if (tagName.equals("GIS_X_COOR")) {
                                xpp.next();
                                //주유소 x좌표
                                oilX=xpp.getText();
                            }else if (tagName.equals("GIS_Y_COOR")) {
                                xpp.next();
                                //주유소 y좌표
                                oilY=xpp.getText();
                            }else if(tagName.equals("NEW_ADR")){
                                //buffer.append("주소: ");
                                xpp.next();
                                oil.setAdress(xpp.getText());
                            }else if(tagName.equals("TEL")){
                                //buffer.append("번호: ");
                                xpp.next();
                                oil.setTel(xpp.getText());
                            } else {
                                xpp.next();
                            }
                            break;

                        case XmlPullParser.TEXT:
                            break;

                        case XmlPullParser.END_TAG:
                            tagName = xpp.getName();
                            if (tagName.equals("OIL")) {
                                Tm128 tm = new Tm128(Double.parseDouble(oilX), Double.parseDouble(oilY));
                                LatLng x2= tm.toLatLng();
                                oil.setX(x2.latitude);
                                oil.setY(x2.longitude);
                                isRegion(oil);
                                stations.add(oil);
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


        return stations;
    }

    //주유소 상세검색
    public Oil detailGas(String str) {
        //네트워크를 통해서 xml문서를 읽어오기..

        String address = "https://www.opinet.co.kr/api/detailById.do?code=" + apiKey + "&id=" + str + "&out=xml";
        Oil oil = new Oil();


            try {
                //URL객체생성
                URL url= new URL(address);

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
                            }
                            break;
                    }

                    eventType=xpp.next();
                }//while ..

                return oil;

            } catch (MalformedURLException e) { e.printStackTrace();} catch (IOException e) {e.printStackTrace();} catch (XmlPullParserException e) {e.printStackTrace();}

        return oil;

    }

}
