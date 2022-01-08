/* Copyright 2021,  Gergana Kirilova

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.udacity.project4.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.idling.CountingIdlingResource

/**Custom IdlingResources class to test multiple toasts,idea taken from here:
 * https://knowledge.udacity.com/questions/770651 */

object ToastIdlingResource {
    private val idlingResource: CountingIdlingResource = CountingIdlingResource("toast")

    fun getIdlingResource(): IdlingResource {
        return idlingResource
    }

    fun increment() {
        idlingResource.increment()
    }

    private val listener: View.OnAttachStateChangeListener =
        object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View?) {
                if (!idlingResource.isIdleNow) {
                    idlingResource.decrement()
                }
            }

            override fun onViewDetachedFromWindow(v: View?) {}
        }

    fun showToast(context: Context?, text: CharSequence?, duration: Int): Toast {
        val t: Toast = Toast.makeText(context, text, duration)
        t.view?.addOnAttachStateChangeListener(listener)
        return t
    }

}
