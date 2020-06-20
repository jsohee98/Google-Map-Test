package com.example.mapex;

import java.util.ArrayList;

public class GeoData {
    public static ArrayList<CenterData> getAddressData(){
        ArrayList<CenterData> list=new ArrayList<>();

        CenterData data=new CenterData();
        data.centerName="연세공감심리상담센터";
        data.centerLat=37.561241;
        data.centerLng=126.964818;
        list.add(data);

        data=new CenterData();
        data.centerName="서울부부심리상담센터";
        data.centerLat=37.559239;
        data.centerLng=126.961822;
        list.add(data);

        data=new CenterData();
        data.centerName="세은심리상담연구소";
        data.centerLat=37.5560544;
        data.centerLng=126.9651011;
        list.add(data);

        data=new CenterData();
        data.centerName="서울특별시팀청소년일시쉼터";
        data.centerLat=37.553733;
        data.centerLng=126.964316;
        list.add(data);

        data.centerName="서울예술심리상담연구소";
        data.centerLat=37.550773;
        data.centerLng=126.962213;
        list.add(data);

        data=new CenterData();
        data.centerName="한국신체심리상담교육연구원";
        data.centerLat=37.552474;
        data.centerLng=126.9705477;
        list.add(data);

        data=new CenterData();
        data.centerName="AKEFT 코칭센터";
        data.centerLat=37.560226;
        data.centerLng=126.968623;
        list.add(data);

        data=new CenterData();
        data.centerName="서울중독심리연구소";
        data.centerLat=37.558818;
        data.centerLng=126.961644;
        list.add(data);

        data=new CenterData();
        data.centerName="살래";
        data.centerLat=37.554419;
        data.centerLng=126.974024;
        list.add(data);

        data=new CenterData();
        data.centerName="용산아동발달센터";
        data.centerLat=37.546918;
        data.centerLng=126.972131;
        list.add(data);

        return list;
    }
 }

class CenterData {
    String centerName;
    double centerLat;
    double centerLng;
}

