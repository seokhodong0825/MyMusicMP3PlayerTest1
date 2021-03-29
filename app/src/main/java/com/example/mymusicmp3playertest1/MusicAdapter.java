package com.example.mymusicmp3playertest1;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MusicAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<MusicData> musicDataArrayList;

    public MusicAdapter(Context context, ArrayList<MusicData> musicDataArrayList) {
        this.context = context;
        this.musicDataArrayList = musicDataArrayList;
    }

    @Override
    //musicDataArrayList가 null이 아니라면 리스트안에 있는 데이터를 갖고 null이라면 0값을 준다.
    public int getCount() {
        return musicDataArrayList != null ? musicDataArrayList.size() : 0;
    }

    //musicDataArrayList의 위치를 지정.
    @Override
    public Object getItem(int position) {
        return musicDataArrayList.get(position);
    }

    //musicDataArrayList의 ID를 long타입으로 형변환.
    @Override
    public long getItemId(int position) {
        return Long.parseLong(musicDataArrayList.get(position).getId());
    }

    /*ArrayDataList 값을 5개만 만든다고 가정했을 때 위로 넘겨서 사라지는 data를 convertView에 값을 저장하고
    convertView를 retrun함으로써 밑에 새로 생성되는 data 그대로 사용한다.*/
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if (convertView == null){
            //listview에 들어가는 화면항목 객체.
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_item,parent,false);
        }

        //listview에 들어가는 화면항목 객체 아이디 찾기.
        ImageView ivAlbum = convertView.findViewById(R.id.ivAlbum);
        TextView d_tvTitle = convertView.findViewById(R.id.d_tvTitle);
        TextView d_tvArtist = convertView.findViewById(R.id.d_tvArtist);
        TextView d_tvDuration = convertView.findViewById(R.id.d_tvDuration);

        //앨범 이미지를 비트맵으로 변환.
        Bitmap albumImg = getAlbumImg(context, Long.parseLong(musicDataArrayList.get(position).getAlbumArt()), 200);
        try{
            if(albumImg != null){
                ivAlbum.setImageBitmap(albumImg);
            }
        }catch (NullPointerException e){
            Log.d("aaaaa" , e.toString());
        }

        // listviewer에 보여줘야할 정보 셋팅(경과시간)
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

        //listview들어갈 실제 data를 저장.
        d_tvTitle.setText(musicDataArrayList.get(position).getTitle());
        d_tvArtist.setText(musicDataArrayList.get(position).getArtist());
        d_tvDuration.setText(simpleDateFormat.format(Integer.parseInt(musicDataArrayList.get(position).getDuration())));

        return convertView;
    }

    // 앨범아트 가져오는 함수
    public Bitmap getAlbumImg(Context context, long albumArt, int imgMaxSize){

        BitmapFactory.Options options = new BitmapFactory.Options();

        /*컨텐트 프로바이더(Content Provider)는 앱 간의 데이터 공유를 위해 사용됨.
        특정 앱이 다른 앱의 데이터를 직접 접근해서 사용할 수 없기 때문에
        무조건 컨텐트 프로바이더를 통해 다른 앱의 데이터를 사용해야만 한다.
        다른 앱의 데이터를 사용하고자 하는 앱에서는 URI를 이용하여 컨텐트 리졸버(Content Resolver)를 통해
        다른 앱의 컨텐트 프로바이더에게 데이터를 요청하게 되는데
        요청받은 컨텐트 프로바이더는 URI를 확인하고 내부에서 데이터를 꺼내어 컨텐트 리졸버에게 전달한다.
        */

        ContentResolver contentResolver = context.getContentResolver();

        // 앨범아트는 uri를 제공하지 않으므로, 별도로 생성.
        Uri uri = Uri.parse("content://media/external/audio/albumart/" + albumArt);
        if (uri != null){
            ParcelFileDescriptor fd = null;
            try{
                fd = contentResolver.openFileDescriptor(uri, "r");

                //true면 비트맵객체에 메모리를 할당하지 않아서 비트맵을 반환하지 않음.
                //다만 options fields는 값이 채워지기 때문에 Load 하려는 이미지의 크기를 포함한 정보들을 얻어올 수 있다.
                options.inJustDecodeBounds = true;

                int scale = 0;
                if(options.outHeight > imgMaxSize || options.outWidth > imgMaxSize){
                    scale = (int)Math.pow(2,(int) Math.round(Math.log(imgMaxSize / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
                }
                options.inJustDecodeBounds = false; // true면 비트맵을 만들지 않고 해당이미지의 가로, 세로, Mime type등의 정보만 가져옴
                options.inSampleSize = scale; // 이미지의 원본사이즈를 설정된 스케일로 축소

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
    }


}
