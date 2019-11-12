package de.felixnuesse.timedsilence.handler

/**
 * Copyright (C) 2019  Felix Nüsse
 * Created on 10.04.19 - 18:07
 *
 * Edited by: Felix Nüsse felix.nuesse(at)t-online.de
 *
 *
 * This program is released under the GPLv3 license
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 *
 *
 */

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import android.support.v4.app.ActivityCompat.finishAffinity
import android.util.Log
import de.felixnuesse.timedsilence.Constants
import de.felixnuesse.timedsilence.Constants.Companion.APP_NAME
import de.felixnuesse.timedsilence.PrefConstants
import de.felixnuesse.timedsilence.R

class VolumeHandler {
    companion object {
        fun getVolumePermission(activity: Activity) {
            val notificationManager = activity.baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!notificationManager.isNotificationPolicyAccessGranted) {

                Log.d(Constants.APP_NAME, "VolumeHandler: Ask for DND-Access")

                val builder = AlertDialog.Builder(activity)
                builder.setMessage(R.string.GrantDNDPermissionAccess)
                    .setPositiveButton(R.string.GrantDND,
                        DialogInterface.OnClickListener { dialog, id ->
                            val intent = Intent(
                                Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                            )
                            activity.baseContext.startActivity(intent)
                        })
                    .setNegativeButton(R.string.cancel,
                        DialogInterface.OnClickListener { dialog, id ->
                            Log.e(Constants.APP_NAME, "VolumeHandler: Did not get 'Do not Disturb'-Access, quitting...")
                            finishAffinity(activity)
                        })
                // Create the AlertDialog object and return it
                builder.create().show()
            }
        }

        private const val UNSET = -1
        private const val SILENT = 0
        private const val VIBRATE = 1
        private const val LOUD = 2
    }

    var volumeSetting = UNSET

    fun setSilent(){
        volumeSetting = SILENT
    }

    fun setVibrate(){
        if(volumeSetting != SILENT){
            volumeSetting = VIBRATE
        }
    }

    fun setLoud(){
        if(volumeSetting != SILENT && volumeSetting != VIBRATE){
            volumeSetting = LOUD
        }
    }

    private fun applySilent(context: Context) {

        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        mNotificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
        mNotificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)


        if(manager.ringerMode!= AudioManager.RINGER_MODE_SILENT){
            manager.ringerMode=AudioManager.RINGER_MODE_SILENT
        }


        if(!manager.isMusicActive){
            setMediaVolume(0, context, manager)
        }

        setStreamToPercent(
            manager,
            AudioManager.STREAM_ALARM,
            SharedPreferencesHandler.getPref(context, PrefConstants.PREF_VOLUME_ALARM, PrefConstants.PREF_VOLUME_ALARM_DEFAULT)
        )
        setStreamToPercent(
            manager,
            AudioManager.STREAM_NOTIFICATION,
            0
        )
        setStreamToPercent(
            manager,
            AudioManager.STREAM_RING,
            0
        )

    }

    private fun applyLoud(context: Context) {

    val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    mNotificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)

    if(manager.ringerMode!= AudioManager.RINGER_MODE_NORMAL){
        manager.ringerMode = AudioManager.RINGER_MODE_NORMAL
    }

    var alarmVolume=SharedPreferencesHandler.getPref(context, PrefConstants.PREF_VOLUME_ALARM, PrefConstants.PREF_VOLUME_ALARM_DEFAULT)
    var mediaVolume=SharedPreferencesHandler.getPref(context, PrefConstants.PREF_VOLUME_MUSIC, PrefConstants.PREF_VOLUME_MUSIC_DEFAULT)
    var notifcationVolume=SharedPreferencesHandler.getPref(context, PrefConstants.PREF_VOLUME_NOTIFICATION, PrefConstants.PREF_VOLUME_NOTIFICATION_DEFAULT)
    var ringerVolume=SharedPreferencesHandler.getPref(context, PrefConstants.PREF_VOLUME_RINGER, PrefConstants.PREF_VOLUME_RINGER_DEFAULT)


    if(!manager.isMusicActive){
        setMediaVolume(mediaVolume, context, manager)
    }

    setStreamToPercent(
        manager,
        AudioManager.STREAM_ALARM,
        alarmVolume
    )
    setStreamToPercent(
        manager,
        AudioManager.STREAM_NOTIFICATION,
        notifcationVolume
    )
    setStreamToPercent(
        manager,
        AudioManager.STREAM_RING,
        ringerVolume
    )

}

    private fun applyVibrate(context: Context) {

        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if(manager.ringerMode!= AudioManager.RINGER_MODE_VIBRATE){
            manager.ringerMode=AudioManager.RINGER_MODE_VIBRATE
        }


        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        mNotificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)


        if(!manager.isMusicActive){
            setMediaVolume(0, context, manager)
        }



        var alarmVolume=SharedPreferencesHandler.getPref(context, PrefConstants.PREF_VOLUME_ALARM, PrefConstants.PREF_VOLUME_ALARM_DEFAULT)
        if(false){
            alarmVolume=0;
        }


        setStreamToPercent(
            manager,
            AudioManager.STREAM_ALARM,
            alarmVolume
        )
        setStreamToPercent(
            manager,
            AudioManager.STREAM_NOTIFICATION,
            0
        )
        setStreamToPercent(
            manager,
            AudioManager.STREAM_RING,
            0
        )

    }

    private fun setStreamToPercent(manager: AudioManager, stream: Int, percentage: Int) {
    val maxVol = manager.getStreamMaxVolume(stream)*100
    val onePercent = maxVol / 100
    val vol = (onePercent * percentage)/100
    manager.setStreamVolume(stream, vol, 0)
}

    private fun setMediaVolume(percentage: Int, context: Context, manager: AudioManager){


    Log.d(Constants.APP_NAME, "VolumeHandler: Setting Audio Volume!")

    val ignoreCheckWhenConnected=SharedPreferencesHandler.getPref(context, PrefConstants.PREF_IGNORE_CHECK_WHEN_HEADSET, PrefConstants.PREF_IGNORE_CHECK_WHEN_HEADSET_DEFAULT)

    if(HeadsetHandler.headphonesConnected(context) && ignoreCheckWhenConnected){
        Log.d(Constants.APP_NAME, "VolumeHandler: Found headset, skipping...")
        return
    }

    setStreamToPercent(
        manager,
        AudioManager.STREAM_MUSIC,
        percentage
    )
    Log.d(Constants.APP_NAME, "VolumeHandler: Mediavolume set.")

}

    fun isButtonClickAudible(context: Context): Boolean{
    Log.d(Constants.APP_NAME, "VolumeHandler: Check if Buttonclicks are audible")
    val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    if(0>=manager.getStreamVolume(AudioManager.STREAM_RING)){
        return false
    }
    return true
}

    fun applyVolume(context: Context){

        Log.d(Constants.APP_NAME, "VolumeHandler: VolumeSetting: $volumeSetting")

        when (volumeSetting) {
            SILENT -> applySilent(context)
            VIBRATE -> applyVibrate(context)
            LOUD -> applyLoud(context)
            else -> {
               // applySilent(context)
            }
        }
    }
}