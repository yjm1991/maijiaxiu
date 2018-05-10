package com.maijiaxiu.yjm;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedList;

/**
 * Created by hosson on 08/03/2018.
 */

public class SharedPreferenceManager {

    private SharedPreferences mSharedPreferences;

    public SharedPreferenceManager(Context context, String spName){
        if(TextUtils.isEmpty(spName)){
            throw new NullPointerException("sharedPreference can't be empty");
        }
        mSharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    /**
     * 存储整型
     *
     * @param key
     * @param value
     */
    public void saveToPreference(String key, int value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * 存储long 型
     *
     * @param key
     * @param value
     */
    public void saveToPreference(String key, long value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    /**
     * 存储布尔值
     *
     * @param key
     * @param value
     */
    public void saveToPreference(String key, boolean value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * 存储字符串型
     *
     * @param key
     * @param value
     */
    public void saveToPreference(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void remove(String key){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * 存储对象
     *
     * @param key
     * @param t
     * @param <T>
     */
    public <T> void saveToPreference(String key, T t) {
        if (t != null) {
            try {
                Gson gson = new Gson();
                String result = gson.toJson(t);
                saveToPreference(key, result);
            } catch (Exception ex) {
                ex.printStackTrace();

            }
        }
    }

    /**
     * 获取布尔类型的值
     *
     * @param paramString
     * @return
     */
    public boolean gainBooleanValue(String paramString) {
        return this.mSharedPreferences.getBoolean(paramString, false);
    }

    /**
     * 获取整型的值
     *
     * @param paramString
     * @return
     */
    public int gainIntValue(String paramString) {
        return this.mSharedPreferences.getInt(paramString, -1);
    }

    /**
     * 获取long的值
     *
     * @param paramString
     * @return
     */
    public long gainLongValue(String paramString) {
        return this.mSharedPreferences.getLong(paramString, -1);
    }

    /**
     * 获取字符串型的值
     *
     * @param paramString
     * @return
     */
    public String gainStringValue(String paramString) {
        return this.mSharedPreferences.getString(paramString, "");
    }

    /**
     * 获取对象
     *
     * @param key
     * @param <T>
     */
    public <T> T gainTValue(String key, Class<T> t) {
        try {
            String result = gainStringValue(key);
            Gson gson = new Gson();
            return gson.fromJson(result, t);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 获取集合
     *
     * @param key
     * @param <T>
     */
    public <T> LinkedList<T> gainLinkedList(String key, Class<T> tClass) {
        try {
            Gson gson = new Gson();
            String result = gainStringValue(key);
            Type collectionType = new TypeToken<LinkedList<JsonObject>>(){}.getType();
            LinkedList<JsonObject> jsonObjects = gson.fromJson(result, collectionType);
            LinkedList<T> resultList = new LinkedList<>();
            if(jsonObjects != null){
                for(JsonObject jsonObject : jsonObjects){
                    resultList.add(gson.fromJson(jsonObject, tClass));
                }
            }
            return resultList;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
