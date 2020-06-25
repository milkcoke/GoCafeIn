package com.example.gocafein

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class LocationPermissionDialogActivity : DialogFragment() {
    internal lateinit var noticeDialogListener: NoticeDialogListener

    interface NoticeDialogListener {
        fun onDialogConfirmButtonClick()
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(context)
        val inflater = requireActivity().layoutInflater

//.setView(inflater.inflate(R.layout.activity_location_permission_dialog, null))
        builder
            .setTitle("Go CafeIn")
            .setIcon(R.drawable.gocafein_app_logo)
            .setMessage("카페를 찾기위해 위치 권한이 필요해요!")
            .setNeutralButton("확인", DialogInterface.OnClickListener { dialog, which ->
                noticeDialogListener.onDialogConfirmButtonClick()
            })
        return builder.create()
    }

//    onCreate 이전에 호출되는 콜백 메소드
//    Dialog fragment receives a reference to parent Activity through this method
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            noticeDialogListener = context as NoticeDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + "반드시 NOticeDialogListener를 구현해야합니다."))
        }
    }
}