package demo.face.school.com.facedemo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKMatching;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.arcsoft.facetracking.AFT_FSDKEngine;
import com.arcsoft.facetracking.AFT_FSDKError;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.arcsoft.facetracking.AFT_FSDKVersion;
import com.guo.android_extend.java.ExtByteArrayOutputStream;
import com.guo.android_extend.tools.CameraHelper;
import com.guo.android_extend.widget.CameraFrameData;
import com.guo.android_extend.widget.CameraGLSurfaceView;
import com.guo.android_extend.widget.CameraSurfaceView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import demo.face.school.com.facedemo.activity.RegisterFaceEditActivity;
import demo.face.school.com.facedemo.dao.FaceDB;
import demo.face.school.com.facedemo.loop.AbsLoop;
import demo.face.school.com.facedemo.model.EventBusModel;
import demo.face.school.com.facedemo.model.StudentModel;
import demo.face.school.com.facedemo.util.DisplayUtil;

public class MainActivity extends Activity implements CameraSurfaceView.OnCameraListener, View.OnTouchListener, Camera.AutoFocusCallback {
    private int mWidth, mHeight, mFormat;
    private SurfaceHolder surfaceHolder = null;
    private CameraSurfaceView mSurfaceView;
    private CameraGLSurfaceView mGLSurfaceView;
    private Camera mCamera;
    AFT_FSDKVersion version = new AFT_FSDKVersion();
    AFT_FSDKEngine engine = new AFT_FSDKEngine();
    List<AFT_FSDKFace> result = new ArrayList<>();


    int mCameraID;
    int mCameraRotate;
    boolean mCameraMirror;
    byte[] mImageNV21 = null;
    FRAbsLoop mFRAbsLoop = null;
    AFT_FSDKFace mAFT_FSDKFace = null;
    Handler mHandler;
    private ImageView mImageView;
    private Context mContext;

    //未注册的学生信息
    private TextView register_btn;
    private TextView register_tip;
    private ListView mListView;
    private ListAdapter adapter;
    private RelativeLayout register_layout;
    private boolean isStart = true;
    private List<StudentModel> studentModels = new ArrayList<>(); //学生数据，将从数据库读取，作为人脸识别遍历用
    private StudentModel mStudentModel;
    //学生信息布局
    private RelativeLayout student_layout;
    private TextView student_name;
    private TextView sign_time;
    private TextView main_tip_txt;
    private String signName;

    Uri mImage;

    private static final int REQUEST_CODE_IMAGE_CAMERA = 1;
    private static final int REQUEST_CODE_IMAGE_OP = 2;
    private static final int REQUEST_CODE_OP = 3;

    public Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    showRegisterLayout();
                    main_tip_txt.setText("创建您的打卡信息吧~");
                    break;
                }

                case 2: {
                    showStudentLayout();
                    student_name.setText("姓名:" + signName);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");// HH:mm:ss
                    Date date = new Date(System.currentTimeMillis());

                    long nowtime = System.currentTimeMillis();
                    final Calendar mCalendar = Calendar.getInstance();
                    mCalendar.setTimeInMillis(nowtime);
                    int apm = mCalendar.get(Calendar.AM_PM);
                    if (apm == 0) {
                        sign_time.setText("到班时间:" + simpleDateFormat.format(date) + "\tA.M.");

                    } else {
                        sign_time.setText("到班时间:" + simpleDateFormat.format(date) + "\tP.M.");
                    }
                    main_tip_txt.setText("打卡完成~祝您有个美好的一天~");
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        //去除title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉Activity上面的状态栏
        studentModels = DataSupport.findAll(StudentModel.class);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        if (studentModels == null || studentModels.size() == 0) {
            StudentModel data01 = new StudentModel();
            data01.setStudentId(453211);
            data01.setName("光头强");
            data01.setFaceData("");
            data01.save();
            StudentModel data02 = new StudentModel();
            data02.setStudentId(453222);
            data02.setName("熊大");
            data02.setFaceData("");
            data02.save();
            StudentModel data03 = new StudentModel();
            data03.setStudentId(453233);
            data03.setName("熊二");
            data03.setFaceData("");
            data03.save();
            studentModels = DataSupport.findAll(StudentModel.class);

        }


        initView();
    }

    private void initView() {

        student_layout = (RelativeLayout) findViewById(R.id.student_layout);
        student_name = (TextView) findViewById(R.id.student_name);
        sign_time = (TextView) findViewById(R.id.sign_time);
        main_tip_txt = (TextView) findViewById(R.id.main_tip_txt);
        register_tip = (TextView) findViewById(R.id.register_tip);
        register_btn = (TextView) findViewById(R.id.register_btn);

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStudentModel == null || TextUtils.isEmpty(mStudentModel.getName())) {

                } else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("请选择注册类型")
                            .setIcon(R.drawable.dialog_tip_tag)
                            .setItems(new String[]{"图库选择", "相机拍摄"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 1: //照相机
                                            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                                            ContentValues values = new ContentValues(1);
                                            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                                            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                            mImage = uri;
                                            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                                            startActivityForResult(intent, REQUEST_CODE_IMAGE_CAMERA);
                                            break;
                                        case 0: //图片
                                            Intent getImageByalbum = new Intent(Intent.ACTION_GET_CONTENT);
                                            getImageByalbum.addCategory(Intent.CATEGORY_OPENABLE);
                                            getImageByalbum.setType("image/jpeg");
                                            startActivityForResult(getImageByalbum, REQUEST_CODE_IMAGE_OP);
                                            break;
                                        default:
                                            ;
                                    }
                                }
                            })
                            .show();

                }
            }
        });

        register_layout = (RelativeLayout) findViewById(R.id.register_layout);
        mListView = (ListView) findViewById(R.id.listview);
        mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;//调用前置摄像头
        mCameraMirror = true;//是否是镜面效果
        Point p = DisplayUtil.getScreenMetrics(this);
        mWidth = p.x;
        mHeight = p.y;
        mFormat = ImageFormat.NV21;
        mHandler = new Handler();

        mGLSurfaceView = (CameraGLSurfaceView) findViewById(R.id.glsurfaceView);
        mGLSurfaceView.setOnTouchListener(this);
        mSurfaceView = (CameraSurfaceView) findViewById(R.id.surfaceView);
        mSurfaceView.setOnCameraListener(this);
        mSurfaceView.setupGLSurafceView(mGLSurfaceView, true, mCameraMirror, mCameraRotate);
        mSurfaceView.debug_print_fps(true, false);
        mGLSurfaceView.setZOrderOnTop(true);
        surfaceHolder = mGLSurfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mImageView = (ImageView) findViewById(R.id.imageView);
        AFT_FSDKError err = engine.AFT_FSDK_InitialFaceEngine(FaceDB.appid, FaceDB.ft_key, AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, 16, 5);
        Log.d("--->", "AFT_FSDK_InitialFaceEngine =" + err.getCode());
        err = engine.AFT_FSDK_GetVersion(version);
        Log.d("---->", "AFT_FSDK_GetVersion:" + version.toString() + "," + err.getCode());

        mFRAbsLoop = new FRAbsLoop();
        mFRAbsLoop.start();


        adapter = new ListAdapter(mContext);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedPosition(position);
                adapter.notifyDataSetInvalidated();
                mStudentModel = studentModels.get(position);
            }
        });

    }


    Runnable hide = new Runnable() {
        //        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void run() {
            mImageView.setImageAlpha(128);
        }
    };

    class FRAbsLoop extends AbsLoop {

        AFR_FSDKVersion version = new AFR_FSDKVersion();
        AFR_FSDKEngine engine = new AFR_FSDKEngine();
        AFR_FSDKFace result = new AFR_FSDKFace();


        @Override
        public void setup() {
            AFR_FSDKError error = engine.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
            Log.d("------>", "AFR_FSDK_InitialEngine = " + error.getCode());
            error = engine.AFR_FSDK_GetVersion(version);
            Log.d("------>", "FR=" + version.toString() + "," + error.getCode()); //(210, 178 - 478, 446), degree = 1　780, 2208 - 1942, 3370
        }

        @Override
        public void run() {
            super.run();
            Thread thisThread = Thread.currentThread();
            setup();
            while (mBlinker == thisThread) {

                if (isStart == true) {
                    loop();
                }
            }
            over();

        }

        @Override
        public void loop() {
            if (mImageNV21 != null) {
                long time = System.currentTimeMillis();
                AFR_FSDKError error = engine.AFR_FSDK_ExtractFRFeature(mImageNV21, mWidth, mHeight, AFR_FSDKEngine.CP_PAF_NV21, mAFT_FSDKFace.getRect(), mAFT_FSDKFace.getDegree(), result);
                Log.d("------>", "AFR_FSDK_ExtractFRFeature cost :" + (System.currentTimeMillis() - time) + "ms");
                Log.d("------>", "Face=" + result.getFeatureData()[0] + "," + result.getFeatureData()[1] + "," + result.getFeatureData()[2] + "," + error.getCode());

                AFR_FSDKMatching score = new AFR_FSDKMatching();
                float max = 0.0f;
                String name = null;
                String studentId = null;
//                FaceDao faceDao = mdaoSession.getFaceDao();
//                List<Face> faces = faceDao.loadAll();

                for (StudentModel fr : studentModels) {
                    if (fr.getFaceData() != null && !TextUtils.isEmpty(fr.getFaceData())) {
                        AFR_FSDKFace mdara = new AFR_FSDKFace();
                        mdara.setFeatureData(Base64.decode(fr.getFaceData(), Base64.DEFAULT));
                        error = engine.AFR_FSDK_FacePairMatching(result, mdara, score);
                        Log.d("------>", "Score:" + score.getScore() + ", AFR_FSDK_FacePairMatching=" + error.getCode());
                        if (max < score.getScore()) {
                            max = score.getScore();
                            name = fr.getName();
                            studentId = fr.getStudentId() + "";
                        }
                    }

                }

                //crop
                byte[] data = mImageNV21;
                YuvImage yuv = new YuvImage(data, ImageFormat.NV21, mWidth, mHeight, null);
                ExtByteArrayOutputStream ops = new ExtByteArrayOutputStream();
                yuv.compressToJpeg(mAFT_FSDKFace.getRect(), 80, ops);
                final Bitmap bmp = BitmapFactory.decodeByteArray(ops.getByteArray(), 0, ops.getByteArray().length);
                try {
                    ops.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (max > 0.6f) {
                    //fr success.
                    final float max_score = max;
                    Log.e("------>", "fit Score:" + max + ", NAME:" + name);
                    final String mNameShow = name;
                    mHandler.removeCallbacks(hide);
                    final String finalName = name;
                    mHandler.post(new Runnable() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void run() {
                            mImageView.setRotation(mCameraRotate);
                            if (mCameraMirror) {
                                mImageView.setScaleY(-1);
                            }
                            mImageView.setImageAlpha(255);
                            mImageView.setImageBitmap(bmp);
//                            showToast(finalName);
                        }
                    });
                    isStart = false;
                    handler2.sendEmptyMessage(2);
                    signName=name;

                    try {
                        sleep(4000);
                        isStart = true;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    isStart = true;

                } else {
                    //识别成功，未找到对应的学生
                    Log.e("---->", "未找到学生");
                    handler2.sendEmptyMessage(1);
                    isStart = true;

                }
                mImageNV21 = null;
            }

        }

        @Override
        public void over() {
            AFR_FSDKError error = engine.AFR_FSDK_UninitialEngine();
            Log.d("----->", "AFR_FSDK_UninitialEngine : " + error.getCode());
        }
    }


    /* (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mFRAbsLoop.shutdown();
        AFT_FSDKError err = engine.AFT_FSDK_UninitialFaceEngine();
        Log.d("----->", "AFT_FSDK_UninitialFaceEngine =" + err.getCode());
    }


    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (success) {
            Log.d("----->", "Camera Focus SUCCESS!");
        }
    }

    @Override
    public Camera setupCamera() {
        // TODO Auto-generated method stub
        mCamera = Camera.open(mCameraID);
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mWidth, mHeight);
            parameters.setPreviewFormat(mFormat);

            for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
                Log.e("----->", "SIZE:" + size.width + "x" + size.height);
            }
            for (Integer format : parameters.getSupportedPreviewFormats()) {
                Log.e("----->", "FORMAT:" + format);
            }

            List<int[]> fps = parameters.getSupportedPreviewFpsRange();
            for (int[] count : fps) {
                Log.d("----->", "T:");
                for (int data : count) {
                    Log.d("----->", "V=" + data);
                }
            }
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mCamera != null) {
            mWidth = mCamera.getParameters().getPreviewSize().width;
            mHeight = mCamera.getParameters().getPreviewSize().height;
        }
        return mCamera;
    }

    @Override
    public void setupChanged(int format, int width, int height) {

    }

    @Override
    public boolean startPreviewLater() {
        return false;
    }

    @Override
    public Object onPreview(byte[] data, int width, int height, int format, long timestamp) {
        AFT_FSDKError err = engine.AFT_FSDK_FaceFeatureDetect(data, width, height, AFT_FSDKEngine.CP_PAF_NV21, result);
        Log.d("----->", "AFT_FSDK_FaceFeatureDetect =" + err.getCode());
        Log.d("----->", "Face=" + result.size());
        for (AFT_FSDKFace face : result) {
            Log.d("----->", "Face:" + face.toString());
        }
        if (mImageNV21 == null) {
            if (!result.isEmpty()) {
                mAFT_FSDKFace = result.get(0).clone();
                mImageNV21 = data.clone();
            } else {
                mHandler.postDelayed(hide, 3000);
            }
        }
        //copy rects
        Rect[] rects = new Rect[result.size()];
        for (int i = 0; i < result.size(); i++) {
            rects[i] = new Rect(result.get(i).getRect());
        }
        //clear result.
        result.clear();
        return rects;
    }


    @Override
    public void onBeforeRender(CameraFrameData data) {

    }

    @Override
    public void onAfterRender(CameraFrameData data) {
        mGLSurfaceView.getGLES2Render().draw_rect((Rect[]) data.getParams(), getResources().getColor(R.color.SpecialGreen), 4);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        CameraHelper.touchFocus(mCamera, event, v, this);
        return false;
    }


    class ListAdapter extends BaseAdapter {
        private int selectedPosition = -1;// 选中的位置
        private Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return studentModels.size();
        }

        @Override
        public Object getItem(int position) {
            return studentModels.get(position);
        }

        public void setSelectedPosition(int position) {
            selectedPosition = position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.listview_item, null);
                holder.studentName = (TextView) convertView.findViewById(R.id.studentName);
                holder.studentId = (TextView) convertView.findViewById(R.id.studentId);
                holder.iv = (ImageView) convertView.findViewById(R.id.iv);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.studentName.setText(studentModels.get(position).getName());
            holder.studentId.setText(studentModels.get(position).getStudentId() + "");
            if (position % 2 == 0) {
                if (selectedPosition == position) {
                    convertView.setSelected(true);
                    convertView.setPressed(true);
                    holder.iv.setImageResource(R.drawable.select_yes);
                } else {
                    convertView.setSelected(false);
                    convertView.setPressed(false);
                    holder.iv.setImageResource(R.drawable.select_no);
                }
            } else {
                if (selectedPosition == position) {
                    convertView.setSelected(true);
                    convertView.setPressed(true);
                    holder.iv.setImageResource(R.drawable.select_yes);
                } else {
                    convertView.setSelected(false);
                    convertView.setPressed(false);
                    holder.iv.setImageResource(R.drawable.select_no);
                }
            }
            return convertView;
        }

        class ViewHolder {
            TextView studentName;
            TextView studentId;
            ImageView iv;
        }

    }


    public void showRegisterLayout() {
        if (register_layout.getVisibility() == View.GONE) {
            register_layout.setVisibility(View.VISIBLE);
        }
        if (register_tip.getVisibility() == View.GONE) {
            register_tip.setVisibility(View.VISIBLE);
        }

        if (student_layout.getVisibility() == View.VISIBLE) {
            student_layout.setVisibility(View.GONE);
        }
        ;
    }


    public void showStudentLayout() {
        if (register_layout.getVisibility() == View.VISIBLE) {
            register_layout.setVisibility(View.GONE);
        }
        if (register_tip.getVisibility() == View.VISIBLE) {
            register_tip.setVisibility(View.GONE);
        }

        if (student_layout.getVisibility() == View.GONE) {
            student_layout.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE_OP && resultCode == RESULT_OK) {
            Uri mPath = data.getData();
            String file = getPath(mPath);
            Bitmap bmp = decodeImage(file);
            if (bmp == null || bmp.getWidth() <= 0 || bmp.getHeight() <= 0) {
                Log.e("haha", "error");
            } else {
                Log.i("haha", "bmp [" + bmp.getWidth() + "," + bmp.getHeight());
            }
            startRegister(bmp, file);
        } else if (requestCode == REQUEST_CODE_OP) {
            Log.i("haha", "RESULT =" + resultCode);
            if (data == null) {
                return;
            }
            Bundle bundle = data.getExtras();
            String path = bundle.getString("imagePath");
            Log.i("haha", "path=" + path);
        } else if (requestCode == REQUEST_CODE_IMAGE_CAMERA && resultCode == RESULT_OK) {
            Uri mPath = mImage;
            String file = getPath(mPath);
            Bitmap bmp = decodeImage(file);
            startRegister(bmp, file);
        }
    }


    /**
     * @param uri
     * @return
     */
    private String getPath(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(this, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(this, contentUri, null, null);
                } else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    return getDataColumn(this, contentUri, selection, selectionArgs);
                }
            }
        }
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor actualimagecursor = this.getContentResolver().query(uri, proj, null, null, null);
        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        actualimagecursor.moveToFirst();
        String img_path = actualimagecursor.getString(actual_image_column_index);
        String end = img_path.substring(img_path.length() - 4);
        if (0 != end.compareToIgnoreCase(".jpg") && 0 != end.compareToIgnoreCase(".png")) {
            return null;
        }
        return img_path;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param path
     * @return
     */
    private Bitmap decodeImage(String path) {
        Bitmap res;
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inSampleSize = 1;
            op.inJustDecodeBounds = false;
            //op.inMutable = true;
            res = BitmapFactory.decodeFile(path, op);
            //rotate and scale.
            Matrix matrix = new Matrix();

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }

            Bitmap temp = Bitmap.createBitmap(res, 0, 0, res.getWidth(), res.getHeight(), matrix, true);
            Log.d("com.arcsoft", "check target Image:" + temp.getWidth() + "X" + temp.getHeight());

            if (!temp.equals(res)) {
                res.recycle();
            }
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @param mBitmap
     */
    private void startRegister(Bitmap mBitmap, String file) {
        Intent it = new Intent(MainActivity.this, RegisterFaceEditActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("imagePath", file);
        bundle.putString("studengtId", mStudentModel.getStudentId() + "");
        it.putExtras(bundle);
        startActivityForResult(it, REQUEST_CODE_OP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        studentModels = DataSupport.findAll(StudentModel.class);
    }
}
