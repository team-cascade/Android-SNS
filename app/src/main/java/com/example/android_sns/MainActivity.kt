package com.example.android_sns

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.android_sns.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private var bottomNavigationView: BottomNavigationView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 저장소 동적권한 요청
        requestSinglePermission(Manifest.permission.READ_EXTERNAL_STORAGE)

        bottomNavigationView = binding.bottomNavigation

        supportFragmentManager.beginTransaction().add(R.id.fragment, FeedFragment())
            .commit()

        // 게시글 업로드 후 메인화면으로 다시 바텀 네비게이션의 위치를 바꿈
        val getResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, FeedFragment()).commit()
                    bottomNavigationView!!.selectedItemId = R.id.feedFragment
                }
            }

        val goUploadIntent = Intent(this, UploadActivity::class.java)

        // 바텀 네비 설정
        bottomNavigationView?.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.feedFragment -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, FeedFragment()).commit()
                R.id.searchFragment -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, SearchFragment()).commit()
                R.id.alarmFragment -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, AlarmFragment()).commit()

                R.id.profileFragment -> {
                    var profileFragment = ProfileFragment()
                    var bundle = Bundle()
                    var uid = FirebaseAuth.getInstance().currentUser?.uid
                    bundle.putString("destinationUid",uid)
                    profileFragment.arguments = bundle
                    supportFragmentManager.beginTransaction().replace(R.id.fragment, profileFragment).commit()
                }
                R.id.uploadActivity -> getResult.launch(goUploadIntent)
            }
            true
        } )

        // 계정 없을 시 로그인 액티비티로 이동
        if (Firebase.auth.currentUser == null) {
            startActivity(
                Intent(this, LoginActivity::class.java))
            finish()
        }

        // WearOS로 auth 데이터 전송
       else {
            val token = FirebaseAuth.getInstance().currentUser!!.uid // auth token to transmit to the wearable device
            val dataClient: DataClient = Wearable.getDataClient(applicationContext)
            val putDataReq: PutDataRequest = PutDataMapRequest.create("/auth").run {
                dataMap.putString("token", token)
                asPutDataRequest()
            }
            val putDataTask: Task<DataItem> = dataClient.putDataItem(putDataReq).addOnCompleteListener {
                Log.w("debug-1",it.result.toString())
            }
        }
    }
    // SearchFragment에서 사용하는 메소드
    // 검색 텍스트를 인자로 넘겨서 프로필 프래그먼트로 이동
    fun goProfileFragment(_uid: String?) {
                    var profileFragment = ProfileFragment()
                    var bundle = Bundle()
                    var uid = _uid
                    bundle.putString("destinationUid",uid)
                    profileFragment.arguments = bundle
                    supportFragmentManager.beginTransaction().replace(R.id.fragment, profileFragment).commit()
    }

    // 동적권한 요청
    private fun requestSinglePermission(permission: String) { // 한번에 하나의 권한만 요청하는 예제
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) // 권한 유무 확인
            return
        val requestPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { // 권한 요청 컨트랙트
            if (it == false) { // permission is not granted!
                AlertDialog.Builder(this).apply {
                    setTitle("Warning")
                    setMessage(getString(R.string.no_permission, permission))
                }.show()
            }
        }
        if (shouldShowRequestPermissionRationale(permission)) { // 권한 설명 필수 여부 확인
// you should explain the reason why this app needs the permission.
            AlertDialog.Builder(this).apply {
                setTitle("Reason")
                setMessage(getString(R.string.req_permission_reason, permission))
                setPositiveButton("Allow") { _, _ -> requestPermLauncher.launch(permission) }
                setNegativeButton("Deny") { _, _ -> }
            }.show()
        } else {
// should be called in onCreate()
            requestPermLauncher.launch(permission) // 권한 요청 시작
        }
    }

}
