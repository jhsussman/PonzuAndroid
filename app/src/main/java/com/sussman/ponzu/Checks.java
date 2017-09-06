package com.sussman.ponzu;

public class Checks {
    String name;
    int value;
    Checks(String name, int value){
        this.name = name;
        this.value = value;
    }
    public String getName(){
        return this.name;
    }
    public int getValue(){
        return this.value;
    }
    public void setChecked(int num){
        num=value;
    }
    public void toggleChecked(){
        if(value==0){
            value=1;
        }else if(value==1){
            value=0;
        }
    }

}
