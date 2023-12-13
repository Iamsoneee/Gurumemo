package com.sw.gurumemo

object Constants {
    //　API keys (local.properties)
    const val HOTPEPPER_API_KEY = BuildConfig.hotpepper_api_key
    const val GOOGLE_MAPS_API_KEY = BuildConfig.google_maps_api_key

    //　東京駅（ユーザーが日本に居住していない場合のデフォルト値の設定）
    const val DEFAULT_LATITUDE_JP = 35.68111
    const val DEFAULT_LONGITUDE_JP = 139.76667
}