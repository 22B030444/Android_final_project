package com.example.practice20

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException
import kotlin.Suppress
import kotlin.jvm.JvmStatic

public data class DetailsFragmentArgs(
  public val post: Post,
) : NavArgs {
  @Suppress("CAST_NEVER_SUCCEEDS")
  public fun toBundle(): Bundle {
    val result = Bundle()
    if (Parcelable::class.java.isAssignableFrom(Post::class.java)) {
      result.putParcelable("post", this.post as Parcelable)
    } else if (Serializable::class.java.isAssignableFrom(Post::class.java)) {
      result.putSerializable("post", this.post as Serializable)
    } else {
      throw UnsupportedOperationException(Post::class.java.name + " must implement Parcelable or Serializable or must be an Enum.")
    }
    return result
  }

  @Suppress("CAST_NEVER_SUCCEEDS")
  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    if (Parcelable::class.java.isAssignableFrom(Post::class.java)) {
      result.set("post", this.post as Parcelable)
    } else if (Serializable::class.java.isAssignableFrom(Post::class.java)) {
      result.set("post", this.post as Serializable)
    } else {
      throw UnsupportedOperationException(Post::class.java.name + " must implement Parcelable or Serializable or must be an Enum.")
    }
    return result
  }

  public companion object {
    @JvmStatic
    @Suppress("DEPRECATION")
    public fun fromBundle(bundle: Bundle): DetailsFragmentArgs {
      bundle.setClassLoader(DetailsFragmentArgs::class.java.classLoader)
      val __post : Post?
      if (bundle.containsKey("post")) {
        if (Parcelable::class.java.isAssignableFrom(Post::class.java) || Serializable::class.java.isAssignableFrom(Post::class.java)) {
          __post = bundle.get("post") as Post?
        } else {
          throw UnsupportedOperationException(Post::class.java.name + " must implement Parcelable or Serializable or must be an Enum.")
        }
        if (__post == null) {
          throw IllegalArgumentException("Argument \"post\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"post\" is missing and does not have an android:defaultValue")
      }
      return DetailsFragmentArgs(__post)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): DetailsFragmentArgs {
      val __post : Post?
      if (savedStateHandle.contains("post")) {
        if (Parcelable::class.java.isAssignableFrom(Post::class.java) || Serializable::class.java.isAssignableFrom(Post::class.java)) {
          __post = savedStateHandle.get<Post?>("post")
        } else {
          throw UnsupportedOperationException(Post::class.java.name + " must implement Parcelable or Serializable or must be an Enum.")
        }
        if (__post == null) {
          throw IllegalArgumentException("Argument \"post\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"post\" is missing and does not have an android:defaultValue")
      }
      return DetailsFragmentArgs(__post)
    }
  }
}
