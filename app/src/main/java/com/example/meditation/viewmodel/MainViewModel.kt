package com.example.meditation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.meditation.MyApplication
import com.example.meditation.R
import com.example.meditation.data.ThemeData
import com.example.meditation.model.UserSettings
import com.example.meditation.model.UserSettingsRepository
import com.example.meditation.util.PlayStatus
import kotlinx.android.synthetic.main.fragment_main.view.*
import java.sql.Time
import java.util.*
import kotlin.concurrent.schedule

class MainViewModel(private val context: Application): AndroidViewModel(context) {

    val msgUpperSmall = MutableLiveData<String>()
    val msgLowerLarge = MutableLiveData<String>()
    var themePicFileResId = MutableLiveData<Int>()
    var txtTheme = MutableLiveData<String>()
    var txtLevel = MutableLiveData<String>()
    var levelId = MutableLiveData<Int>()

    var timeId = MutableLiveData<Int>()
    var remainedTimeSeconds = MutableLiveData<Int>()
    var displayTimeSeconds = MutableLiveData<String>()
    
    val playStatus = MutableLiveData<Int>()

    private val userSettingRepository =
        UserSettingsRepository()
    private lateinit var userSettings: UserSettings

    // 呼吸感覚
    private val inhaleInterval = 4
    private var holdInterval = 0
    private var exhaleInterval = 0
    private var totalInterval = 0

    fun initParameters() {
        userSettings = userSettingRepository.loadUserSettings()
        msgUpperSmall.value = ""
        msgLowerLarge.value = ""
        themePicFileResId.value = userSettings.themeResId
        levelId.value = userSettings.levelId
        txtTheme.value = userSettings.themeName
        txtLevel.value = userSettings.levelName
        timeId.value = userSettings.timeId
        remainedTimeSeconds.value = userSettings.time * 60
        displayTimeSeconds.value = changeTimerFormat(remainedTimeSeconds.value!!)
        playStatus.value = 0
    }

    private fun changeTimerFormat(timeSeconds: Int): String {

        val mm = timeSeconds / 60; // 分
        val ss = timeSeconds % 60; // 秒
        return String.format("%1$02d:%2$02d", mm, ss)
//        return  TimeUnit.MINUTES.convert(timeSeconds.toLong(),TimeUnit.MILLISECONDS).toString()
    }

    fun setLevel(selectedItemId: Int) {
        levelId.value = selectedItemId
        txtLevel.value = userSettingRepository.setLevel(selectedItemId)
    }

    fun setTime(selectedItemId: Int) {
        timeId.value = selectedItemId
        remainedTimeSeconds.value = userSettingRepository.setTime(selectedItemId) * 60
        displayTimeSeconds.value = changeTimerFormat(remainedTimeSeconds.value!!)
    }

    fun setTheme(themeData: ThemeData) {
        userSettingRepository.setTheme(themeData)
        txtTheme.value = userSettingRepository.loadUserSettings().themeName
        themePicFileResId.value = userSettingRepository.loadUserSettings().themeResId
    }

    fun changeStatus() {
        when (playStatus.value) {
            PlayStatus.BEFORE_START -> playStatus.value = PlayStatus.ON_START
            PlayStatus.ON_START -> playStatus.value = PlayStatus.PAUSE
            PlayStatus.PAUSE -> playStatus.value = PlayStatus.RUNNING
        }
    }

    fun countDownBeforeStart() {
        msgUpperSmall.value = context.getString(R.string.starts_in)
        var timeRemained = 3
        msgLowerLarge.value = timeRemained.toString()
        val timer = Timer()
        timer.schedule(1000,1000) {
            if(timeRemained > 1){
                timeRemained -= 1
                msgLowerLarge.postValue(timeRemained.toString())

            } else {
                playStatus.postValue(PlayStatus.RUNNING)
                timeRemained = 0
                timer.cancel()
            }

        }
    }

    fun startMeditation() {
        holdInterval = setholdInterval()
        exhaleInterval = setExhaleInterval()
        totalInterval = inhaleInterval + holdInterval + exhaleInterval

        remainedTimeSeconds.value = adjustRemainedTIme(remainedTimeSeconds.value, totalInterval
        )
        displayTimeSeconds.value = changeTimerFormat(remainedTimeSeconds.value!!)
        msgUpperSmall.value = context.getString(R.string.inhale)
        msgLowerLarge.value = inhaleInterval.toString()

        clockMeditation()

    }
    private fun adjustRemainedTIme(remainedTime: Int?, totalInterval: Int): Int? {
        val remainder = remainedTime!! % totalInterval
        return if (remainder > (totalInterval / 2 )){
            remainedTime + (totalInterval - remainder)
        } else {
            remainedTime - remainder
        }
    }

    private fun setExhaleInterval(): Int {
        return when (userSettingRepository.loadUserSettings().levelId){
            0 -> 4
            1 -> 8
            2 -> 8
            3 -> 8
            else -> 0
        }
    }

    private fun setholdInterval(): Int {
        return when (userSettingRepository.loadUserSettings().levelId){
            0 -> 4
            1 -> 4
            2 -> 8
            3 -> 16
            else -> 0
        }
    }
}