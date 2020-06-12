package com.example.testapp1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ActivitySearch extends AppCompatActivity{

    //오피넷에서 발급받은 인증키
    String apiKey="F774200525";


    private OilAdapter adapter;
    private ListView listView;


    private static final String TAG = "listTest"; // 로그캣 태그

    EditText edit;

    ArrayList<Oil> items=new ArrayList<Oil>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        edit= (EditText)findViewById(R.id.edit);

        adapter = new OilAdapter();
        listView = (ListView) findViewById(R.id.listview1);

        listView.setAdapter(adapter);

        //리스트에 있는 주유소를 클릭 했을 시
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // list의 item을 클릭했을 시 ActivityMap을 불러옴과 동시에 값 전달
                String title = ((Oil)adapter.getItem(position)).getTitle();
                String x = ((Oil)adapter.getItem(position)).getX();
                String y = ((Oil)adapter.getItem(position)).getY();

                // new Intent(Intent를 통하여 액티비티 변경)
                Intent intent = new Intent(ActivitySearch.this, ActivityMap.class);
                // putExtra(key, value)
                intent.putExtra("title", title);
                intent.putExtra("x", x);
                intent.putExtra("y", y);
                intent.putExtra("oil", items);
                startActivity(intent);
            }
        });

    }

    //돌아가기 버튼 클릭 시
    public void clickBack(View view) { finish(); }

    //주유소 코드를 얻기 위한 검색기능
    private ArrayList<String> oilsearch(String str) {


        ArrayList<String> oilcode=new ArrayList<String>();

        String location = URLEncoder.encode(str); //  URL 인코딩

        String adress = "https://www.opinet.co.kr/api/searchByName.do?code="
                + apiKey
                + "&out=xml&osnm=" + str;
        //주소 뒤에 [? key=Value & key = value id= aaa & pw= 1234] 이게 GET방식

        //유류 가격 정보 adress="http://www.opinet.co.kr/api/avgAllPrice.do?out=xml&code=F774200525";
        // 주유소 상세 정보 adress="http://www.opinet.co.kr/api/detailById.do?code=" + apikey + "&id=" + 주유소id + "&out=xml";
        //위 xml문서의 주소(address)에 스트림을 연결하여 데이터를 읽어오기


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

            while(eventType!=XmlPullParser.END_DOCUMENT){

                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;

                    case XmlPullParser.START_TAG:
                        tagName=xpp.getName();
                        if(tagName.equals("OIL")){
                            //검색한 주유소의 코드번호 추출
                        }else if(tagName.equals("UNI_ID")){
                            xpp.next();
                            //oilcode 리스트에 추가
                            oilcode.add(xpp.getText());
                        }else {
                            xpp.next();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        tagName = xpp.getName();
                        if(tagName.equals("OIL")){
                        }
                        break;
                }

                eventType=xpp.next();
            }//while ..

        } catch (MalformedURLException e) { e.printStackTrace();} catch (IOException e) {e.printStackTrace();} catch (XmlPullParserException e) {e.printStackTrace();}

        return oilcode;
    }

    //검색 버튼 클릭
    public void clickBtn(View view) {
        //네트워크를 통해서 xml문서를 읽어오기..
        new Thread(){
            @Override
            public void run() {

                adapter.removeItemAll();
                items.clear();

                String str= edit.getText().toString(); // edit에 입력한 글자 얻어오기
                String location = URLEncoder.encode(str); //  URL 인코딩

                //검색한 주유소들의 코드를 가지고 주유소 상세검색
                ArrayList<String> oils= oilsearch(str);
                int i=0;
                while(i<oils.size()){
                    String adress = "http://www.opinet.co.kr/api/detailById.do?code=" + apiKey + "&id=" + oils.get(i) + "&out=xml";

                    runOnUiThread(new Runnable() {  //여기는 별도 스레드이므로 화면 구성을 하려면 runOnUiThread 필요
                        @Override
                        public void run() {
                            Toast.makeText(ActivitySearch.this,"검색 중..",Toast.LENGTH_SHORT).show();
                        }
                    });

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
                                        //Log.d(TAG, oil.getTitle());
                                    }else if(tagName.equals("VAN_ADR")){
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
                                        oil.setX(xpp.getText());
                                    }else if(tagName.equals("GIS_Y_COOR")){
                                        //buffer.append("y좌표: ");
                                        xpp.next();
                                        oil.setY(xpp.getText());
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
                                        items.add(oil);

                                    }
                                    break;
                            }

                            eventType=xpp.next();
                        }//while ..


                    } catch (MalformedURLException e) { e.printStackTrace();} catch (IOException e) {e.printStackTrace();} catch (XmlPullParserException e) {e.printStackTrace();}


                    i++;
                } // olis while 끝..

                //주소 뒤에 [? key=Value & key = value id= aaa & pw= 1234] 이게 GET방식

                //유류 가격 정보 adress="http://www.opinet.co.kr/api/avgAllPrice.do?out=xml&code=F774200525";
                // 주유소 상세 정보 adress="http://www.opinet.co.kr/api/detailById.do?code=" + apikey + "&id=" + 주유소id + "&out=xml";
                //위 xml문서의 주소(address)에 스트림을 연결하여 데이터를 읽어오기

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setData();
                        Toast.makeText(ActivitySearch.this, "검색완료!!" ,Toast.LENGTH_SHORT).show();
                    }
                });

            }// run() ..
        }.start();
    }

    private void setData() { //리스트 업데이트 메소드
        for (int i = 0; i < items.size(); i++) {
            adapter.addItem(items.get(i));
            adapter.notifyDataSetChanged();
        }
        Log.d(TAG, "업데이트 완료");
    }



}
