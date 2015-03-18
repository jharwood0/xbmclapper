/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xbmclapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author josh
 */
public class XBMCRemote {

    private final String host;
    private final int port;
    private final String rpc = "jsonrpc?request=";
    private final int incdecmultiplier = 10;

    private String request(String urlToRead) {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        String result = "";
        try {
            url = new URL(urlToRead);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public XBMCRemote(String host, int port) {
        if (host.contains("http://")) {
            this.host = host;
        } else {
            this.host = "http://" + host;
        }
        this.port = port;
    }

    public void PlayPause() {
        String PlayPauseJson = "{\"jsonrpc\":%20\"2.0\",%20\"method\":%20\"Player.PlayPause\",%20\"params\":%20{%20\"playerid\":%201%20},%20\"id\":%201}";
        request(host + ":" + port + "/" + rpc + PlayPauseJson);
    }

    public void incVolume() {
        String incVolume = "{%20\"jsonrpc\":%20\"2.0\",%20\"method\":%20\"Application.SetVolume\",%20\"params\":%20{%20\"volume\":%20\"increment\"%20},%20\"id\":%201%20}";
        for (int i = 0; i < incdecmultiplier; i++) {
            request(host + ":" + port + "/" + rpc + incVolume);
        }
    }

    public void decVolume() {
        String decVolume = "{%20\"jsonrpc\":%20\"2.0\",%20\"method\":%20\"Application.SetVolume\",%20\"params\":%20{%20\"volume\":%20\"decrement\"%20},%20\"id\":%201%20}";
        for (int i = 0; i < incdecmultiplier; i++) {
            request(host + ":" + port + "/" + rpc + decVolume);
        }

    }

    public void next() {
        //check if playing
        String check = "{\"jsonrpc\":%20\"2.0\",%20\"method\":%20\"Player.GetActivePlayers\",%20\"id\":%201}";
        String out = request(host + ":" + port + "/" + rpc + check);
        //bit of a hack for now..
        if (!out.contains("video")) {
            this.down();
            this.enter();
        } else {
            System.out.println("Skipping because already playing...");
        }
    }

    public void down() {
        String down = "{\"jsonrpc\":%20\"2.0\",%20\"method\":%20\"Input.Down\",%20\"id\":%201}";
        request(host + ":" + port + "/" + rpc + down);

    }

    public void enter() {
        String enter = "{\"jsonrpc\":%20\"2.0\",%20\"method\":%20\"Input.Select\",%20\"id\":%201}";
        request(host + ":" + port + "/" + rpc + enter);
    }

    void home() {
        String home = "{\"jsonrpc\":%20\"2.0\",%20\"method\":%20\"Input.Home\",%20\"id\":%201}";
        request(host + ":" + port + "/" + rpc + home);
    }

    void help() {
        String help = "{%20\"jsonrpc\":%20\"2.0\",%20\"method\":%20\"GUI.ShowNotification\",%20\"params\":%20{%20\"title\":%20\"Welcome%20to%20XBMClapper%200.1!\",%20\"message\":%20\"1%20=%20Next,%202%20=%20PlayPause,%203%20=%20Volume%20Up,%204%20=%20Volume%20Down,%205%20=%20Home\"%20},%20\"id\":%201%20}";
        request(host + ":" + port + "/" + rpc + help);
    }

}
