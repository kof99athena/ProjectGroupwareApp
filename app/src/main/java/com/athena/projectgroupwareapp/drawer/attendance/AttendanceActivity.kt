package com.athena.projectgroupwareapp.drawer.attendance

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.athena.projectgroupwareapp.databinding.ActivityAttendanceBinding
import com.athena.projectgroupwareapp.drawer.attendance.recycler.AttendanceItem
import com.athena.projectgroupwareapp.login.G
import com.athena.projectgroupwareapp.main.tab2.chatting.MyItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.util.maps.helper.Utility
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class AttendanceActivity : AppCompatActivity() {
    //파이어베이스에 데이터를 추가하자
    lateinit var firebase : FirebaseFirestore
    lateinit var chatRef : CollectionReference //컬렉션 참조(→)하는 변수
    lateinit var chatRef2 : DocumentReference //컬렉션 참조(→)하는 변수


    //카카오 맵을 추가하자
    //카카오는 그래들 라이브러리가 아니라서 다른 사이트들과 다르다.

    lateinit var binding : ActivityAttendanceBinding
    var myLocation : Location?= null //내 위치를 못 찾아 올수도 있으니까 null로 해준다
    //[ Google Fused Location API 사용 : play-services-location ]
    //위치는 아주 위험하다 퍼미션 (동적 퍼미션)
    val providerClient : FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this) } //사용자측에서 제공하는 위치
    //멤버변수에서는 context를 바로 쓰지 못한다. myLocation에 내 위치를 준다.


    val mapView : MapView by lazy { MapView(this) } //맵뷰객체를 나중에 생성할것이다


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = com.athena.projectgroupwareapp.databinding.ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //시작하면 내 위치 정보제공에 대한 동적 퍼미션 시작
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_DENIED){
            //거부되었으니 퍼미션을 받아오자! 퍼미션 받아오는 객체 준비.
         permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }else{
            requstMyLocations()
        }

        //사원이 회사 근처에 있다면 출퇴근 버튼을 누를수있게하자.
        //일단 내 위치 찾아오기!
        var keyHash : String = Utility.getKeyHash(this)
        Log.i("keyhash",keyHash)

        //MapView 객체 생성 및 ViewGroup에 붙이기
        val containerMapview : ViewGroup? = null
        containerMapview?.addView(mapView)


        attendance() //출퇴근버튼 및 내역 눌렀을때 발동하는 메소드

    }//onCreate


    //퍼미션 받아오는 객체이다.
    val permissionLauncher : ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission(),object :ActivityResultCallback<Boolean>{
        override fun onActivityResult(result: Boolean?) {
            if(result!!) requstMyLocations() //내위치 찾아오는 작업메소드, resulr!!는 결과가 true일때이다.
            else Toast.makeText(this@AttendanceActivity, "위치찾기를 허용해주세요.", Toast.LENGTH_SHORT).show()
        }
    })

    fun requstMyLocations(){
        //위치정보 기준을 설정하는 요청 객체
        val request : com.google.android.gms.location.LocationRequest = com.google.android.gms.location.LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,100).build()
        //PRIORITY_HIGH_ACCURACY  : GPS로 우선 적용해주세요

        //실시간 위치정보 갱신 요청 - 이정보는 위치정보가 있을때만 쓸수있다. 동적 허가 받았는지 실행문 써야한다.-> 그걸 onCreate메소드에서 썼으니까 이 지역에서는 못본다.
       //그걸 명시해줘야했다. addpermissioncheck를 자동으로 실행해줘야한다
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            return
        }
        providerClient.requestLocationUpdates(request,locationCallback, Looper.getMainLooper())

    }

    //위치검색결과 콜백객체
    val locationCallback : LocationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            myLocation = p0.lastLocation
            //정보 얻어왔으니까 실시간 업데이트 종료
            providerClient.removeLocationUpdates(this) //this는 메인이 아니다. locationCallback이다
        }

    }


    //출퇴근 및 내역 눌렀을때 발동하는 메소드
    fun attendance(){
        firebase = FirebaseFirestore.getInstance()
        chatRef = firebase.collection("attendance")

        chatRef2 = firebase.collection("attendance")
            .document(G.employeeAccount?.id.toString())// 안에 필드가없으면 도큐먼트 사이즈가 0이나온다. 하나는 넣어주자.

        //1. 오늘의 날짜를 표기하자
        var sdf = SimpleDateFormat("YYYY.MM.dd")
        var today = sdf.format(Date())
        binding.today.text = today


        var timeIn : String = String()
        var timeOut :String = String()


        //출근버튼을 누르면 출근 시간을 찍어주자.
        binding.btnIn.setOnClickListener {

            var calendar : Calendar = Calendar.getInstance()
            var t1 : String = "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"
            binding.timeIn.text = t1
            timeIn = binding.timeIn.text.toString()

            var attendanceItem : AttendanceItem = AttendanceItem(today,timeIn,timeOut)
        }

        //퇴근버튼을 누르면 퇴근 시간을 찍어주자.
        binding.btnOut.setOnClickListener {

            var calendar : Calendar = Calendar.getInstance()
            var t2 : String = "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"
            binding.timeOut.text = t2
            timeOut = binding.timeOut.text.toString()

            var attendanceItem : AttendanceItem = AttendanceItem(today,timeIn,timeOut)
            chatRef.document(G.employeeAccount?.id.toString()).collection("attendance").document(today.toString()).set(attendanceItem)

            var myitem : MyItem = MyItem(G.employeeAccount?.name.toString())
            chatRef2.set(myitem)
        }

        binding.myattendance.setOnClickListener{
            var intent : Intent = Intent(this,AttendanceListActivity::class.java)
            startActivity(intent)
        }

    }




}//onCreate


