# Location Reminder

A Todo list app with location reminders that remind the user to do something when he reaches a specific location. The app will require the user to create an account and login to set and access reminders.

## Getting Started

1. Clone the project to your local machine.
2. Open the project using Android Studio.


### Installation

Step by step explanation of how to get a dev environment running.

```
1. To enable Firebase Authentication:
        a. Go to the authentication tab at the Firebase console and enable Email/Password and Google Sign-in methods.
        b. download `google-services.json` and add it to the app.
2. To enable Google Maps:
    a. Go to APIs & Services at the Google console.
    b. Select your project and go to APIs & Credentials.
    c. Create a new api key and restrict it for android apps.
    d. Add your package name and SHA-1 signing-certificate fingerprint.
    c. Enable Maps SDK for Android from API restrictions and Save.
    d. Copy the api key to your local.properties file like this:
       MAPS_API_KEY_DEBUG=your_key_here
3. Run the app on your mobile phone or emulator with Google Play Services on it.
```

## Testing

Right click on the `test` or `androidTest` packages and select Run Tests

### Tests Breakdown


```
1.androidTest
        RemindersDaoTest - tests the RemindersDao methods to ensure that read / write / delete operations function as expected.
        RemindersLocalRepositoryTest - Integration test between the RemindersLocalRepository and RemindersDao s' methods to ensure that read / write / delete operations function         as expected.
        ReminderListFragmentTest - tests the reminder's navigation functionality as well as the data ,available in the UI, to ensure that read/write/delete UI-related operations
        function as expected and error-containing snackbar is displayed if applicable.
        RemindersActivityTest - end-to-end black box test for the RemindersActivity , to ensure that the UX is smooth and the proper data and widgets are present in the UI               during specific user interactions.
        
2. test
      RemindersListViewModelTest - tests the methods of the RemindersListViewModel and the LiveData's values to ensure that read/write/delete operations deliver the expected           output and error values are produced where applicable.
      SaveReminderViewModelTest - tests the methods of the RemindersListViewModel and the LiveData's values to ensure that read/write/delete operations deliver the expected           output and error values are produced where applicable.
      
```

## Project Instructions I hereby,being the student, completed
    1. Create a Login screen to ask users to login using an email address or a Google account.  Upon successful login, navigate the user to the Reminders screen.   If there is no account, the app should navigate to a Register screen.
    2. Create a Register screen to allow a user to register using an email address or a Google account.
    3. Create a screen that displays the reminders retrieved from local storage. If there are no reminders, display a   "No Data"  indicator.  If there are any errors, display an error message.
    4. Create a screen that shows a map with the user's current location and asks the user to select a point of interest to create a reminder.
    5. Create a screen to add a reminder when a user reaches the selected location.  Each reminder should include
        a. title
        b. description
        c. selected location
    6. Reminder data should be saved to local storage.
    7. For each reminder, create a geofencing request in the background that fires up a notification when the user enters the geofencing area.
    8. Provide testing for the ViewModels, Coroutines and LiveData objects.
    9. Create a FakeDataSource to replace the Data Layer and test the app in isolation.
    10. Use Espresso and Mockito to test each screen of the app:
        a. Test DAO (Data Access Object) and Repository classes.
        b. Add testing for the error messages.
        c. Add End-To-End testing for the Fragments navigation.

## Built With

* [Koin](https://github.com/InsertKoinIO/koin) - A pragmatic lightweight dependency injection framework for Kotlin.
* [FirebaseUI Authentication](https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md) - FirebaseUI provides a drop-in auth solution that handles the UI flows for signing
* [JobIntentService](https://developer.android.com/reference/androidx/core/app/JobIntentService) - Run background service from the background application, Compatible with >= Android O.

## License

Copyright 2021,  Gergana Kirilova

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Screenshots

<img src="https://user-images.githubusercontent.com/51824954/147573847-1e116480-ee45-4a84-869a-0df5a7ccf89a.png" width="30%"></img>
<img src="https://user-images.githubusercontent.com/51824954/147573987-56345f2a-7916-4918-8fae-9923a6c066cc.png" width="30%"></img>
<img src="https://user-images.githubusercontent.com/51824954/147574106-3efa449e-1a77-43b6-a73f-7f17a273fb9d.png" width="30%"></img>
<img src="https://user-images.githubusercontent.com/51824954/147574143-0ae41ea3-4fc3-4629-ac7f-70e08eb897d4.png" width="30%"></img>
<img src="https://user-images.githubusercontent.com/51824954/147574252-ec2b9092-3fb5-444a-9f0a-9bf574c5be20.png" width="30%"></img>
<img src="https://user-images.githubusercontent.com/51824954/147574303-30805922-3e2f-420b-9131-2aacd4236641.png" width="30%"></img>
<img src="https://user-images.githubusercontent.com/51824954/148388028-1bc0bd53-ad42-4513-8276-33087646fb29.png" width="30%"></img>
<img src="https://user-images.githubusercontent.com/51824954/148388125-19ccdbaa-35a5-415a-979d-429ff87e911e.png" width="30%"></img>
<img src="https://user-images.githubusercontent.com/51824954/148388233-afa3e8d2-24f3-41ab-9176-5bc4612f7125.png" width="30%"></img>
<img src="https://user-images.githubusercontent.com/51824954/147574523-b0832449-37aa-4e45-ba9c-6ad019a1a6c5.png" width="30%"></img>
<img src="https://user-images.githubusercontent.com/51824954/147574586-0edacf76-b8cb-46f0-a1af-30fa5d5e0129.png" width="30%"></img>
<img src="https://user-images.githubusercontent.com/51824954/147574625-126bc90d-5172-4214-8457-0ddd7e67418c.png" width="30%"></img>
<img src="https://user-images.githubusercontent.com/51824954/147574661-c356e8c3-1aeb-4b71-bd67-2dcfdbf12b1b.png" width="30%"></img>
