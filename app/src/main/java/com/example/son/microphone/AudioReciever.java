package com.example.son.microphone;

        import android.app.Activity;
        import android.app.ProgressDialog;
        import android.media.AudioFormat;
        import android.media.AudioManager;
        import android.media.AudioRecord;
        import android.media.AudioTrack;
        import android.media.MediaRecorder;
        import android.os.Bundle;
        import android.os.Environment;
        import android.util.Log;
        import android.view.KeyEvent;
        import android.view.View;
        import android.widget.Button;

        import com.example.microphone.R;

        import java.io.BufferedInputStream;
        import java.io.BufferedOutputStream;
        import java.io.DataInputStream;
        import java.io.DataOutputStream;
        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
        import java.util.Timer;
        import java.util.TimerTask;

public class AudioReciever extends Activity {
    public final static String AUDIO_RECORDING = "Audio recording. Please wait... ";
    public final static String AUDIO_RECORDING_STOP = "Audio recording stop";
    public final static String AUDIO_PLAYING = "Audio playing. Please wait... ";
    public final static String AUDIO_PLAYING_STOP = "Audio playing stop";
    public final static String AUDIO_REMOVING_FILES = ".pcm files removing from SDCard... ";
    public final static String AUDIO_REMOVING_FILES_DONE = "Operation done";
    private final static String AUDIO_FILE_PREFIX = "testAudio";
    final String TAG = "myLogs";
    public boolean recording;
    public boolean playing;
    public boolean cleaning;
    public ProgressDialog pd;
    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we
    // use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format
    boolean isReading = false;
    Timer timer;
    Thread recordThread;
    Thread playThread;
    Thread clearThread;
    AudioRecord audioRecord;
    String random;
    int counter = 0;
    File audioFile = null;
    AudioTrack audioTrack;
    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.record: {

                    progressDialogInitializator(AUDIO_RECORDING);


                    recordThread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            startRecord();

                        }

                    });

                    recordThread.start();


                    break;
                }

                case R.id.play: {
                    progressDialogInitializator(AUDIO_PLAYING);
                    playThread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            playRecord();

                            pd.dismiss();
                        }

                    });

                    playThread.start();

                    break;

                }

                case R.id.clear: {
                    progressDialogInitializator(AUDIO_REMOVING_FILES);

                    clearThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(4000);
                                clearAllFiles();
                                pd.dismiss();

                            } catch (Exception e) {
                                Log.e("threadmessage", e.getMessage());
                            }


                        }

                    });

                    clearThread.start();

                    break;

                }
            }
        }
    };

    public void onCreate(Bundle main) {
        super.onCreate(main);
        setContentView(R.layout.activity_main);
        Button record = (Button) findViewById(R.id.record);
        record.setOnClickListener(btnClick);
        Button play = (Button) findViewById(R.id.play);
        play.setOnClickListener(btnClick);
        Button clearAll = (Button) findViewById(R.id.clear);
        clearAll.setOnClickListener(btnClick);

    }

    ProgressDialog progressDialogInitializator(String toastMessage) {

        pd = new ProgressDialog(this);
        pd.setTitle(toastMessage);
        pd.show();

        return pd;

    }

    private void startRecord() {

        if (recording) {
            return;
        }
        recording = true;
        timerExecute();

        if (audioFile != null && audioFile.exists()) {
            audioFile.delete();
        }

        File audioFile = createAudioFile();

        try {

            OutputStream outputStream = new FileOutputStream(audioFile);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                    outputStream);
            DataOutputStream dataOutputStream = new DataOutputStream(
                    bufferedOutputStream);

            int minBufferSize = AudioRecord.getMinBufferSize(8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            short[] audioData = new short[minBufferSize];

            initializeAudioTrack(minBufferSize);

            while (recording) {
                int numberOfShort = audioRecord.read(audioData, 0,
                        minBufferSize);
                for (int i = 0; i < numberOfShort; i++) {
                    dataOutputStream.writeShort(audioData[i]);
                }
            }

            audioRecord.stop();
            audioRecord.release();
            dataOutputStream.close();

            Log.i("microphone", "Record stopping");


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void timerExecute() {

        timer = new Timer();
        Log.i("microphone", "Timer start");
        MyTimerTask myTask = new MyTimerTask();
        timer.schedule(myTask, 10000);

    }

    private File createAudioFile() {
        String construct = "";
        counter++;
        construct = AUDIO_FILE_PREFIX + counter + ".pcm";
        File file = new File(Environment.getExternalStorageDirectory(),
                construct);
        audioFile = file;
        Log.i("microphone", "Audio file " + construct + " created");

        return file;
    }

    AudioTrack initializeAudioTrack(int bufferSizeInBytes, short[] audioData) {
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes,
                AudioTrack.MODE_STREAM);

        audioTrack.play();

        audioTrack.write(audioData, 0, bufferSizeInBytes);

        return audioTrack;

    }

    private void playRecord() {

        if (playing) {
            return;
        }


        if (audioFile == null || !audioFile.exists()) {
            return;
        }

        playing = true;

        int shortSizeInBytes = Short.SIZE / Byte.SIZE;

        int bufferSizeInBytes = (int) (audioFile.length() / shortSizeInBytes);
        short[] audioData = new short[bufferSizeInBytes];

        try {
            InputStream inputStream = new FileInputStream(audioFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(
                    inputStream);
            DataInputStream dataInputStream = new DataInputStream(
                    bufferedInputStream);

            int i = bufferSizeInBytes - 1;

            while (dataInputStream.available() > 0 && i > 0) {
                audioData[i] = dataInputStream.readShort();
                i--;
            }

            dataInputStream.close();

            initializeAudioTrack(bufferSizeInBytes, audioData);

            playing = false;


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    AudioRecord initializeAudioTrack(int minBufferSize) {

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize);

        while (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            // Do nothing
        }

        audioRecord.startRecording();

        return audioRecord;

    }

    void clearAllFiles() {

        if (cleaning) {
            return;
        }


        cleaning = true;

        File[] files = Environment.getExternalStorageDirectory().listFiles();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            if (fileName.startsWith(AUDIO_FILE_PREFIX) && fileName.contains(".pcm")) {
                files[i].delete();
            }
        }

        cleaning = false;

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            Log.i("microphone", "Timer stop");
            recording = false;
            if (timer != null) {
                timer.cancel();
                timer = null;

            } else {

            }

            pd.dismiss();
        }
    }

}


