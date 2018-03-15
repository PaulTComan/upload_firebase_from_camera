package com.mytrendin.firebaseimageupload;

//User has to be the same as the first folder
public class Parametrii_baza_de_date {
    private String Numele,URL,Sesizarea;

    public Parametrii_baza_de_date() {
    }

    public Parametrii_baza_de_date(String numele, String imgURL, String sesizarea) {

        Numele = numele;
        URL=imgURL;
        Sesizarea=sesizarea;
    }

    public String getNumele() {
        return Numele;
    }

    public void setNumele(String numele) {
        Numele = numele;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getSesizarea() {
        return Sesizarea;
    }

    public void setSesizarea(String sesizarea) {
        Sesizarea = sesizarea;
    }
}

