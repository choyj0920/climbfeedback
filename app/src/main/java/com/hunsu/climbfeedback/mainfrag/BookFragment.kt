package com.hunsu.climbfeedback.mainfrag
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hunsu.climbfeedback.R
import com.hunsu.climbfeedback.databinding.FragmentBookBinding
import com.hunsu.climbfeedback.mainfrag.adapter.Item
import com.hunsu.climbfeedback.mainfrag.adapter.RvAdapter
class BookFragment : Fragment() {
    private var binding: FragmentBookBinding? =null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentBookBinding.inflate(inflater,container,false)
        return binding!!.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        // 데이터 생성
        val itemList = listOf(
            Item(R.drawable.book_image, "클라이밍 입문 지식"),
            Item(R.drawable.img, "기본 용어 및 기술"),
            Item(R.drawable.warning, "안전 에티켓"),
            //Item(R.drawable.img, "Text 5"),
        )

        // Adapter 설정
        val adapter = RvAdapter(requireContext(), itemList) { item ->
            // 항목 클릭 시 상세 페이지로 이동
            val detailFragment = DetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("IMAGE_RES_ID", item.imageResId)
                    putString("TEXT", item.text)
                }
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, detailFragment)
                .addToBackStack(null)
                .commit()
        }
        recyclerView.adapter = adapter
    }
}