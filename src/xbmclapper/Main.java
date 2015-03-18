/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xbmclapper;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author josh
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        XBMCRemote remote = new XBMCRemote("192.168.0.16", 8080);
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

        
        remote.help();
        boolean inClap = false;
        double clap_threshold = 0.9;
        int comboThreshold = 500; //500 milli seconds
        long time = System.currentTimeMillis();
        int noClap = 0;

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

            float peak = 0f;
            for (float sample : samples) {
                float abs = Math.abs(sample);
                if (abs > peak) {
                    peak = abs;
                }
            }

            if (lastPeak > peak) {
                peak = lastPeak * 0.875f;
            }

            //We have to use inClap boolean to wait for clap_threshold peak to be reduced so it won't detect it as 2 clap_thresholds
            if (!inClap) {
                //clap_threshold
                if (peak > clap_threshold) {
                    //how long to wait after each clap for another
                    //if (noClap == 0) {
                    time = System.currentTimeMillis();
                    //}
                    noClap++;
                    inClap = true;
                }
            } else {
                //we are inClap, check if peak has reduced yet...
                //below 80% of theshold
                if (peak < (clap_threshold * 0.8)) {
                    inClap = false;
                }
            }

            if (System.currentTimeMillis() - time > comboThreshold && noClap != 0) {
                //more function to be added
                System.out.println("You just clapped " + noClap + " times");
                switch (noClap) {
                    case 1: //next
                        System.out.println("Next episode");
                        remote.next();
                        break;
                    case 2: //play / pause
                        System.out.println("Play/Pause..");
                        remote.PlayPause();
                        break;
                    case 3: //volume up
                        System.out.println("Volume up");
                        remote.decVolume();
                        break;
                    case 4: //volume down
                        System.out.println("Volume down");
                        remote.incVolume();
                        break;
                    case 5: //home
                        System.out.println("Home");
                        remote.home();
                }
                noClap = 0;
            }

            lastPeak = peak;

        }
    }

}
