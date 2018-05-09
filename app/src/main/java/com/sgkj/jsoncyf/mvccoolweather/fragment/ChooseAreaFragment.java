package com.sgkj.jsoncyf.mvccoolweather.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.sgkj.jsoncyf.mvccoolweather.R;
import com.sgkj.jsoncyf.mvccoolweather.activity.WeatherActivity;
import com.sgkj.jsoncyf.mvccoolweather.db.City;
import com.sgkj.jsoncyf.mvccoolweather.db.County;
import com.sgkj.jsoncyf.mvccoolweather.db.Province;
import com.sgkj.jsoncyf.mvccoolweather.util.HttpUtil;
import com.sgkj.jsoncyf.mvccoolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseAreaFragment extends Fragment {
    private static final String TAG = "ChooseAreaFragment";
    public String strHttp="http://guolin.tech/api/china";

    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;
    private TextView titleTv;
    private Button backBtn;
    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList=new ArrayList<>();
    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;

    //选中的省份
    private Province selectdProvince;
    //选中的城市
    private City selectdCity;

    //当前选中的级别
    private int currentLevel;

    public ChooseAreaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleTv = (TextView)view.findViewById(R.id.title_tv);
        backBtn = (Button)view.findViewById(R.id.back_btn);
        listView = (ListView)view.findViewById(R.id.list_view);
        adapter= new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectdProvince=provinceList.get(position);
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    selectdCity=cityList.get(position);
                    queryCounties();
                }else if (currentLevel==LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    Intent intent=new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             if (currentLevel==LEVEL_COUNTY){
                 queryCities();
             } else if (currentLevel==LEVEL_CITY) {
                 queryProvinces();
             }
            }});

        queryProvinces();
    }

    //查询全国所有的省,优先从数据库查询,如果没有查询到再去服务器上查询
    private void queryProvinces(){
        titleTv.setText("中国");
        backBtn.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);

            currentLevel=LEVEL_PROVINCE;
        }else {
            String strUrl=strHttp;
            queryFromServer(strUrl,"province");
        }
    }

    //查询选中省内所有的市,优先从数据库查询,如果没有查询到再去服务器上查询
    private void queryCities(){
        titleTv.setText(selectdProvince.getProvinceName());
        backBtn.setVisibility(View.VISIBLE);

        cityList=DataSupport.where("provinceid=?",String.valueOf(selectdProvince.getId())).find(City.class);

        if (cityList.size()>0){
            dataList.clear();
            for (City city :cityList){
                dataList.add(city.getCityName());
            }

            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode = selectdProvince.getProvinceCode();
            String address=strHttp+"/"+provinceCode;
            Log.e(TAG,"address:"+address);
            queryFromServer(address,"city");
        }
    }

    //查询选中市内所有的县,优先从数据库查询,如果没有查询到再去服务器上查询
    private void queryCounties(){
        titleTv.setText(selectdCity.getCityName());
        backBtn.setVisibility(View.VISIBLE);

        countyList=DataSupport.where("cityid=?",String.valueOf(selectdCity.getId())).find(County.class);
        if (countyList.size()>0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int provinceCode = selectdProvince.getProvinceCode();
            int cityCode = selectdCity.getCityCode();
            String address=strHttp+"/"+provinceCode+"/"+cityCode;

            queryFromServer(address,"county");
        }
    }

    //根据传入的地址和类型从服务器上查询省市县数据
    private void queryFromServer(String strUrl, final String type) {
        showProgressDialog();
        HttpUtil.sendOkhttpRequest(strUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                boolean resulet=false;
                if (TextUtils.equals("province",type)){
                    resulet=Utility.handleProvinceResponse(responseString);
                }else if (TextUtils.equals("city",type)){
                    resulet=Utility.handleCityResponse(responseString,selectdProvince.getId());
                }else if (TextUtils.equals("county",type)){
                    resulet=Utility.handleCountyResponse(responseString,selectdCity.getId());
                }

                if (resulet){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if (TextUtils.equals("province",type)){
                                queryProvinces();
                            } else if (TextUtils.equals("city",type)) {
                                queryCities();
                            }else if (TextUtils.equals("county",type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }
        });
    }

    private void closeProgressDialog() {
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

}
