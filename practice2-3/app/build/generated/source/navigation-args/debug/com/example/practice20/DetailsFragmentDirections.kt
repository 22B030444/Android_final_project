package com.example.practice20

import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections

public class DetailsFragmentDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionDetailsFragmentToHomeFragment2(): NavDirections = ActionOnlyNavDirections(R.id.action_detailsFragment_to_homeFragment2)
  }
}
