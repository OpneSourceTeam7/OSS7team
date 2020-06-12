package com.example.testapp1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class OilAdapter extends BaseAdapter {
    private ArrayList<Oil> listCustom = new ArrayList<>();

    // ListView에 보여질 Item 수
    @Override
    public int getCount() {
        return listCustom.size();
    }

    // 하나의 Item(코드, 상호명, 주소, 번호)
    @Override
    public Object getItem(int position) {
        return listCustom.get(position);
    }

    // Item의 id : Item을 구별하기 위한 것으로 position 사용
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 실제로 Item이 보여지는 부분
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OilViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.oil_item, null, false);

            holder = new OilViewHolder();
            holder.oilTitle = (TextView) convertView.findViewById(R.id.oil_title);
            holder.oilAdress = (TextView) convertView.findViewById(R.id.oil_adr);
            holder.oilTel = (TextView) convertView.findViewById(R.id.oil_tel);

            holder.oilH1 = (TextView) convertView.findViewById(R.id.oil_h1);
            holder.oilG = (TextView) convertView.findViewById(R.id.oil_g);
            holder.oilH2 = (TextView) convertView.findViewById(R.id.oil_h2);
            //holder.oilS = (TextView) convertView.findViewById(R.id.oil_s);
            //holder.oilB = (TextView) convertView.findViewById(R.id.oil_b);

            convertView.setTag(holder);
        } else {
            holder = (OilViewHolder) convertView.getTag();
        }

        Oil oil = listCustom.get(position);

        if(oil.getH1() > 0) holder.oilH1.setText("휘발유: " + oil.getH1());
        if(oil.getG() > 0) holder.oilG.setText("경유: " + oil.getG());
        if(oil.getH2() > 0) holder.oilH2.setText("고급휘발유: " + oil.getH2());
        //if(oil.getS() > 0) holder.oilS.setText("실내등유: " + oil.getS());
        //if(oil.getB() > 0) holder.oilB.setText("LPG: " + oil.getB());

        holder.oilTitle.setText(oil.getTitle());
        holder.oilAdress.setText(oil.getAdress());
        holder.oilTel.setText(oil.getTel());

        return convertView;
    }

    class OilViewHolder {
        TextView oilTitle;
        TextView oilAdress;
        TextView oilTel;

        TextView oilH1;
        TextView oilG;
        TextView oilH2;
        TextView oilS;
        TextView oilB;
    }

    // MainActivity에서 Adapter에있는 ArrayList에 data를 추가시켜주는 함수
    public void addItem(Oil oil) {
        listCustom.add(oil);
    }

    public void removeItemAll() {
        listCustom.clear();
    }
}
