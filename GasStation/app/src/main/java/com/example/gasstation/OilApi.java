package com.example.gasstation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.Xml;

import androidx.annotation.XmlRes;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.Tm128;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;


public class OilApi {

    Context mContext;

    private static final String TAG = "apiTest"; // 로그캣 태그


    //오피넷에서 발급받은 인증키
    String apiKey="F774200525";

//    ArrayList<Oil> allItems=new ArrayList<Oil>();//모든 주유소
//    ArrayList<Oil> regionItem=new ArrayList<Oil>();//지역화페 가맹점인 주유소

    public OilApi( Context context) {
        mContext =context;
    }

    //주유소 좌표값을 상대로 어느 지역인지 확인하는 함수
    public String regionSearch(Double lo, Double la){

        String apiURL =  "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?request=coordsToaddr&coords=" + lo + "," + la + "&sourcecrs=epsg:4326&output=json&orders=roadaddr";

        String xml = new String();
        try {

            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "bv2cn2n8ui");
            con.setRequestProperty("X-NCP-APIGW-API-KEY", "Fj4hgAOcoiAeF8zhgTnwFJJZFEPUudNKQPOwcMko");
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            xml = response.toString();

            //읽어들인 XML문서를 파싱해주는 객체 생성
            JSONObject jObject = new JSONObject(xml);
            JSONArray jja = (JSONArray) jObject.getJSONArray("results");

            JSONObject addr = (JSONObject) jja.getJSONObject(0);
            JSONObject region = (JSONObject) addr.get("region");
            JSONObject area = (JSONObject) region.get("area2");

            Log.d("Test123", area.get("name").toString());
           return area.get("name").toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //주유소 지역화폐가맹점 확인
    private void isRegion(Oil gasStation, String reg){

        Resources res = mContext.getResources();

        StringBuffer buffer = new StringBuffer();

        int xmlnum;
        Log.d("Test123", reg);

        if(reg != null) {
            if (reg.contains("안성시")) xmlnum = R.xml.anseong;
            else if (reg.contains("동두천시")) xmlnum = R.xml.dongducheon;
            else if (reg.contains("가평군")) xmlnum = R.xml.gapyung;
            else if (reg.contains("광주시")) xmlnum = R.xml.gwangju;
            else if (reg.contains("광명시")) xmlnum = R.xml.gwangmyeong;
            else if (reg.contains("하남시")) xmlnum = R.xml.hanam;
            else if (reg.contains("화성시")) xmlnum = R.xml.hwaseong;
            else if (reg.contains("이천시")) xmlnum = R.xml.icheon;
            else if (reg.contains("오산시")) xmlnum = R.xml.osan;
            else if (reg.contains("파주시")) xmlnum = R.xml.paju;
            else if (reg.contains("포천시")) xmlnum = R.xml.pocheon;
            else if (reg.contains("수원시")) xmlnum = R.xml.suwon;
            else if (reg.contains("의정부시")) xmlnum = R.xml.uijeongbu;
            else if (reg.contains("양주시")) xmlnum = R.xml.yangju;
            else if (reg.contains("양평군")) xmlnum = R.xml.yangpyeonggun;
            else if (reg.contains("여주시")) xmlnum = R.xml.yeoju;
            else if (reg.contains("연천군")) xmlnum = R.xml.yeoncheongun;
            else if (reg.contains("용인시")) xmlnum = R.xml.yongin;
            else xmlnum=0;

        }else xmlnum=0;


            if(xmlnum != 0) {

                try {

                    XmlResourceParser xpp = res.getXml(xmlnum);

                    int eventType = xpp.getEventType();

                    String tagName;
                    Double x = 0.0, y = 0.0;

                    while (eventType != XmlPullParser.END_DOCUMENT) {

                        switch (eventType) {
                            case XmlPullParser.START_DOCUMENT:
                                break;

                            case XmlPullParser.START_TAG:
                                tagName = xpp.getName();
                                if (tagName.equals("REFINE_ROADNM_ADDR")) {
                                    //buffer.append("주소: ");
                                    xpp.next();
                                } else if (tagName.equals("REFINE_WGS84_LAT")) {
                                    //buffer.append("x좌표: ");
                                    xpp.next();
                                    if (xpp.getText() != null)
                                        x = Double.parseDouble(xpp.getText());
                                } else if (tagName.equals("REFINE_WGS84_LOGT")) {
                                    //buffer.append("y좌표: ");
                                    xpp.next();
                                    if (xpp.getText() != null)
                                        y = Double.parseDouble(xpp.getText());
                                } else {
                                    xpp.next();
                                }
                                break;

                            case XmlPullParser.TEXT:
                                break;

                            case XmlPullParser.END_TAG:
                                tagName = xpp.getName();
                                if (tagName.equals("row")) {
                                    if (String.format("%.3f", x).equals(String.format("%.3f", gasStation.getX())) & String.format("%.3f", y).equals(String.format("%.3f", gasStation.getY()))) {
                                        gasStation.setRegion(true);
                                        return;
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
    }

    //주변 주유소 검색
    public ArrayList<Oil> oilSearchRadius(Double x, Double y) {


        GeoPoint in_pt = new GeoPoint(y, x);
        GeoPoint katec_pt = GeoTrans.convert(GeoTrans.GEO, GeoTrans.KATEC, in_pt);

        //Log.d(TAG, "x값:" + katec_pt.x + "  y값:" + katec_pt.y);

        ArrayList<Oil> oils=new ArrayList<>();//주유소 코드를 저장할 배열리스트
        String reg = regionSearch(y, x);


        int radius = 5000; //반경(m) //최대 :5000
        String[] prodcd = {"B027", "D047"};// 기름종류 / 휘발유:B027, 경유:D047, 고급휘발유: B034, 실내등유: C004, 자동차부탄: K015)
        Oil oil = new Oil();

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
                                isRegion(oil, reg);
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
                        oils.get(i).setG(oils.get(k).getG());
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
                                isRegion(oil, oil.getAdress());
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
