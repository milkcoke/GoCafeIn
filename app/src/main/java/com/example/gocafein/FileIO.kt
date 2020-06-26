package com.example.gocafein

import android.util.Log
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.*

object FileIO {
    val FILE_NAME = "seoul_noisy_2019_4.xls"


//    raw resource  파일을 Internal Storage로 옭김
    fun fileCopy(targetFile: File, rawResource: InputStream) {
        val inputStream : InputStream = rawResource
        val fileOutputStream = FileOutputStream(targetFile)
        try {
//            targetFile은 앱 내부 저장소의 seoul_noisy_2019_4.xls 파일이다.
            inputStream.copyTo(fileOutputStream, 1024)
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        } finally {
            inputStream.close()
            fileOutputStream.close()
        }
    }

    fun fileRead(targetFile: File) {

//        val fileReader = FileReader(targetFile)
//        일단 메뉴얼대로
        val workBook = HSSFWorkbookFactory.create(FileInputStream(targetFile))
        val rowNumber = 0
        val columnNumber = 0
        val sheet = workBook.getSheetAt(0)
        Log.i("excel", "${sheet.getRow(rowNumber).getCell(columnNumber)}")
//        val bufferReader = BufferedReader(fileReader)

//        try {
//            while(true) {
//                var wordString = bufferReader.readLine()
//                Log.i("file", wordString)
//            }
//        } catch (ioe: IOException) {
//            ioe.printStackTrace()
//        } finally {
//            bufferReader.close()
//        }
    }


}