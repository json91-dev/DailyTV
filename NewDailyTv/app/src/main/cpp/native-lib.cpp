#include <jni.h>
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/opencv.hpp>
#include <android/log.h>

using namespace cv;
using namespace std;



extern "C"
JNIEXPORT jstring

JNICALL
Java_com_example_user_dailytv10_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C" {


////////////////////////////////이미지 크기 재설정 함수 ////////////////////////////////////

float resize(Mat img_src, Mat &img_resize, int resize_width) {

    //새로운 가로픽셀수를 입력된 이미지의 가로픽셀수로 나눈다
    float scale = resize_width / (float) img_src.cols;
    //입력된 이미지의 가로 픽셀수가 새로운 가로픽셀수보다 더 크면
    if (img_src.cols > resize_width) {
        //높이 설정
        int new_height = cvRound(img_src.rows * scale);
        resize(img_src, img_resize, Size(resize_width, new_height));
    } else {
        img_resize = img_src;
    }


    return scale;
}

/////////////////////////////////////이미지 Blend부분 /////////////////////////////////////////






//////////////////////////////////////회색으로 바꾸는 함수이다///////////////////////////////////
JNIEXPORT void JNICALL
Java_com_example_user_dailytv_Activities_OpencvResultActivity_ConvertRGBtoGray(JNIEnv *env,
                                                                         jobject instance,
                                                                         jlong matAddrInput,
                                                                         jlong matAddrResult) {
    //이 함수는 mat값을 받아서 변환된 값을 mat값으로 전달하는 함수이다.

    Mat &matInput = *(Mat *) matAddrInput;
    Mat &matResult = *(Mat *) matAddrResult;

    cvtColor(matInput, matResult, CV_RGBA2GRAY);

}
////////////////////////////////////////바다색//////////////////////////////////////
JNIEXPORT void JNICALL
Java_com_example_user_dailytv_Activities_OpencvResultActivity_ConvertRGBtoFilter2(JNIEnv *env,
                                                                               jobject instance,
                                                                               jlong matAddrInput,
                                                                               jlong matAddrResult) {
    //이 함수는 mat값을 받아서 변환된 값을 mat값으로 전달하는 함수이다.

    Mat &matInput = *(Mat *) matAddrInput;
    Mat &matResult = *(Mat *) matAddrResult;

    cvtColor(matInput, matResult, CV_RGBA2BGR);

}



JNIEXPORT void JNICALL
Java_com_example_user_dailytv_Activities_OpencvResultActivity_ConvertRGBtoFilter3(JNIEnv *env,
                                                                                  jobject instance,
                                                                                  jlong matAddrInput,
                                                                                  jlong matAddrResult) {
    //이 함수는 mat값을 받아서 변환된 값을 mat값으로 전달하는 함수이다.

    Mat &matInput = *(Mat *) matAddrInput;
    Mat &matResult = *(Mat *) matAddrResult;




    cvtColor(matInput, matResult, CV_RGBA2GRAY);
    medianBlur(matResult,matResult,7);
    Laplacian(matResult,matResult,CV_8U,5);
    threshold(matResult,matResult,85,255,THRESH_BINARY_INV);


}

JNIEXPORT void JNICALL
Java_com_example_user_dailytv_Activities_OpencvResultActivity_ConvertRGBtoFilter4(JNIEnv *env,
                                                                                  jobject instance,
                                                                                  jlong matAddrInput,
                                                                                  jlong matAddrResult

) {
    //이 함수는 mat값을 받아서 변환된 값을 mat값으로 전달하는 함수이다.

    Mat &matInput = *(Mat *) matAddrInput;
    Mat &matResult = *(Mat *) matAddrResult;

    cvtColor(matInput, matResult, CV_RGBA2YUV_I420);

}

////////////////////////////////////////얼굴 감지하는 함수이다///////////////////////////////////

JNIEXPORT void JNICALL
Java_com_example_user_dailytv_Activities_OpencvActivity_detect(JNIEnv *env, jclass type,
                                                               jlong cascadeClassifier_face,
                                                               jlong cascadeClassifier_eye,
                                                               jlong addrInput, jlong addrResult,
                                                               jstring imagefilename
                                                             , jdouble sizeratio, jdouble heightratio

)

{


    //초기화 시, 참조자는 객체(변수)를 직접 입력받고, 포인터는 객체(변수)의 주소값을 입력받는다.
    //왼쪽값은 주소값
    //내 예상 오른쪽은 주소값인데 (mat을 참조함으로 가로로 Mat* 표시) * 가 있으므로 원본 Mat
    Mat &img_input = *(Mat *) addrInput;
    Mat &img_result = *(Mat *) addrResult;


    img_result = img_input.clone();

    std::vector<Rect> faces;
    Mat img_gray;
    //input되는 Mat에 gray 필터를 거쳐서 img_gray라는 Mat에 전달한다.
    cvtColor(img_input, img_gray, COLOR_BGR2GRAY);
    //영상의 히스토그램을 조절하여 명암 분포가 빈약한 영상을 균일하게 만들어주는 기법
    equalizeHist(img_gray, img_gray);
    //명암 세게 처리한 부분을 크기조절하여서 img_resize로 전달한다.
    //리사이즈된 비율을 resizeRatio에 전달한다
    Mat img_resize;
    float resizeRatio = resize(img_gray, img_resize, 640);


    ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.1, 2, 0 | CASCADE_SCALE_IMAGE, Size(30, 30));



    //////////////////////////////////////////이미지  받아오는 부분////////////////////////////


    //////////////////////////////////////////////

    //일단 경로값은 받아오는데 성공하였다... 이제 이미지를 붙여볼까..?
    const char *nativeFileNameString = env->GetStringUTFChars(imagefilename, JNI_FALSE);
    string baseDir("/storage/emulated/0/masks/");
    baseDir.append(nativeFileNameString);
    const char *pathDir = baseDir.c_str();

    Mat imagemat;
    int maskflag=0;


    __android_log_print(ANDROID_LOG_DEBUG, "[native-lib] ", "전달된 string값 %s", nativeFileNameString);


    if(!strcmp(nativeFileNameString,"xbutton"))//같다면
    {
        maskflag=0;
    }
    else
    {

        //CV_LOAD_IMAGE_UNCHANGED (<0) loads the image as is (including the alpha channel if present)
        //CV_LOAD_IMAGE_GRAYSCALE ( 0) loads the image as an intensity one
        //CV_LOAD_IMAGE_COLOR (>0) loads the image in the BGR format

        maskflag=1;
        __android_log_print(ANDROID_LOG_DEBUG, "이미지경로 값 ", "%s", pathDir);

        //이미지는 rgb값일듯 -> rgbtogray
        imagemat=imread(pathDir,-1);
    }





    ////////////////////////////////////////////////////////////////////////////////////////////
    for (int i = 0; i < faces.size(); i++) {
        //비율크기로부터 중간 지점 구하기 즉 마스크가 표시될 중간지점을 산출할 수 있다.
        double real_facesize_x = (faces[i].x / resizeRatio);
        double real_facesize_y = (faces[i].y +heightratio/ resizeRatio);
        double real_facesize_width = (faces[i].width / resizeRatio);
        double real_facesize_height = (faces[i].height / resizeRatio);


        Point center(real_facesize_x + real_facesize_width / 2,
                     real_facesize_y + real_facesize_height / 2);


        __android_log_print(ANDROID_LOG_DEBUG, "real_facesize_x", "%f", real_facesize_x);
        __android_log_print(ANDROID_LOG_DEBUG, "real_facesize_y", "%f", real_facesize_y);
        __android_log_print(ANDROID_LOG_DEBUG, "real_facesize_width", "%f", real_facesize_width);
        __android_log_print(ANDROID_LOG_DEBUG, "real_facesize_height", "%f", real_facesize_height);


        __android_log_print(ANDROID_LOG_DEBUG, "real_facesize_x(int)", "%d", (int)real_facesize_x);
        __android_log_print(ANDROID_LOG_DEBUG, "real_facesize_y(int)", "%d", (int)real_facesize_y);
        __android_log_print(ANDROID_LOG_DEBUG, "real_facesize_width(int)", "%d", (int)real_facesize_width);
        __android_log_print(ANDROID_LOG_DEBUG, "real_facesize_height(int)", "%d", (int)real_facesize_height);


        __android_log_print(ANDROID_LOG_DEBUG, "size 비율", "%f", sizeratio);
        __android_log_print(ANDROID_LOG_DEBUG, "height 비율", "%f", heightratio);




        real_facesize_width=real_facesize_width*sizeratio;
        real_facesize_height=real_facesize_height*sizeratio;

        //ellipse(img_result, center, Size(real_facesize_width / 2, real_facesize_height / 2), 0, 0, 360, Scalar(255, 0, 255), 30, 8, 0);


        //현재 출력하는 방송화면 : img_result
        //중앙 좌표 : center
        //얼굴 크기 : face_size_width,facesize_height
            __android_log_print(ANDROID_LOG_DEBUG, "MaskFlag의 값 ", "내부 루프 실행 전 %d", maskflag);


        //if(maskflag==1&&(int)(center.y-(real_facesize_height/2))>0&&(real_facesize_y+real_facesize_height)<img_input.rows)

        if(maskflag==1&&real_facesize_y>0&&(real_facesize_y+real_facesize_height)<img_input.rows)
        {


            ///src1 => 원본에서 사각형만큼 자른 이미지
            // mask1 => 인식된 얼굴크기만큼 resize한 이미지
            // mask2 => 흑백으로 바꾸고 잘라낸이미지지

           __android_log_print(ANDROID_LOG_DEBUG, "MaskFlag의 값 ", "내부 루프 실행 %d", maskflag);
            //////////////////1단계//////////////////

            Mat mask1,src1;

            //mask1은
            resize(imagemat,mask1, Size((int)real_facesize_width ,(int)real_facesize_height));

            __android_log_print(ANDROID_LOG_DEBUG, "real_facesize_x", "%f", real_facesize_x);
            __android_log_print(ANDROID_LOG_DEBUG, "real_facesize_y", "%f", real_facesize_y);
            __android_log_print(ANDROID_LOG_DEBUG, "real_facesize_width", "%f", real_facesize_width);
            __android_log_print(ANDROID_LOG_DEBUG, "real_facesize_height", "%f", real_facesize_height);


            __android_log_print(ANDROID_LOG_DEBUG, "real_facesize_x(int)", "%d", (int)real_facesize_x);
            __android_log_print(ANDROID_LOG_DEBUG, "real_facesize_y(int)", "%d", (int)real_facesize_y);
            __android_log_print(ANDROID_LOG_DEBUG, "real_facesize_width(int)", "%d", (int)real_facesize_width);
            __android_log_print(ANDROID_LOG_DEBUG, "real_facesize_height(int)", "%d", (int)real_facesize_height);






            cv::Rect roi={(int)(center.x - real_facesize_width / 2), (int)(center.y - real_facesize_width / 2), (int)((real_facesize_width )),(int) ((real_facesize_width)) };


            img_result(roi).copyTo(src1);



            //////////////////2단계(이미지 잘라내기)///////////////////////
            Mat mask2, m, m1;
            cvtColor( mask1, mask2, CV_BGR2GRAY );
            threshold( mask2, mask2, 230, 255, CV_THRESH_BINARY_INV );






            //////////////////원본 이미지 색깔 채널별로 나누기/////////////
            //#bitwise_and 연산자는 둘다 0이 아닌 경우만 값을 통과 시킴.
            //#즉 mask가 검정색이 아닌 경우만 통과가 되기때문에 mask영역 이외는 모두 제거됨.

            vector<Mat> maskChannels( 4 ), result_mask( 4 );
            //색깔 채널별로 나누기
            //maskChannles는 인식된 얼굴 크기만큼 resize한 이미지를 채널별로 나눈 이미지이다.

            split( mask1, maskChannels );


            //img_result의 순서는
            bitwise_and( maskChannels[2], mask2, result_mask[0] ); //maskChannels[0]=> R result_mask[0]=>B
            bitwise_and( maskChannels[1], mask2, result_mask[1] ); //maskChannles[1]=> G result_mask[1]=>G
            bitwise_and( maskChannels[0], mask2, result_mask[2] ); //maskChannels[2]=> B result_mask[2]=>R
            bitwise_and( maskChannels[3], mask2, result_mask[3]);


            //나누고 병합하기
            merge( result_mask, m );


            __android_log_print(ANDROID_LOG_DEBUG, "MaskFlag의 값 ", "내부 루프 실행333333333333333333333333 %d", maskflag);

            //왜 두번하는지는 잘 모르겠다...(같은부분을 전환시키는듯??)
            //마스크 영상을 반전시키는 부분>??
            mask2 = 255 - mask2;
            __android_log_print(ANDROID_LOG_DEBUG, "MaskFlag의 값 ", "내부 루프 실행4444444444444444444444444444444 %d", maskflag);
            vector<Mat> srcChannels( 3 );
            __android_log_print(ANDROID_LOG_DEBUG, "MaskFlag의 값 ", "내부 루프 실행55555555555555555555555555555555 %d", maskflag);



            split(src1, srcChannels);
            __android_log_print(ANDROID_LOG_DEBUG, "MaskFlag의 값 ", "내부 루프 실행666666666666666666666666666666666 %d", maskflag);
            bitwise_and( srcChannels[0], mask2, result_mask[0] );
            bitwise_and( srcChannels[1], mask2, result_mask[1] );
            bitwise_and( srcChannels[2], mask2, result_mask[2] );
            bitwise_and( srcChannels[3], mask2, result_mask[3] );

            __android_log_print(ANDROID_LOG_DEBUG, "MaskFlag의 값 ", "내부 루프 실행00000000000000000000000000000000 %d", maskflag);

            merge( result_mask, m1 );

            __android_log_print(ANDROID_LOG_DEBUG, "MaskFlag의 값 ", "내부 루프 실행0000000000000000000000000000000 %d", maskflag);

            //m이미지와 m1이미지 연산처리
            addWeighted( m, 1, m1, 1, 0, m1);


            m1.copyTo( img_result( roi ) );
        }



        /*
        Rect face_area(real_facesize_x, real_facesize_y, real_facesize_width, real_facesize_height);
        Mat faceROI = img_gray(face_area);
        std::vector<Rect> eyes;
        //-- In each face, detect eyes
        ((CascadeClassifier *) cascadeClassifier_eye)->detectMultiScale(faceROI, eyes, 1.1, 2, 0 | CASCADE_SCALE_IMAGE, Size(30, 30));
        for (size_t j = 0; j < eyes.size(); j++) {
            Point eye_center(real_facesize_x + eyes[j].x + eyes[j].width / 2,
                             real_facesize_y + eyes[j].y + eyes[j].height / 2);
            int radius = cvRound((eyes[j].width + eyes[j].height) * 0.25);
            circle(img_result, eye_center, radius, Scalar(255, 0, 0), 30, 8, 0);
        }
         */
    }

}

///////////////////////////////////분류기 감지 소스////////////////////////

JNIEXPORT jlong JNICALL
Java_com_example_user_dailytv_Activities_OpencvActivity_loadCascade(JNIEnv *env, jclass type,
                                                                    jstring cascadeFileName) {
    const char *nativeFileNameString = env->GetStringUTFChars(cascadeFileName, JNI_FALSE);

    string baseDir("/storage/emulated/0/");
    baseDir.append(nativeFileNameString);
    const char *pathDir = baseDir.c_str();

    jlong ret = 0;
    //외부저장소에 있는 xml파일(학습데이터)로부터 분류기를 로딩
    //CascadeClassifier을 생성하고 그 주소값 반환 (long형으로)
    ret = (jlong) new CascadeClassifier(pathDir);
    if (((CascadeClassifier *) ret)->empty()) {
        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ", "CascadeClassifier로 로딩 실패  %s", nativeFileNameString);
    } else
        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ", "CascadeClassifier로 로딩 성공 %s", nativeFileNameString);

    return ret;
}






}


