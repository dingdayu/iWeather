package com.dingxiaoyu.iweather.web;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dingxiaoyu.iweather.Weather;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by dingdayu on 2016/10/18.
 */

public class CaiyunWeather {
    private String uri;
    protected String TAG = "CaiyunWeather";
    // 请填写你的彩云挑起token
    protected String CaiyunToken = "nGdiAEmrjzF7yJu6";
    // 请填写你的高德WEB KEY
    protected String GaodeKEY = "71e31da1d901eede04a8f2d09a0c6ea0";

    protected OkHttpClient mOkHttpClient = new OkHttpClient();

    public void getLocation(String city)
    {
        try {
            String location = geo(city);
            get(location);
        } catch (Exception e)
        {
            returnsError();
        }
    }

    public void get(String location) {
        uri = "https://api.caiyunapp.com/v2/" + CaiyunToken + "/" + location +"/forecast.json";

        String content;

        //创建一个Request
        final Request request = new Request.Builder()
                .url(uri)
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);
        //请求加入调度
        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(Request request, IOException e)
            {
                Log.i("Caiyun", e.getMessage());
                returnsError();
            }

            @Override
            public void onResponse(final Response response) throws IOException
            {
                try {
                    String htmlStr =  response.body().string();
                    JSONObject jsonObject = JSON.parseObject(htmlStr);
                    returns(jsonObject);
                } catch (IOException e)
                {
                    returnsError();;
                }

            }
        });

    }

    private String geo(String city) throws IOException
    {
        String content;

        String uri = "http://restapi.amap.com/v3/geocode/geo?key=" + GaodeKEY + "&address=" + city ;
        //创建一个Request
        final Request request = new Request.Builder()
                .url(uri)
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);

        Response execute = call.execute();
        content = execute.body().string();
        JSONObject jsonObject = JSON.parseObject(content);

        String status = jsonObject.getString("status");

        if(status == "0") {
            return "116.407526,39.904030";
        } else {
            JSONArray geocodes = jsonObject.getJSONArray("geocodes");

            String location = geocodes.getJSONObject(0).getString("location");

            return location;
        }
    }

    private String regeo(String location) throws IOException
    {
        String content;

        String uri = "http://restapi.amap.com/v3/geocode/regeo?key=" + GaodeKEY + "&location=" + location + "&poitype=&radius=&extensions=base&batch=false&roadlevel=";
        Log.i(TAG, "regeo :uri" + uri);
        //创建一个Request
        final Request request = new Request.Builder()
                .url(uri)
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);

        Response execute = call.execute();
        content = execute.body().string();
        JSONObject jsonObject = JSON.parseObject(content);

        String status = jsonObject.getString("status");
        String info = jsonObject.getString("info");
        Log.i(TAG, "regeo :status" + status + ",info:" + info);

        if(status == "0") {
            return "未知";
        } else {
            JSONObject regeocode = jsonObject.getJSONObject("regeocode");

            String country = regeocode.getJSONObject("addressComponent").getString("country");
            String province = regeocode.getJSONObject("addressComponent").getString("province");
            String city = regeocode.getJSONObject("addressComponent").getString("city");
            String district = regeocode.getJSONObject("addressComponent").getString("district");

            if(district != null) {
                return district;
            } else if (city != null){
                return city;
            } else if (province != null) {
                return province;
            } else {
                return country;
            }
        }
    }

    private void returnsError()
    {
        Message msg = Weather.handler.obtainMessage();
        msg.what = 0;
        Weather.handler.sendMessage(msg);
    }

    private void returns(JSONObject jsonObject)
    {
        String status = jsonObject.getString("status");
        Message msg = new Message();
        Log.i(TAG, status);
        if ("ok".equals(status)) {
            // 查询成功
            msg.what = 1;
            String city = "";
            try {
                String loction = jsonObject.getJSONArray("location").getString(1)
                        + "," + jsonObject.getJSONArray("location").getString(0);

                city = regeo(loction);
                Log.i(TAG , "city ：" + loction + "," + city);
            } catch (Exception e) {
                Log.i(TAG , "city regeo error！");
            }

            JSONObject result = jsonObject.getJSONObject("result");
            // 分钟级别数据
            JSONObject minutely = result.getJSONObject("minutely");
            // 小时级别数据
            JSONObject hourly = result.getJSONObject("hourly");
            // 日级别数据
            JSONObject daily = result.getJSONObject("daily");

            // 日级别天气
            JSONArray skycon = daily.getJSONArray("skycon");
            JSONArray wind = daily.getJSONArray("wind");
            JSONArray temperature = daily.getJSONArray("temperature");

            // 分钟级别描述
            String description = minutely.getString("description");
            // 当前温度
            String currentTemperature = hourly.getJSONArray("temperature").getJSONObject(0).getString("value");


            String[] dateArray = {"明天","后天","第三天","第四天"};
            String[] weatherArray = new String[5];
            String[] windArray = new String[5];
            String[] temperatureArray = new String[5];

            // 处理天气
            for (int i = 1; i < skycon.size(); i++) {
                weatherArray[i-1] = switchSkycon(skycon.getJSONObject(i).getString("value"));
            }

            for (int i = 1; i < wind.size(); i++) {
                windArray[i-1] = switchWind(wind.getJSONObject(i));
            }

            for (int i = 1; i < skycon.size(); i++) {
                temperatureArray[i-1] = switchTemperature(temperature.getJSONObject(i));
            }

            Bundle bundle = new Bundle();
            bundle.putStringArray("date", dateArray);
            bundle.putStringArray("weather", weatherArray);
            bundle.putStringArray("wind", windArray);
            bundle.putStringArray("temperature", temperatureArray);
            bundle.putString("city", city);


            bundle.putString("description", description);
            bundle.putString("current_temperature", currentTemperature);
            Log.i(TAG , bundle.toString());
            msg.setData(bundle);
        } else if ("No result available".equals(status)) {
            // 没有天气信息
            msg.what = 2;
        } else {
            // 其他错误
            msg.what = 0;
        }
        Weather.handler.sendMessage(msg);
    }



    private String switchTemperature(JSONObject temperature)
    {
        float max = temperature.getFloat("max");
        float min = temperature.getFloat("min");
        float avg = temperature.getFloat("avg");

        return avg + " ℃";
    }

    private String switchWind(JSONObject wind)
    {

        JSONObject avg = wind.getJSONObject("avg");

        // 风向
        float direction = avg.getFloat("direction");
        // 风力
        float speed = avg.getFloat("speed");

        if(speed <= 0.02) {
            return "无风";
        }
        if(speed <= 1.5) {
            return "软风";
        }
        if(speed <= 3.3) {
            return "轻风";
        }
        if(speed <= 5.4) {
            return "微风";
        }
        if(speed <= 7.9) {
            return "和风";
        }
        if(speed <= 10.7) {
            return "轻劲风";
        }
        if(speed <= 13.8) {
            return "强风";
        }
        if(speed <= 17.1) {
            return "疾风";
        }
        if(speed <= 20.7) {
            return "大风";
        }
        if(speed <= 24.4) {
            return "烈风";
        }
        if(speed <= 28.4) {
            return "狂风";
        }
        if(speed <= 32.6) {
            return "暴风";
        }
        if(speed <= 36.9) {
            return "台风";
        }
        return "飓风";
    }

    private String switchSkycon(String skycon)
    {
        String ret = "未知";

        if("CLEAR_DAY".equals(skycon)) {
            ret = "晴天";
        }
        if("CLEAR_NIGHT".equals(skycon)) {
            ret = "晴夜";
        }
        if("PARTLY_CLOUDY_DAY".equals(skycon)) {
            ret = "多云";
        }
        if("PARTLY_CLOUDY_NIGHT".equals(skycon)) {
            ret = "多云";
        }
        if("CLOUDY".equals(skycon)) {
            ret = "阴";
        }
        if("RAIN".equals(skycon)) {
            ret = "雨";
        }
        if("SLEET".equals(skycon)) {
            ret = "冻雨";
        }
        if("SNOW".equals(skycon)) {
            ret = "雪";
        }
        if("WIND".equals(skycon)) {
            ret = "风";
        }
        if("FOG".equals(skycon)) {
            ret = "雾";
        }
        if("HAZE".equals(skycon)) {
            ret = "霾";
        }

        return ret;
//
//        switch (skycon) {
//            case "CLEAR_DAY" :
//                ret = "晴天";
//                break;
//            case "CLEAR_NIGHT" :
//                ret = "晴夜";
//                break;
//            case "PARTLY_CLOUDY_DAY" :
//                ret = "多云";
//                break;
//            case "PARTLY_CLOUDY_NIGHT" :
//                ret = "多云";
//                break;
//            case "CLOUDY" :
//                ret = "阴";
//                break;
//            case "RAIN" :
//                ret = "雨";
//                break;
//            case "SLEET" :
//                ret = "冻雨";
//                break;
//            case "SNOW" :
//                ret = "雪";
//                break;
//            case "WIND" :
//                ret = "风";
//                break;
//            case "FOG" :
//                ret = "雾";
//                break;
//            case "HAZE" :
//                ret = "霾";
//                break;
//            default:
//                ret = "未知";
//        }
//        return ret;
    }
}
