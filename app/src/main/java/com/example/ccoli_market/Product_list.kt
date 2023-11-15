package com.example.ccoli_market

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ccoli_market.databinding.ActivityMainBinding
import com.example.ccoli_market.databinding.ProductListBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProductListFragment : Fragment(R.layout.product_list) {

    private lateinit var binding: ProductListBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ProductListBinding.bind(view)

        val dataList = mutableListOf<MyItem>()
        dataList.add(MyItem(R.drawable.sample1, "산지 한달된 선풍기 팝니다", "서울 서대문구 창천동", "1,000원", 13, 25, "대현동", "이사가서 필요가 없어졌어요 급하게 내놓습니다", false))
        // ... (나머지 데이터 추가)

        val adapter = MyAdapter(dataList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val recyclerView = binding.recyclerView
        val decoration = AddressAdapterDecoration()
        recyclerView.addItemDecoration(decoration)

        // Spinner list
        val adList =  resources.getStringArray(R.array.spinnerArray)
        val adAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, adList)
        binding.spinner.adapter = adAdapter

        // 클릭한 데이터 상세페이지로 넘기기
        adapter.itemClick = object : MyAdapter.ItemClick {
            override fun onClick(view: View, position: Int) {
                val clickedItem = dataList[position]
                val intent = Intent(requireContext(), DetailActivity::class.java)
                intent.putExtra("likePosition", position)
                intent.putExtra("myItem", clickedItem)
                activityResultLauncher.launch(intent)
            }
        }

        // 롱클릭 삭제 -> 목록 리스트에서 삭제하는 부분
        adapter.longItemClick = object : MyAdapter.LongItemClick {
            override fun onLongClick(view: View, position: Int) {
                val itemRomove = dataList[position]
                AlertDialog.Builder(requireContext())
                    .setIcon(R.drawable.chat)
                    .setTitle("삭제")
                    .setMessage("정말로 삭제하시겠습니까?")
                    .setPositiveButton("확인") { dialog, _ ->
                        dataList.remove(itemRomove)
                        adapter.notifyDataSetChanged()
                    }
                    .setNegativeButton("취소") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        // floating button
        val fadeIn = AlphaAnimation(0f, 1f).apply { duration = 700 }
        val fadeOut = AlphaAnimation(1f, 0f).apply { duration = 700 }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // 항상 FAB를 표시
                if (binding.floatingButton.visibility == View.INVISIBLE) {
                    binding.floatingButton.visibility = View.VISIBLE
                    binding.floatingButton.startAnimation(fadeIn)
                }
            }
        })

        binding.floatingButton.setOnClickListener {
            binding.recyclerView.smoothScrollToPosition(0)
        }

        // Notification
        // binding.notiButton.setOnClickListener {
        //     showNotification()
        // }

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val likePosition = it.data?.getIntExtra("likePosition",0) as Int
                val isLiked = it.data?.getBooleanExtra("isLiked",false) as Boolean
                if (isLiked) {
                    dataList[likePosition].isLiked = true
                    dataList[likePosition].likeCount += 1
                } else {
                    if (dataList[likePosition].isLiked) {
                        dataList[likePosition].isLiked = false
                        dataList[likePosition].likeCount -= 1
                    }
                }
                adapter.notifyItemChanged(likePosition)
            }
        }
    }

    private fun showNotification() {
        val manager = requireContext().getSystemService(NotificationManager::class.java)

        val builder: NotificationCompat.Builder
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelId="one-channel"
            val channelName="My Channel One"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "My Channel One Description"
                setShowBadge(true)
                val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                setSound(uri, audioAttributes)
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
            builder = NotificationCompat.Builder(requireContext(), channelId)
        } else {
            builder = NotificationCompat.Builder(requireContext())
        }

        builder.run {
            setSmallIcon(R.drawable.colli_icon2)
            setWhen(System.currentTimeMillis())
            setContentTitle("키워드 알림")
            setContentText("설정한 키워드에 대한 알림이 도착했습니다!!")
        }
        manager.notify(11, builder.build())
    }

    // 뒤로가기 버튼 클릭 시 종료 다이얼로그
    fun onBackPressed() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.chat)
            .setTitle("종료")
            .setMessage("정말로 종료하시겠습니까?")
            .setPositiveButton("확인") { dialog, _ ->
                requireActivity().finish()
            }
            .setNegativeButton("취소"){ dialog,_ ->
                dialog.dismiss()
            }
            .show()
    }
}
