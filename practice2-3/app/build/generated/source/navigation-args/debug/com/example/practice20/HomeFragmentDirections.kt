package com.example.practice20

import android.os.Bundle
import android.os.Parcelable
import androidx.`annotation`.CheckResult
import androidx.navigation.NavDirections
import java.io.Serializable
import java.lang.UnsupportedOperationException
import kotlin.Int
import kotlin.Suppress

public class HomeFragmentDirections private constructor() {
  private data class ActionHomeFragmentToDetailsFragment(
    public val post: Post,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_homeFragment_to_detailsFragment

    public override val arguments: Bundle
      @Suppress("CAST_NEVER_SUCCEEDS")
      get() {
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
  }

  public companion object {
    @CheckResult
    public fun actionHomeFragmentToDetailsFragment(post: Post): NavDirections = ActionHomeFragmentToDetailsFragment(post)
  }
}
