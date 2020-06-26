package com.example.gocafein

import com.naver.maps.geometry.LatLng

//이거랑 별도화면이 필요해 (mapx, mapy)는 어차피 지도에 표시하는데 쓰일거고
//별도 화면에서 RecyclerView같은 형태로 표현하면 되지않을까? (이미지..?)

data class Cafe (val name: String,val snsLink: String, val roadAddress: String, var location: LatLng){
}