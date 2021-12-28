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

object Constants {

    const val GEOFENCE_RADIUS_IN_METERS = 100f

    // set longer responsiveness to check for geofence entry alerts less often,
    // reducing battery drainage
    const val NOTIFICATION_RESPONSIVENESS_IN_MS = 300_000L

    internal const val ACTION_GEOFENCE_EVENT =
        "SaveReminderFragment.savereminder.action.ACTION_GEOFENCE_EVENT"
}