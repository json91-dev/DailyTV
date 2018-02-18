package com.example.user.dailytv.Module2;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.user.dailytv.Activities.ExoPlayer2Activity;
import com.example.user.dailytv.R;

/*
 * Portions (c) 2009 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Coby Plain coby.plain@gmail.com, Ali Muzaffar ali@muzaffar.me
 */

public class DrawSurfaceView extends View {

	final static String TAG="DrawSurfaceView";

	Point me = new Point(-33.870932d, 151.204727d, "Me");
	Paint mPaint = new Paint();
	private double OFFSET = 0d;
	private double screenWidth, screenHeight = 0d;
	private Bitmap[] mSpots, mBlips;
	private Bitmap mRadar;

	private Bitmap mArrow;

	Context mContext;



	Point nextLocation= new Point(-33.870932d, 151.204727d, "nextLocation");

	public static ArrayList<Point> props = new ArrayList<Point>();

	public ArrayList<Point> tmapPointArrayList;


	// tmap에서 경로에 대한 정보를 모두 가져왔을때 surfaceview의 onDraw에서 그리는 작업을 처리하기 위한 flag이다.
	boolean isOkayGetTmapPointArrayList=false;

	// 맨처음 Activity가 실행되면 props값으로 동서남북을 Drawing한다.
	static {
		//위도 90도
		props.add(new Point(90d, 110.8000, "북쪽"));
		props.add(new Point(-90d, -110.8000, "남쪽"));
		props.add(new Point(37.76719d, 130.92407, "동쪽"));
		props.add(new Point(37.76719d, 123.78845, "서쪽"));
	}

	// 경로에 대한 정보값을 가져올때까지 진행상황을 표시하는 progressBar이다.

	private ProgressDialog progressBar;

	public void setTmapPointArrayList(ArrayList<Point> tmapPointArrayList)
	{
		isOkayGetTmapPointArrayList=true;
		this.tmapPointArrayList=tmapPointArrayList;

		//1. tmap에서 가져온 값에서 첫번째 좌표는 자신의 위치이고 첫번째 좌표에 대한 설명은 다음 가야할 목적지에 대한 경로 안내이다.
		//2. 하지만 내가 사용할 Point클래스에서는 nextLocation이 첫번째 좌표는 다음 목적지 좌표이고 좌표에 대한 설명은 위와 같다.
		//3. 따라서 내가 필요한 값으로 바꾸기 위해 description을 하나씩 이동시켜야 되기 때문에 다음 for문을 통해 이를 수행한다.

		for(int i=tmapPointArrayList.size()-1; i>0 ; i--)
		{
			tmapPointArrayList.get(i).description=tmapPointArrayList.get(i-1).description+"";
		}

		getNextLocation();
		getNextLocation();

		//프로그래스바 종료


		progressBar.dismiss();
	}

	// 경로(Point)값들을 저장하고있는 ArrayList에서 다음 경로를 가져오는 함수이다.
	private void getNextLocation()
	{
		if(tmapPointArrayList.size()>=1) {
			nextLocation = tmapPointArrayList.get(0);
			tmapPointArrayList.remove(0);
		}
		else if(tmapPointArrayList.size()==0)
		{
			Toast.makeText(mContext,"목적지에 도착하셨습니다.!!",Toast.LENGTH_LONG).show();
		}
	}




	public DrawSurfaceView(Context c, Paint paint) {
		super(c);
	}

	//맨처음 객체가 생성될때 다음 함수들을 실행시킨다.
	public DrawSurfaceView(Context context, AttributeSet set) {
		super(context, set);


		mContext=context;

		//프로그래스바 실행

		progressBar=new ProgressDialog(mContext);
		progressBar.setCancelable(false);
		progressBar.setMessage("경로를 탐색중입니다.. 잠시만 기다려주세요.");
		progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressBar.show();


		//레이터의 색상 설정
		mPaint.setColor(Color.GREEN);
		//텍스트의 크기 설정
		mPaint.setTextSize(50);
		//팬 두께 설정
		mPaint.setStrokeWidth(DpiUtils.getPxFromDpi(getContext(), 2));
		//곡선이 부드럽게 처리된다.
		mPaint.setAntiAlias(true);
		//레이터 이미지 Bitmap값을 가져온다.
		mRadar = BitmapFactory.decodeResource(context.getResources(), R.drawable.radar);

		//동서남북값을 ar로 표시하는 bitmap 이미지를 가져온다.
		mSpots = new Bitmap[props.size()];
		for (int i = 0; i < mSpots.length; i++) 
			mSpots[i] = BitmapFactory.decodeResource(context.getResources(), R.drawable.dot3);

		//레이터 내부에서 사용되는 bitmap 이미지를 가져온다.
		mBlips = new Bitmap[props.size()];
		for (int i = 0; i < mBlips.length; i++)
			mBlips[i] = BitmapFactory.decodeResource(context.getResources(), R.drawable.blip);


		//방향을 나타내는데 사용되는 bitmap 이미지를 가져온다.
		mArrow=BitmapFactory.decodeResource(context.getResources(),R.drawable.array2);

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		Log.d("onSizeChanged", "in here w=" + w + " h=" + h);
		screenWidth = (double) w;
		screenHeight = (double) h;
	}


	// 이부분은 백그라운드로 계속 수행되며 다른 함수에서 onDraw()내부에 있는 변수의 값들을
	// 변형시켜줌으로써 이미지 드로잉을 수행한다.
	@Override
	protected void onDraw(Canvas canvas) {

		//레이터를 그린다.
		canvas.drawBitmap(mRadar, 0, 0, mPaint);

		//레이더의 중심점을 찾는다.
		int radarCentreX = mRadar.getWidth() / 2;
		int radarCentreY = mRadar.getHeight() / 2;

		for (int i = 0; i < mBlips.length; i++) {

			// 레이터에 사용된다.
			// 레이더 안에 bitmap이미지들과 동서남북의 bitmap이미지들을 순차적으로 가져온다.
			Bitmap blip = mBlips[i];
			Bitmap spot = mSpots[i];

			//기본 동서남북의 위치를 담는 변수를 u라고 한다.
			Point u = props.get(i);


			//me 는 현재 나의 위치 Point 객체이다.
			double dist = distInMetres(me, u);
			
			if (blip == null || spot == null)
				continue;
			
			if(dist > 70)
				dist = 70; //we have set points very far away for demonstration

			// 두 지점의 경도와 위도를 입력하고 각도를 얻는다.?
			// u는 동서남북의 위치이다.
			// OFFSET은 나침판의 첫번째값 (x축)
			double angle = bearing(me.latitude, me.longitude, u.latitude, u.longitude) - OFFSET;
			double xPos, yPos;
			
			if(angle < 0)
				angle = (angle+360)%360;
			
			xPos = Math.sin(Math.toRadians(angle)) * dist;
			yPos = Math.sqrt(Math.pow(dist, 2) - Math.pow(xPos, 2));

			if (angle > 90 && angle < 270)
				yPos *= -1;
			
			double posInPx = angle * (screenWidth / 90d);


			//레이더와 레이더 안의 동서남북을 그리는 작업을 수행한다.
			int blipCentreX = blip.getWidth() / 2;
			int blipCentreY = blip.getHeight() / 2;
			
			xPos = xPos - blipCentreX;
			yPos = yPos + blipCentreY;
			canvas.drawBitmap(blip, (radarCentreX + (int) xPos), (radarCentreY - (int) yPos), mPaint); //radar blip


			//reuse xPos
			// 동서남북의 위치를 그리는 작업을 수행한다.
			int spotCentreX = spot.getWidth() / 2;
			int spotCentreY = spot.getHeight() / 2;
			xPos = posInPx - spotCentreX;
			
			if (angle <= 45) 
				u.x = (float) ((screenWidth / 2) + xPos);
			
			else if (angle >= 315) 
				u.x = (float) ((screenWidth / 2) - ((screenWidth*4) - xPos));
			
			else
				u.x = (float) (float)(screenWidth*9); //somewhere off the screen
			
			u.y = (float)screenHeight/2 + spotCentreY;
			canvas.drawBitmap(spot, u.x, u.y, mPaint); //camera spot



			if(u.description.length()>15)
			{
				String des1=u.description.substring(0,15);
				String des2=u.description.substring(15,u.description.length());
				canvas.drawText(des1, u.x, u.y, mPaint); //text
				canvas.drawText(des2,u.x,u.y+30,mPaint);

			}
			else
			{
				canvas.drawText(u.description, u.x, u.y, mPaint); //text

			}



		}

		// 현재위치에서 첫번째 지점까지의 방향을 SurfaceView에 표시한다.
		// 현재 위치 정보를 계속 받아오고 첫번쨰 지점까지의 거리를 계속 받아와서 SurfaceView에 표시한다.
		// 첫번째 지점 근처에 도착하게되면 두번째 지점까지의 방향을 SurfaceView에 표시한다.
		// 위의 작업을 반복한다.
		// 마지막 지점에 도착하게되면 도착했다는 메세지를 반환한다.


//
///////////////////////////////////////////////////////////////////////////////////////////////////////////
		if(isOkayGetTmapPointArrayList) {

			//////////남은 거리를 표시하는 text 를 캔버스에 그린다.//////////////




			//Double distanceBetweenPoints = distInMetres(me, nextLocation);


			Location curPoint=new Location("myLocation");
			curPoint.setLatitude(me.latitude);
			curPoint.setLongitude(me.longitude);

			Location nextPoint=new Location("nextLocation");
			nextPoint.setLatitude(nextLocation.latitude);
			nextPoint.setLongitude(nextLocation.longitude);

			float distanceBetweenLocations=curPoint.distanceTo(nextPoint);
			String distanceMeterBetweenLocations= Math.floor(distanceBetweenLocations) + "";
			//Log.e(TAG, "남은 거리 : " + distanceBetweenPoints);


			Log.e(TAG,"현재 내 경위도 : "+curPoint.getLatitude()+","+curPoint.getLongitude());
			Log.e(TAG,"다음 위치의 경위도 : "+nextPoint.getLatitude()+","+nextPoint.getLongitude());
			Log.e(TAG,"남은 거리 : "+distanceBetweenLocations);




			//Log.e(TAG,"텍스트 x좌표"+xPos);
			//Log.e(TAG,"텍스트 y좌표"+yPos);

			Paint myPaint = new Paint();

			//레이터의 색상 설정 //텍스트의 크기 설정 //팬 두께 설정 //곡선이 부드럽게 처리된다.
			myPaint.setColor(Color.BLACK);
			myPaint.setTextSize(100);
			myPaint.setStrokeWidth(DpiUtils.getPxFromDpi(getContext(), 2));
			myPaint.setAntiAlias(true);


			float yPosText = (float) (screenHeight / 5) * 4;
			float xPosText= (float) (screenWidth / 4);
			canvas.drawText("남은거리 : "+distanceMeterBetweenLocations+"m", xPosText, yPosText, myPaint);

			myPaint.setTextSize(70);
			yPosText=(float)(screenHeight/6)*5;
			xPosText= (float) (screenWidth / 4);

			//글자수 2줄로 넣기
			if(nextLocation.description.length()>18)
			{
				try {
					String des1 = nextLocation.description.substring(0, 18);
					String des2 = nextLocation.description.substring(18, nextLocation.description.length());
					canvas.drawText(des1, xPosText, yPosText, myPaint); //text
					canvas.drawText(des2, xPosText, yPosText + 70, myPaint);
				}catch (Exception e)
				{
					Log.e("문자열파싱에러",e.getMessage());
				}
			}
			else
			{
				canvas.drawText(nextLocation.description+"",xPosText,yPosText,myPaint);
			}



			//////////////////현재 위치와 다음 위치까지의 방향나타내는 Bitmap을 SurfaceView위에 표시한다.

			double angle = bearing(me.latitude, me.longitude, nextLocation.latitude, nextLocation.longitude) - OFFSET;
			//double dist = distanceBetweenPoints;

			//dist와 angle에 따른 x좌표와 y좌표
			double xPos, yPos;

			double posInPx = angle * (screenWidth / 90d);


			int arrowCentreX = mArrow.getWidth() / 2;
			int arrowCentreY = mArrow.getHeight() / 2;

			// 길안내 화살표를 표시할 x좌표 설정
			xPos = posInPx - arrowCentreX;



			if (angle <= 45)
				nextLocation.x = (float) ((screenWidth / 2) + xPos);

			else if (angle >= 315)
				nextLocation.x = (float) ((screenWidth / 2) - ((screenWidth*4) - xPos));

			else
				nextLocation.x = (float)(float)(screenWidth*9); //somewhere off the screen

			nextLocation.y = (float)screenHeight/2 - arrowCentreY;



			canvas.drawBitmap(mArrow, nextLocation.x, nextLocation.y, mPaint); //camera spot






			//////////////////////////////////////////////////////////////////////
			//1. 만약 1m이내까지 접근하였다면 다음 지점에대한 정보를 가져온다.
			//2. tmap에서 경로Point를 모두 가져온 후 호출되어야 한다.(isOkayGetTmapPointArrayList)
			if (isOkayGetTmapPointArrayList && distanceBetweenLocations < 8) {

				if(tmapPointArrayList.size()!=0)
					Toast.makeText(mContext,"경유지 도착성공!! 다음경로로 이동해주세요",Toast.LENGTH_LONG).show();

				getNextLocation();
			}
		}

	}

	public void setOffset(float offset) {
		this.OFFSET = offset;
	}


	public void setMyLocation(double latitude, double longitude) {
		me.latitude = latitude;
		me.longitude = longitude;
	}


	//두 지점 사이의 거리를 구하는 함수이다.
	//화면상의 거리를 어디에 두는지 결정한다.??

	protected double distInMetres(Point me, Point u) {

		double lat1 = me.latitude;
		double lng1 = me.longitude;

		double lat2 = u.latitude;
		double lng2 = u.longitude;

		double earthRadius = 6371;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);

		//두 지점의 위도값 경도값에 차이의 각도
		//a=sin제곱(위도) + sin제곱(경도)*cos(위도) + cos(경도)
		//c= 2*탄젠트(루트a,루트 1-a)
		//c*지구반지름= dist;

		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;

		return dist * 1000;
	}

	protected static double bearing(double lat1, double lon1, double lat2, double lon2) {

		//두 지점에서 경도값의 차이(호의길이)를

		double longDiff = Math.toRadians(lon2 - lon1);
		double la1 = Math.toRadians(lat1);
		double la2 = Math.toRadians(lat2);
		double y = Math.sin(longDiff) * Math.cos(la2);
		//두 지점 사이의 각도를 구하는 공식이다.
		double x = Math.cos(la1) * Math.sin(la2) - Math.sin(la1) * Math.cos(la2) * Math.cos(longDiff);

		double result = Math.toDegrees(Math.atan2(y, x));
		return (result+360.0d)%360.0d;
	}


}
