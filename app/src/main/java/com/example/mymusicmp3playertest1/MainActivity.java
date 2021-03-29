package com.example.mymusicmp3playertest1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView ivAlbum;
    private TextView tvPlayCount,tvTitle,tvArtist,tvCurrentTime,tvDuration;
    private ImageButton ibLike,ibPlay,ibPrevious,ibNext;
    private SeekBar seekBar;
    private ListView listView,listViewLike;
    //MusicArrayList
    private ArrayList<MusicData> musicDataArrayList = new ArrayList<>();
    private ArrayList<MusicData> likeArrayList = new ArrayList<>();
    private int index;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private MusicData musicData;
    private DrawerLayout drawerLayout;
    private MusicAdapter musicAdapter;
    private MusicDBHelper musicDBHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewByIdFunc();
        requestPermissionsFunc();
        //MP3 데이터를 저장하는 arrayList.
        //musicDataArrayList = findMusic();
        //Log.d("aaaaa" , ""+musicDataArrayList.size());


        //DB 생성.
        musicDBHelper = MusicDBHelper.getInstance(getApplicationContext());

        //음악 리스트 가져오기.
        musicDataArrayList = musicDBHelper.compareArrayList();
        Log.d("aaaaa" , "========="+musicDataArrayList.size());

        //음악 DB 저장.
        insertDB(musicDataArrayList);
        //MusicAdapter 객체 생성.
        musicAdapter = new MusicAdapter(getApplicationContext(),musicDataArrayList);
        //listView에 adapter를 셋팅.
        listView.setAdapter(musicAdapter);


        //음악소리 나오게 해주는 모듈.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                setPlayerData(position, true);
                drawerLayout.closeDrawer(Gravity.LEFT);

            }
        });

    }

    //외부파일 접근 허용 모듈.
    private void requestPermissionsFunc() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MODE_PRIVATE);
    }

    //id 찾기.
    private void findViewByIdFunc() {
        ivAlbum = findViewById(R.id.ivAlbum);
        tvPlayCount = findViewById(R.id.tvPlayCount);
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvDuration = findViewById(R.id.tvDuration);
        ibLike = findViewById(R.id.ibLike);
        ibPlay = findViewById(R.id.ibPlay);
        ibPrevious = findViewById(R.id.ibPrevious);
        ibNext = findViewById(R.id.ibNext);
        seekBar = findViewById(R.id.seekBar);
        listView = findViewById(R.id.listView);
        listViewLike = findViewById(R.id.listViewLike);
        drawerLayout = findViewById(R.id.drawerLayout);

        ibPlay.setOnClickListener(this);
        ibPrevious.setOnClickListener(this);
        ibNext.setOnClickListener(this);
        ibLike.setOnClickListener(this);
        seekBarChangeMethod();
    }


    //DB에 MP3파일 삽입
    private void insertDB(ArrayList<MusicData> arrayList){
        boolean retureValue = musicDBHelper.insertMusicDataToDB(arrayList);
        if (retureValue){
            Toast.makeText(getApplicationContext(), "삽입성공", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "삽입실패", Toast.LENGTH_SHORT).show();
        }

    }

    //버튼 클릭 이벤트처리 함수.
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.ibPlay :
                if(ibPlay.isActivated()){
                    mediaPlayer.pause();
                    ibPlay.setActivated(false);
                }else{
                    mediaPlayer.start();

                    setSeekBarThread();
                }
                break;
            case R.id.ibPrevious :
                mediaPlayer.stop();
                mediaPlayer.reset();
                try {
                    if(index == 0){
                        index = musicDataArrayList.size();
                    }
                    index--;
                    setPlayerData(index, true);

                } catch (Exception e) {
                    Log.d("ubPrevious",e.getMessage());
                }
                break;
            case R.id.ibNext :
                try {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    if(index == musicDataArrayList.size()-1){
                        index= -1;
                    }
                    index++;
                    setPlayerData(index, true);

                } catch (Exception e) {
                    Log.d("ibNext",e.getMessage());
                }
                break;

            case R.id.ibLike :

                if(ibLike.isActivated()){
                    ibLike.setActivated(false);
                    musicData.setLiked(0);
                    likeArrayList.remove(musicData);
                    musicAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplication(), "좋아요 취소!!", Toast.LENGTH_SHORT).show();

                }else{
                    ibLike.setActivated(true);
                    musicData.setLiked(1);
                    likeArrayList.add(musicData);
                    musicAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplication(), "좋아요!!", Toast.LENGTH_SHORT).show();
                }
                break;
            default:break;
        }
    }




    //sdCard 안의 음악을 검색.
    private ArrayList<MusicData> findMusic() {
        ArrayList<MusicData> sdCardList = new ArrayList<>();

        String[] data = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};

        // 특정 폴더에서 음악 가져오기
//        String selection = MediaStore.Audio.Media.DATA + " like ? ";
//        String selectionArqs = new String[]{"%MusicList%"}

        // 전체 영역에서 음악 가져오기.
        Cursor cursor = getApplicationContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                data, null, null, data[2] + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {

                // 음악 데이터 가져오기.
                String id = cursor.getString(cursor.getColumnIndex(data[0]));
                String artist = cursor.getString(cursor.getColumnIndex(data[1]));
                String title = cursor.getString(cursor.getColumnIndex(data[2]));
                String albumArt = cursor.getString(cursor.getColumnIndex(data[3]));
                String duration = cursor.getString(cursor.getColumnIndex(data[4]));

                MusicData mData = new MusicData(id, artist, title, albumArt, duration, 0, 0);

                sdCardList.add(mData);
            }
        }
        return sdCardList;
    }

    //리스트뷰에서 아이템을 선택하면 해당된 위치와 좋아요 음악(false), 일반음악(true)인지 선택 내용이 온다.
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setPlayerData(int position, boolean flag) {
        index = position;

        mediaPlayer.stop();
        mediaPlayer.reset();

        if(flag == true){
            musicData=musicDataArrayList.get(index);
        }else{

        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

        tvTitle.setText(musicData.getTitle());
        tvArtist.setText(musicData.getArtist());
        tvPlayCount.setText(String.valueOf(musicData.getPlayCount()));
        tvDuration.setText(simpleDateFormat.format(Integer.parseInt(musicData.getDuration())));

        if(musicData.getLiked() == 1){
            ibLike.setActivated(true);
        }else{
            ibLike.setActivated(false);
        }

        // 앨범 이미지 셋팅.
        Bitmap albumImg = getAlbumImg(Long.parseLong(musicData.getAlbumArt()), 200);
        if(albumImg != null){
            ivAlbum.setImageBitmap(albumImg);
        }else{
            ivAlbum.setImageResource(R.drawable.album_default);
        }

        // 음악 play.
        Uri musicURI = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,musicData.getId());
        try {
            mediaPlayer.setDataSource(getApplicationContext(), musicURI);
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(Integer.parseInt(musicData.getDuration()));
            ibPlay.setActivated(true);

            setSeekBarThread();

            //한곡의 노래를 완료했을 때 발생하는 이벤트리스너.
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    musicData.setPlayCount(musicData.getPlayCount() + 1);
                    ibNext.callOnClick();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //앨범사진 아이디와 앨범사이즈를 부여한다.
    private Bitmap getAlbumImg(long albumArt, int imgMaxSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        /*컨텐트 프로바이더(Content Provider)는 앱 간의 데이터 공유를 위해 사용됨.
        특정 앱이 다른 앱의 데이터를 직접 접근해서 사용할 수 없기 때문에
        무조건 컨텐트 프로바이더를 통해 다른 앱의 데이터를 사용해야만 한다.
        다른 앱의 데이터를 사용하고자 하는 앱에서는 URI를 이용하여 컨텐트 리졸버(Content Resolver)를 통해
        다른 앱의 컨텐트 프로바이더에게 데이터를 요청하게 되는데
        요청받은 컨텐트 프로바이더는 URI를 확인하고 내부에서 데이터를 꺼내어 컨텐트 리졸버에게 전달한다.
        */
        ContentResolver contentResolver = getApplicationContext().getContentResolver();

        // 앨범아트는 uri를 제공하지 않으므로, 별도로 생성.
        Uri uri = Uri.parse("content://media/external/audio/albumart/"+albumArt);
        if (uri != null){
            ParcelFileDescriptor fd = null;
            try{
                fd = contentResolver.openFileDescriptor(uri, "r");

                //true면 비트맵객체에 메모리를 할당하지 않아서 비트맵을 반환하지 않음.
                //다만 options fields는 값이 채워지기 때문에 Load 하려는 이미지의 크기를 포함한 정보들을 얻어올 수 있다.
                //93번 문항부터 98번까지는 체크안해도 되는 문장임. options.inJustDecodeBounds = false; 앞문장까지

                options.inJustDecodeBounds = false; // false 비트맵을 만들고 해당이미지의 가로, 세로, 중심으로 가져옴
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, options);

                if(bitmap != null){
                    // 정확하게 사이즈를 맞춤
                    if(options.outWidth != imgMaxSize || options.outHeight != imgMaxSize){
                        Bitmap tmp = Bitmap.createScaledBitmap(bitmap, imgMaxSize, imgMaxSize, true);
                        bitmap.recycle();
                        bitmap = tmp;
                    }
                }
                return bitmap;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }//end of  getAlbumImg

    //setSeekBarThread에 관한 함수.
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setSeekBarThread(){
        Thread thread = new Thread(new Runnable() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

            @Override
            public void run() {
                while(mediaPlayer.isPlaying()){
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvCurrentTime.setText(simpleDateFormat.format(mediaPlayer.getCurrentPosition()));
                        }
                    });
                    SystemClock.sleep(100);
                }
            }
        });
        thread.start();
    }

    //setSeekBar 변경에 관한 함수.
    private void seekBarChangeMethod() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // 사용자가 움직였을시, seekbar 이동.
                if(b){
                    mediaPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }





}