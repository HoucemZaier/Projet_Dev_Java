package com.PlaNova.services;

import javax.sound.sampled.*;
import java.io.*;

public class VoiceService {
    private AudioFormat format;
    private TargetDataLine line;
    private File audioFile;

    public VoiceService() {
    
        format = new AudioFormat(16000, 16, 1, true, false);
    }

    public void startRecording(String fileName) throws LineUnavailableException {
        audioFile = new File(fileName);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Microphone not supported");
        }

        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        Thread recorder = new Thread(() -> {
            try (AudioInputStream ais = new AudioInputStream(line)) {
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, audioFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        recorder.start();
        System.out.println("[DEBUG] Recording started...");
    }

    public void stopRecording() {
        if (line != null) {
            line.stop();
            line.close();
            System.out.println("[DEBUG] Recording stopped. Saved to: " + audioFile.getAbsolutePath());
        }
    }

    public File getAudioFile() {
        return audioFile;
    }
}
