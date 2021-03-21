package com.example.redesign

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.redesign.utils.Constants
import kotlinx.android.synthetic.main.frag1.*
import kotlin.random.Random as Random
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory


class Fragment1 : Fragment() {
    private lateinit var personArray : ArrayList<String>
    private lateinit var myungunArray : ArrayList<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        return inflater.inflate(R.layout.frag1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resultButton.setOnClickListener {
            companyInput()
        }
        myungunArray = parseMyungun(1)
        personArray = parseMyungun(2)
        setMyungun()
//        getCorpData 작동하는지 확인하기 위해 추가한 코드. 추후 삭제
//        mainActivity.getCorpData("anything")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("data1" ,text_myungun.text.toString())
    }

    // 명언 저장된 xml 파일을 nameArray, myungunArray 변수로 저장
    private fun parseMyungun(attributeIndex : Int) : ArrayList<String>{
        // attributeIndex 1 -> 명언
        // attributeIndex 2 -> 이름
        val myungunArray = arrayListOf<String>()

        val xmlData = context!!.assets.open("file1.xml")
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()

        parser.setInput(xmlData, null)

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name

            when (event) {
                XmlPullParser.END_TAG -> {
                    if (tagName == "text") {
                        val attribute = parser.getAttributeValue(attributeIndex)
                        myungunArray.add(attribute)
                    }
                }
            }
            event = parser.next()
        }
        return myungunArray
    }

    // 명언 수에서 저장된 명언과 이름 어레이에서
    private fun setMyungun(){
        val index = Random.nextInt(MainActivity.NUM_MYUNGUN)
        text_myungun.text = myungunArray[index]
        text_person.text = personArray[index]
    }

    private fun companyInput() {
        // TODO 회사명 없이 검색 눌렀을 때 검색 넘어가지 않게, 다른 에러 가능한 케이스도 처리해줘야함
        // TODO editText 클릭하고 다른 곳 누르면 키보드 없어지게 설정.줌

        // intent 로 Result 클래스 설정
        val intent = Intent(context, Result::class.java)

        // editText에 들어가 있는 사명을 잡아서 intent에 company 라는 이름으로 넘겨
        intent.putExtra("companyName",companyname.text?.toString()?:run{
            Log.i(Constants.TAG, "companyInput() intent.putExtra 에 null companyName 입력.")
            // TODO 여기 에러 대신 사명 입력 안했다는 경고창 뜨게 해주면 좋을 것 같습니다.
            throw IllegalArgumentException("company name is null")
        })
        startActivity(intent)
    }
}