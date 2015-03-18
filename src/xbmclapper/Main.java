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
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author josh
 */
public class Main {

    
    private static void request(){
        String urlToRead = "http://192.168.0.16:8080/jsonrpc?request={%22jsonrpc%22:%20%222.0%22,%20%22method%22:%20%22Player.PlayPause%22,%20%22params%22:%20{%20%22playerid%22:%201%20},%20%22id%22:%201}";
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
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        AudioFormat fmt = new AudioFormat(44100f, 16, 1, true, false);
        final int bufferByteSize = 2048;

        TargetDataLine line;
        try {
            line = AudioSystem.getTargetDataLine(fmt);
            line.open(fmt, bufferByteSize);
        } catch (LineUnavailableException e) {
            System.err.println(e);
            return;
        }

        byte[] buf = new byte[bufferByteSize];
        float[] samples = new float[bufferByteSize / 2];

        float lastPeak = 0f;
        
        boolean inClap = false;
        double clap = 0.8;

        line.start();
        for (int b; (b = line.read(buf, 0, buf.length)) > -1;) {

            // convert bytes to samples here
            for (int i = 0, s = 0; i < b;) {
                int sample = 0;

                sample |= buf[i++] & 0xFF; // (reverse these two lines
                sample |= buf[i++] << 8;   //  if the format is big endian)

                // normalize to range of +/-1.0f
                samples[s++] = sample / 32768f;
            }

            float rms = 0f;
            float peak = 0f;
            for (float sample : samples) {

                float abs = Math.abs(sample);
                if (abs > peak) {
                    peak = abs;
                }

                rms += sample * sample;
            }

            rms = (float) Math.sqrt(rms / samples.length);

            if (lastPeak > peak) {
                peak = lastPeak * 0.875f;
            }

            lastPeak = peak;

            if (!inClap) {
                if (peak > clap) {
                    System.out.println("You Just clapped!\n");
                    System.out.println("Sending:\nhttp://82.45.32.120:8080/jsonrpc?request={%22jsonrpc%22:%20%222.0%22,%20%22id%22:%201,%20%22method%22:%20%22Playlist.GetPlaylists%22}");
                    request();
                    inClap = true;
                }
            } else {
                System.out.println("We are in a clap!");
                System.out.println("rms = "+rms);
                System.out.println("peak = "+peak);
                if (peak < (clap*0.9)) {
                    System.out.println("We are out of a clap!\n");
                    inClap = false;
                    //we have finsehd the clap
                }

            }

        }
    }

}
