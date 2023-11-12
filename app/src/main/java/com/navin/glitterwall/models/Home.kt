package com.navin.glitterwall.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Home(
    @SerializedName("all_video")
    val allWallpaper: List<AllVideo>,
    @SerializedName("category")
    val categoryWallpaper: List<Category>,
    @SerializedName("featured_video")
    val featuredWallpaper: List<FeaturedVideo>,
    @SerializedName("latest_video")
    val latestWallpaper: List<LatestVideo>
) : Parcelable