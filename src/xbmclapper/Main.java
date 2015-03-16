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
        AudioFormat fmt = new AudioFormat(44100f, 16, 1, true, false);
            final int bufferByteSize = 2048;

            TargetDataLine line;
            try {
                line = AudioSystem.getTargetDataLine(fmt);
                line.open(fmt, bufferByteSize);
            } catch(LineUnavailableException e) {
                System.err.println(e);
                return;
            }

            byte[] buf = new byte[bufferByteSize];
            float[] samples = new float[bufferByteSize / 2];

            float lastPeak = 0f;

            line.start();
            for(int b; (b = line.read(buf, 0, buf.length)) > -1;) {
                
                // convert bytes to samples here
                for(int i = 0, s = 0; i < b;) {
                    int sample = 0;

                    sample |= buf[i++] & 0xFF; // (reverse these two lines
                    sample |= buf[i++] << 8;   //  if the format is big endian)

                    // normalize to range of +/-1.0f
                    samples[s++] = sample / 32768f;
                }

                float rms = 0f;
                float peak = 0f;
                for(float sample : samples) {

                    float abs = Math.abs(sample);
                    if(abs > peak) {
                        peak = abs;
                    }

                    rms += sample * sample;
                }

                rms = (float)Math.sqrt(rms / samples.length);

                if(lastPeak > peak) {
                    peak = lastPeak * 0.875f;
                }

                lastPeak = peak;
                if(peak > 0.8){
                    System.out.println("You Just clapped!\n");
                }

            }
    }
    
}
