package br.com.openedu.model;

import com.mongodb.BasicDBObject;

public class Location extends BasicDBObject{
    
    public String getAddress(){
        return getString("address");
    }
    
    public void setAddress(String address){
        super.put("address", address);
    }
    
    public String getCity(){
        return getString("city");
    }
    
    public void setCity(String city){
        super.put("city", city);
    }
    
    public String getState(){
        return getString("state");
    }
    
    public void setState(String state){
        super.put("state", state);
    }
    
    public String getCountry(){
        return getString("country");
    }
    
    public void setCountry(String country){
        super.put("country", country);
    }
}
